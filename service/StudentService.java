package service;

import dao.StudentDAO;
import dao.UserDAO;
import dao.AssessmentDAO;
import dao.SubjectDAO;
import dao.AttendanceDAO;
import dao.NotificationDAO;
import dao.ActivityLogDAO;
import model.Student;
import model.User;
import model.Assessment;
import model.Attendance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final AssessmentDAO assessmentDAO;
    private final SubjectDAO subjectDAO;
    private final AttendanceDAO attendanceDAO;
    private final NotificationDAO notificationDAO;
    private final ActivityLogDAO logDAO;

    public StudentService(StudentDAO studentDAO, UserDAO userDAO, AssessmentDAO assessmentDAO, 
                          SubjectDAO subjectDAO, AttendanceDAO attendanceDAO, 
                          NotificationDAO notificationDAO, ActivityLogDAO logDAO) {
        this.studentDAO = studentDAO;
        this.userDAO = userDAO;
        this.assessmentDAO = assessmentDAO;
        this.subjectDAO = subjectDAO;
        this.attendanceDAO = attendanceDAO;
        this.notificationDAO = notificationDAO;
        this.logDAO = logDAO;
    }

    /**
     * Add student and auto-provision their login account
     */
    public Student addStudent(String name, String rollNumber, String studentClass, String section, 
                              String email, String phoneNumber, String dateOfBirth, String address) {
        String studentId = studentDAO.generateNextId();
        Student student = new Student(studentId, name, rollNumber, studentClass, section, email, phoneNumber, dateOfBirth, address);
        studentDAO.save(student);

        // Auto-provision student portal account
        // Username is studentId, default password is "student123"
        String username = studentId.toLowerCase();
        String passwordHash = AuthService.hashPassword("student123");
        User studentUser = new User(
                username,
                passwordHash,
                "STUDENT",
                "What is your student ID?",
                studentId,
                studentId
        );
        userDAO.save(studentUser);

        logDAO.log(getActorUsername(), getActorRole(), "Added student: " + name + " (" + studentId + ") and auto-provisioned user account");
        return student;
    }

    /**
     * Update student details
     */
    public void updateStudent(Student student) {
        studentDAO.save(student);
        logDAO.log(getActorUsername(), getActorRole(), "Updated student details: " + student.getName() + " (" + student.getStudentId() + ")");
    }

    /**
     * Delete student and clean up all associated data cascadingly
     */
    public void deleteStudent(String studentId) {
        Student s = studentDAO.getById(studentId);
        if (s == null) return;

        // Cascade delete all references
        studentDAO.delete(studentId);
        assessmentDAO.deleteAllForStudent(studentId);
        subjectDAO.unassignAllForStudent(studentId);
        attendanceDAO.deleteAllForStudent(studentId);
        notificationDAO.deleteAllForStudent(studentId);
        
        // Delete linked user account
        User u = userDAO.getByLinkedId(studentId);
        if (u != null) {
            userDAO.delete(u.getUsername());
        }

        logDAO.log(getActorUsername(), getActorRole(), "Deleted student: " + s.getName() + " (" + studentId + ") and cascade cleared all records");
    }

    public List<Student> getAllStudents() {
        return studentDAO.getAll();
    }

    public Student getStudentById(String studentId) {
        return studentDAO.getById(studentId);
    }

    /**
     * Advanced multi-variable filtering for student searching
     */
    public List<Student> filterStudents(String searchPattern, String className, String section) {
        List<Student> students = studentDAO.getAll();
        
        return students.stream()
                .filter(s -> {
                    if (searchPattern == null || searchPattern.trim().isEmpty()) return true;
                    String pat = searchPattern.toLowerCase();
                    return s.getName().toLowerCase().contains(pat) || 
                           s.getStudentId().toLowerCase().contains(pat) ||
                           s.getRollNumber().toLowerCase().contains(pat);
                })
                .filter(s -> {
                    if (className == null || className.equals("All") || className.trim().isEmpty()) return true;
                    return s.getStudentClass().equalsIgnoreCase(className);
                })
                .filter(s -> {
                    if (section == null || section.equals("All") || section.trim().isEmpty()) return true;
                    return s.getSection().equalsIgnoreCase(section);
                })
                .collect(Collectors.toList());
    }

    private String getActorUsername() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUsername() : "SYSTEM";
    }

    private String getActorRole() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getRole() : "SYSTEM";
    }
}
