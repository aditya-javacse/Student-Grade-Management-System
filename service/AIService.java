package service;

import dao.AssessmentDAO;
import dao.SubjectDAO;
import model.Assessment;
import model.Subject;
import java.util.ArrayList;
import java.util.List;

public class AIService {
    private final AssessmentDAO assessmentDAO;
    private final SubjectDAO subjectDAO;

    public AIService(AssessmentDAO assessmentDAO, SubjectDAO subjectDAO) {
        this.assessmentDAO = assessmentDAO;
        this.subjectDAO = subjectDAO;
    }

    public static class AIRecommendation {
        public String subjectName;
        public String subjectCode;
        public double currentMarks;
        public String currentGrade;
        public List<String> weakAreas = new ArrayList<>();
        public List<String> studyTips = new ArrayList<>();
        public String projectedGrade;

        @Override
        public String toString() {
            return "Recommendation for " + subjectName + " (" + currentGrade + "): " + studyTips;
        }
    }

    /**
     * Generates a detailed AI report card recommendation set for a student.
     */
    public List<AIRecommendation> generateRecommendations(String studentId) {
        List<AIRecommendation> recommendations = new ArrayList<>();
        List<Assessment> assessments = assessmentDAO.getByStudentId(studentId);

        for (Assessment a : assessments) {
            Subject sub = subjectDAO.getById(a.getSubjectId());
            if (sub == null) continue;

            AIRecommendation rec = new AIRecommendation();
            rec.subjectName = sub.getName();
            rec.subjectCode = sub.getCode();
            rec.currentMarks = a.getTotalMarks();
            rec.currentGrade = a.getGrade();

            // Evaluate weaknesses & suggest study plans based on grade distribution
            boolean lowExam = a.getFinalExamMarks() < 60 || a.getMidTermMarks() < 60;
            boolean lowAssignment = a.getAssignmentMarks() < 60 || a.getQuizMarks() < 60;
            boolean lowInternal = a.getInternalAssessment() < 60;

            if (rec.currentMarks < 60.0) {
                rec.weakAreas.add("Core conceptual understanding in " + sub.getName());
            }

            if (lowExam && !lowAssignment) {
                rec.weakAreas.add("Exam performance & timed tests");
                rec.studyTips.add("Practice timed mock exams to manage exam time constraints.");
                rec.studyTips.add("Focus on core theoretical concepts under stress.");
            } else if (lowAssignment && !lowExam) {
                rec.weakAreas.add("Consistency in continuous assessments (Assignments & Quizzes)");
                rec.studyTips.add("Establish a daily schedule to finish assignments ahead of deadlines.");
                rec.studyTips.add("Attempt class quizzes seriously; review notes before each quiz.");
            } else if (lowExam && lowAssignment) {
                rec.weakAreas.add("Foundational knowledge of this subject");
                rec.studyTips.add("Schedule tutoring sessions or seek peer-study groups.");
                rec.studyTips.add("Re-study basics, starting with core textbook exercises.");
            }

            if (lowInternal) {
                rec.weakAreas.add("Class participation and teacher evaluation");
                rec.studyTips.add("Participate actively in class discussions and ask questions.");
            }

            // General recommendations for all subjects below 75%
            if (rec.currentMarks < 75.0) {
                if (rec.studyTips.isEmpty()) {
                    rec.studyTips.add("Dedicate at least 3 dedicated hours per week to practice problem sets.");
                }
                rec.studyTips.add("Create summary charts of key formulas/vocabulary for quick memory checks.");
            }

            // Predict Expected Grade
            // If they are showing good assignment performance, they are expected to do slightly better
            double projectedScore = rec.currentMarks;
            if (a.getAssignmentMarks() > rec.currentMarks + 10) {
                projectedScore += 3.0; // Boost because assignments are strong
                rec.projectedGrade = mapScoreToGrade(projectedScore) + " (With current upward assignment trend)";
            } else if (a.getAssignmentMarks() < rec.currentMarks - 10) {
                projectedScore -= 3.0; // Drop due to missing/low assignments
                rec.projectedGrade = mapScoreToGrade(projectedScore) + " (Alert: Could drop without coursework recovery)";
            } else {
                rec.projectedGrade = mapScoreToGrade(projectedScore);
            }

            // Only return recommendations for subjects that need improvement (grade below A, i.e., score < 85)
            // or if the student is struggling
            if (rec.currentMarks < 85.0 || !rec.weakAreas.isEmpty()) {
                recommendations.add(rec);
            }
        }
        return recommendations;
    }

    private String mapScoreToGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }
}
