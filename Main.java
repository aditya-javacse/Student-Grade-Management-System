import dao.*;
import service.*;
import analytics.AnalyticsService;
import util.MockDataInitializer;
import view.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel for native elements (window frames, scrollbars)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to cross-platform standard
        }

        // Initialize and seed CSV tables if they are empty
        MockDataInitializer.initialize();

        // Instantiate DAO layer
        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        AssessmentDAO assessmentDAO = new AssessmentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        ActivityLogDAO logDAO = new ActivityLogDAO();

        // Instantiate Service layer
        AuthService authService = new AuthService(userDAO, logDAO);
        StudentService studentService = new StudentService(studentDAO, userDAO, assessmentDAO, subjectDAO, attendanceDAO, notificationDAO, logDAO);
        SubjectService subjectService = new SubjectService(subjectDAO, assessmentDAO, logDAO);
        AssessmentService assessmentService = new AssessmentService(assessmentDAO, studentDAO, subjectDAO, notificationDAO, logDAO);
        AttendanceService attendanceService = new AttendanceService(attendanceDAO, studentDAO, notificationDAO, logDAO);
        AnalyticsService analyticsService = new AnalyticsService(studentDAO, assessmentDAO, subjectDAO, attendanceDAO);
        AIService aiService = new AIService(assessmentDAO, subjectDAO);
        ReportService reportService = new ReportService(studentDAO, subjectDAO, assessmentDAO, attendanceDAO, assessmentService, attendanceService, aiService);

        // Open Swing GUI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow frame = new MainWindow(
                    authService,
                    studentService,
                    subjectService,
                    assessmentService,
                    attendanceService,
                    analyticsService,
                    reportService
            );
            frame.setVisible(true);
        });
    }
}
