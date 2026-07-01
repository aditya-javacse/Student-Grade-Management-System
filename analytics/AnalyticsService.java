package analytics;

import dao.StudentDAO;
import dao.AssessmentDAO;
import dao.SubjectDAO;
import dao.AttendanceDAO;
import model.Student;
import model.Subject;
import model.Assessment;
import model.Attendance;

import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {
    private final StudentDAO studentDAO;
    private final AssessmentDAO assessmentDAO;
    private final SubjectDAO subjectDAO;
    private final AttendanceDAO attendanceDAO;

    public AnalyticsService(StudentDAO studentDAO, AssessmentDAO assessmentDAO, 
                            SubjectDAO subjectDAO, AttendanceDAO attendanceDAO) {
        this.studentDAO = studentDAO;
        this.assessmentDAO = assessmentDAO;
        this.subjectDAO = subjectDAO;
        this.attendanceDAO = attendanceDAO;
    }

    public static class ClassSummary {
        public int totalStudents;
        public double averagePercentage;
        public double passPercentage;
        public double failPercentage;
        public Student topper;
        public double topperAverage;
        public Map<String, Integer> gradeDistribution = new HashMap<>();
        public Map<String, Double> subjectAverages = new HashMap<>(); // SubjectCode -> Avg Score
    }

    /**
     * Computes comprehensive analytics for a given class.
     */
    public ClassSummary getClassAnalytics(String className) {
        ClassSummary summary = new ClassSummary();
        List<Student> students = studentDAO.getAll().stream()
                .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                .collect(Collectors.toList());
        
        summary.totalStudents = students.size();
        if (students.isEmpty()) {
            return summary;
        }

        // 1. Calculate individual student averages
        double totalClassSum = 0.0;
        int activeStudents = 0;
        
        Student topper = null;
        double topperAvg = -1.0;

        // Initialize grade distribution map
        summary.gradeDistribution.put("A+", 0);
        summary.gradeDistribution.put("A", 0);
        summary.gradeDistribution.put("B", 0);
        summary.gradeDistribution.put("C", 0);
        summary.gradeDistribution.put("D", 0);
        summary.gradeDistribution.put("F", 0);

        int totalPassCount = 0;
        int totalFailCount = 0;

        for (Student s : students) {
            List<Assessment> assessments = assessmentDAO.getByStudentId(s.getStudentId());
            if (assessments.isEmpty()) continue;

            double studentSum = 0.0;
            for (Assessment a : assessments) {
                studentSum += a.getTotalMarks();
                
                // Track pass/fail per assessment
                if (a.getTotalMarks() >= 50.0) {
                    totalPassCount++;
                } else {
                    totalFailCount++;
                }

                // Increment grade count
                String g = a.getGrade();
                summary.gradeDistribution.put(g, summary.gradeDistribution.getOrDefault(g, 0) + 1);
            }

            double studentAvg = studentSum / assessments.size();
            totalClassSum += studentAvg;
            activeStudents++;

            if (studentAvg > topperAvg) {
                topperAvg = studentAvg;
                topper = s;
            }
        }

        summary.averagePercentage = activeStudents > 0 ? (totalClassSum / activeStudents) : 0.0;
        summary.topper = topper;
        summary.topperAverage = topperAvg;

        int totalAssessments = totalPassCount + totalFailCount;
        if (totalAssessments > 0) {
            summary.passPercentage = ((double) totalPassCount / totalAssessments) * 100.0;
            summary.failPercentage = ((double) totalFailCount / totalAssessments) * 100.0;
        } else {
            summary.passPercentage = 100.0;
            summary.failPercentage = 0.0;
        }

        // 2. Calculate average per subject
        List<Subject> subjects = subjectDAO.getAll();
        for (Subject sub : subjects) {
            double subSum = 0.0;
            int count = 0;
            for (Student s : students) {
                Assessment a = assessmentDAO.getByStudentAndSubject(s.getStudentId(), sub.getSubjectId());
                if (a != null) {
                    subSum += a.getTotalMarks();
                    count++;
                }
            }
            if (count > 0) {
                summary.subjectAverages.put(sub.getCode(), subSum / count);
            }
        }

        return summary;
    }

    /**
     * Finds the topper for a specific subject in a class
     */
    public Student getSubjectTopper(String subjectId, String className, double[] outMaxScore) {
        List<Student> students = studentDAO.getAll().stream()
                .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                .collect(Collectors.toList());

        Student topper = null;
        double maxScore = -1.0;

        for (Student s : students) {
            Assessment a = assessmentDAO.getByStudentAndSubject(s.getStudentId(), subjectId);
            if (a != null && a.getTotalMarks() > maxScore) {
                maxScore = a.getTotalMarks();
                topper = s;
            }
        }

        if (outMaxScore != null && outMaxScore.length > 0) {
            outMaxScore[0] = maxScore;
        }
        return topper;
    }
}
