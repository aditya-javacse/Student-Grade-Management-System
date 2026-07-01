package service;

import dao.AssessmentDAO;
import dao.StudentDAO;
import dao.SubjectDAO;
import dao.NotificationDAO;
import dao.ActivityLogDAO;
import model.Assessment;
import model.Student;
import model.Subject;
import model.Notification;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AssessmentService {
    private final AssessmentDAO assessmentDAO;
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;
    private final NotificationDAO notificationDAO;
    private final ActivityLogDAO logDAO;

    public AssessmentService(AssessmentDAO assessmentDAO, StudentDAO studentDAO, SubjectDAO subjectDAO, 
                             NotificationDAO notificationDAO, ActivityLogDAO logDAO) {
        this.assessmentDAO = assessmentDAO;
        this.studentDAO = studentDAO;
        this.subjectDAO = subjectDAO;
        this.notificationDAO = notificationDAO;
        this.logDAO = logDAO;
    }

    /**
     * Enters or updates marks for a student's subject, recalculates stats, 
     * and sends performance notifications if required.
     */
    public void saveAssessment(Assessment assessment) {
        assessment.recalculate();
        assessmentDAO.save(assessment);
        
        Subject sub = subjectDAO.getById(assessment.getSubjectId());
        String subjectName = (sub != null) ? sub.getName() : "Subject ID " + assessment.getSubjectId();

        // Check for low performance alert (< 50%)
        if (assessment.getTotalMarks() < 50.0) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String notId = notificationDAO.generateNextId();
            String title = "Low Performance Alert: " + subjectName;
            String message = String.format("You scored %.2f%% in %s. This falls below the passing mark. Please consult the AI Study Recommendation engine.", 
                    assessment.getTotalMarks(), subjectName);
            
            // Check if alert already exists for this subject to prevent spamming
            boolean exists = false;
            for (Notification n : notificationDAO.getByStudentId(assessment.getStudentId())) {
                if (n.getTitle().contains(subjectName) && !n.isRead()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                notificationDAO.save(new Notification(notId, assessment.getStudentId(), title, message, date, false));
            }
        }

        logDAO.log(getActorUsername(), getActorRole(), "Updated marks for Student ID " + assessment.getStudentId() + " in " + subjectName);
    }

    public List<Assessment> getAllAssessments() {
        return assessmentDAO.getAll();
    }

    public List<Assessment> getAssessmentsForStudent(String studentId) {
        return assessmentDAO.getByStudentId(studentId);
    }

    public Assessment getAssessment(String studentId, String subjectId) {
        return assessmentDAO.getByStudentAndSubject(studentId, subjectId);
    }

    /**
     * Calculates the overall Average Percentage of a student across all subjects
     */
    public double getStudentAveragePercentage(String studentId) {
        List<Assessment> assessments = assessmentDAO.getByStudentId(studentId);
        if (assessments.isEmpty()) return 0.0;
        
        double sum = 0.0;
        for (Assessment a : assessments) {
            sum += a.getTotalMarks();
        }
        return sum / assessments.size();
    }

    /**
     * Calculates the cumulative GPA of a student (out of 4.0 scale)
     */
    public double getStudentCGPA(String studentId) {
        List<Assessment> assessments = assessmentDAO.getByStudentId(studentId);
        if (assessments.isEmpty()) return 0.0;
        
        double sum = 0.0;
        for (Assessment a : assessments) {
            sum += a.getGpa();
        }
        return sum / assessments.size();
    }

    /**
     * Dynamic Class Rank calculation (based on average marks among classmates)
     */
    public int getStudentRankInClass(String studentId, String className) {
        if (studentId == null || className == null) return -1;
        List<Student> classmates = studentDAO.getAll().stream()
                .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                .collect(Collectors.toList());
        
        if (classmates.isEmpty()) return -1;

        // Map student ID to their average percentage
        Map<String, Double> studentAverages = new HashMap<>();
        for (Student s : classmates) {
            studentAverages.put(s.getStudentId(), getStudentAveragePercentage(s.getStudentId()));
        }

        // Sort student IDs by average descending
        List<String> sortedIds = studentAverages.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return sortedIds.indexOf(studentId) + 1;
    }

    /**
     * Dynamic Subject Rank calculation within a specific class
     */
    public int getStudentRankInSubject(String studentId, String subjectId, String className) {
        if (studentId == null || subjectId == null || className == null) return -1;
        List<Student> classmates = studentDAO.getAll().stream()
                .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                .collect(Collectors.toList());
        
        if (classmates.isEmpty()) return -1;

        Map<String, Double> studentSubjectMarks = new HashMap<>();
        for (Student s : classmates) {
            Assessment a = assessmentDAO.getByStudentAndSubject(s.getStudentId(), subjectId);
            double marks = (a != null) ? a.getTotalMarks() : -1.0; // -1 if not entered
            studentSubjectMarks.put(s.getStudentId(), marks);
        }

        // Filter out students who don't have grades entered
        List<String> sortedIds = studentSubjectMarks.entrySet().stream()
                .filter(e -> e.getValue() >= 0)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        int rank = sortedIds.indexOf(studentId);
        return rank == -1 ? -1 : rank + 1;
    }

    private String getActorUsername() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUsername() : "SYSTEM";
    }

    private String getActorRole() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getRole() : "SYSTEM";
    }
}
