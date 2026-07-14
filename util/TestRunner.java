package util;

import dao.*;
import model.*;
import service.*;

import java.io.File;
import java.util.List;

public class TestRunner {
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("       AI Student Analytics - Automated Test Suite");
        System.out.println("==========================================================");
        System.out.println();

        try {
            // Seed database first for fresh mock environments
            MockDataInitializer.initialize();

            // Run individual unit segments
            testPasswordHashing();
            testUserAuthentication();
            testStudentCascadingOperations();
            testGradeCalculations();
            testAttendancePercentage();
            testAIRecommendations();

            System.out.println();
            System.out.println("==========================================================");
            System.out.printf("   TEST RUN COMPLETE: %d / %d ASSERTS PASSED (%s)\n",
                    passedTests, totalTests, passedTests == totalTests ? "SUCCESS" : "FAILURES DETECTED");
            System.out.println("==========================================================");
            
            if (passedTests != totalTests) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("\n[FATAL ERROR] An unexpected exception crashed the test runner:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertEqual(Object expected, Object actual, String testName) {
        totalTests++;
        if (expected == null && actual == null) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
            return;
        }
        if (expected != null && expected.equals(actual)) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.out.printf("  [FAIL] %s - Expected: [%s], Actual: [%s]\n", testName, expected, actual);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.out.printf("  [FAIL] %s - Condition was FALSE\n", testName);
        }
    }

    private static void testPasswordHashing() {
        System.out.println("\n[1] Testing Hashing Functions...");
        String text = "admin123";
        String hash1 = AuthService.hashPassword(text);
        String hash2 = AuthService.hashPassword(text);
        
        assertEqual(hash1, hash2, "Hashing same password yields identical SHA-256 signatures");
        assertTrue(hash1 != null && hash1.length() == 64, "SHA-256 returns exactly 64-char hexadecimal string");
    }

    private static void testUserAuthentication() {
        System.out.println("\n[2] Testing Authentication & Password Reset...");
        UserDAO userDAO = new UserDAO();
        ActivityLogDAO logDAO = new ActivityLogDAO();
        AuthService auth = new AuthService(userDAO, logDAO);

        // Login Admin
        boolean adminLogin = auth.login("admin", "admin123");
        assertTrue(adminLogin, "Logged in admin account with correct password hash");
        assertEqual("ADMIN", AuthService.getCurrentUser().getRole(), "Log in session maps correct ADMIN role");

        // Login fail check
        boolean failedLogin = auth.login("admin", "wrongpassword");
        assertTrue(!failedLogin, "Rejected login attempt with invalid credentials");

        // Recover reset check
        boolean resetSuccess = auth.resetPassword("admin", "masterkey", "newadminpass");
        assertTrue(resetSuccess, "Successfully reset credentials using matching security recovery key");

        // Verify login with new credentials
        boolean loginWithNew = auth.login("admin", "newadminpass");
        assertTrue(loginWithNew, "Admin logged in successfully using new recovered password");

        // Restore original admin pass for subsequent usage
        auth.resetPassword("admin", "masterkey", "admin123");
        auth.logout();
    }

    private static void testStudentCascadingOperations() {
        System.out.println("\n[3] Testing Student Provisioning & Cascade Delete...");
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();
        AssessmentDAO assessmentDAO = new AssessmentDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        ActivityLogDAO logDAO = new ActivityLogDAO();

        StudentService service = new StudentService(studentDAO, userDAO, assessmentDAO, subjectDAO, attendanceDAO, notificationDAO, logDAO);

        // Save a test student
        Student temp = service.addStudent("Test Student", "9999", "Class 10", "A", "test@test.com", "555-9999", "2010-01-01", "Test St");
        String id = temp.getStudentId();
        
        assertTrue(id.startsWith("STU"), "Student ID is sequentially auto-generated: " + id);
        assertEqual("Test Student", studentDAO.getById(id).getName(), "Retrieved registered student successfully");

        // Verify user auto-provisioning
        User provisioned = userDAO.getByUsername(id.toLowerCase());
        assertTrue(provisioned != null, "provisioned user account is present in database");
        assertEqual("STUDENT", provisioned.getRole(), "Provisioned user role is STUDENT");

        // Add some sample data to check cascade wipeout
        subjectDAO.assignSubjectToStudent(id, "SUB101");
        assessmentDAO.save(new Assessment(id, "SUB101", 90.0, 90.0, 90.0, 90.0, 90.0));
        attendanceDAO.save(new Attendance(id, "2026-06-01", "PRESENT"));
        notificationDAO.save(new Notification("NOT999", id, "Test", "Test", "2026-06-01", false));

        // Perform cascade delete
        service.deleteStudent(id);

        // Verify no orphan records exist
        assertEqual(null, studentDAO.getById(id), "Cascading delete cleared student profile data");
        assertEqual(null, userDAO.getByUsername(id.toLowerCase()), "Cascading delete cleared linked user profile credentials");
        assertTrue(assessmentDAO.getByStudentId(id).isEmpty(), "Cascading delete cleared all grade entries");
        assertTrue(subjectDAO.getAssignedSubjects(id).isEmpty(), "Cascading delete cleared subject enrollments");
        assertTrue(attendanceDAO.getByStudentId(id).isEmpty(), "Cascading delete cleared attendance files");
        assertTrue(notificationDAO.getByStudentId(id).isEmpty(), "Cascading delete cleared alerts");
    }

    private static void testGradeCalculations() {
        System.out.println("\n[4] Testing Weighted Assessment Formulas...");
        // Formula: Assign 15%, Quiz 15%, Mid 30%, Final 30%, Internal 10%
        // Case: All 80
        Assessment a = new Assessment("STU1001", "SUB101", 80.0, 80.0, 80.0, 80.0, 80.0);
        assertEqual(80.0, a.getTotalMarks(), "Consistent scores yield correct totals (80%)");
        assertEqual("A", a.getGrade(), "Total score of 80% maps to A grade");
        assertEqual(3.7, a.getGpa(), "Total score of 80% yields 3.7 GPA");

        // Case: Variable scores: Assign 90, Quiz 80, Mid 70, Final 85, Internal 100
        // Expected: 90*0.15 + 80*0.15 + 70*0.3 + 85*0.3 + 100*0.1 = 13.5 + 12 + 21 + 25.5 + 10 = 82.0
        Assessment a2 = new Assessment("STU1001", "SUB101", 90.0, 80.0, 70.0, 85.0, 100.0);
        assertEqual(82.0, a2.getTotalMarks(), "Variable marks evaluate correctly according to weights (82.0%)");
        assertEqual("A", a2.getGrade(), "Weighted total of 82.0% maps to A grade");

        // Failing case
        Assessment a3 = new Assessment("STU1001", "SUB101", 40.0, 40.0, 40.0, 40.0, 40.0);
        assertEqual("F", a3.getGrade(), "Weighted total below 50.0% yields F grade");
        assertEqual(0.0, a3.getGpa(), "F grade maps to 0.0 GPA");
    }

    private static void testAttendancePercentage() {
        System.out.println("\n[5] Testing Attendance Calculations...");
        AttendanceDAO dao = new AttendanceDAO();
        StudentDAO studentDAO = new StudentDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        ActivityLogDAO logDAO = new ActivityLogDAO();
        
        AttendanceService service = new AttendanceService(dao, studentDAO, notificationDAO, logDAO);

        // Fetch dummy STU1001 records and compare calculations
        double rate = service.calculateAttendancePercentage("STU1001");
        List<Attendance> records = dao.getByStudentId("STU1001");

        long attended = records.stream()
                .filter(r -> r.getStatus().equalsIgnoreCase("PRESENT") || r.getStatus().equalsIgnoreCase("LATE"))
                .count();
        double expectedRate = ((double) attended / records.size()) * 100.0;

        assertEqual(expectedRate, rate, "Service calculates correct mathematical attendance rate");
    }

    private static void testAIRecommendations() {
        System.out.println("\n[6] Testing AI Study Recommendation Engines...");
        AssessmentDAO assessmentDAO = new AssessmentDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        
        AIService ai = new AIService(assessmentDAO, subjectDAO);

        // Force a mock student STU1003 struggling grade (Robert Johnson)
        assessmentDAO.save(new Assessment("STU1003", "SUB101", 30.0, 40.0, 35.0, 45.0, 50.0)); // Total 39.5%

        List<AIService.AIRecommendation> recs = ai.generateRecommendations("STU1003");
        
        assertTrue(!recs.isEmpty(), "AI flagged grades below 85% and compiled recommendations");
        
        AIService.AIRecommendation target = null;
        for (AIService.AIRecommendation r : recs) {
            if (r.subjectCode.equalsIgnoreCase("MATH-101")) {
                target = r;
                break;
            }
        }

        assertTrue(target != null, "AI correctly diagnosed low Math grades");
        assertEqual(39.5, target.currentMarks, "AI retrieved actual math percentage");
        assertTrue(target.weakAreas.contains("Core conceptual understanding in Mathematics"), "AI diagnosed correct weaknesses");
        assertTrue(target.projectedGrade.startsWith("F"), "AI expected projected letter grade: F");
    }
}
