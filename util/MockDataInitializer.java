package util;

import dao.*;
import model.*;
import service.AuthService;

import java.text.SimpleDateFormat;
import java.util.*;

public class MockDataInitializer {

    public static void initialize() {
        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        AssessmentDAO assessmentDAO = new AssessmentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        ActivityLogDAO logDAO = new ActivityLogDAO();

        // 1. Check if database is already seeded (Check if we have students)
        if (!studentDAO.getAll().isEmpty()) {
            return; // Already initialized
        }

        logDAO.log("SYSTEM", "SYSTEM", "Initializing mock database seeding...");

        // 2. Add System Users (Credentials: admin/admin123, teacher/teacher123)
        userDAO.save(new User("admin", AuthService.hashPassword("admin123"), "ADMIN", "Reset Keyword?", "masterkey", ""));
        userDAO.save(new User("teacher", AuthService.hashPassword("teacher123"), "TEACHER", "Favorite subject?", "grading", ""));

        // 3. Register Subjects
        Subject math = new Subject("SUB101", "Mathematics", "MATH-101", 4);
        Subject phys = new Subject("SUB102", "Physics", "PHYS-102", 4);
        Subject engl = new Subject("SUB103", "English Lit.", "ENGL-103", 3);
        Subject hist = new Subject("SUB104", "World History", "HIST-104", 3);
        Subject comp = new Subject("SUB105", "Computer Sci.", "COMP-105", 4);

        subjectDAO.save(math);
        subjectDAO.save(phys);
        subjectDAO.save(engl);
        subjectDAO.save(hist);
        subjectDAO.save(comp);

        // 4. Register 15 Students across classes 10, 11, 12
        String[][] studentData = {
                {"John Doe", "1001", "Class 10", "A", "john.doe@email.com", "555-0101", "2010-05-14", "123 Maple St, Springfield"},
                {"Jane Smith", "1002", "Class 10", "A", "jane.smith@email.com", "555-0102", "2010-08-22", "456 Oak Rd, Shelbyville"},
                {"Robert Johnson", "1003", "Class 10", "B", "robert.j@email.com", "555-0103", "2010-03-09", "789 Pine Ave, Capital City"},
                {"Emily Davis", "1004", "Class 10", "B", "emily.d@email.com", "555-0104", "2010-11-30", "321 Cedar Dr, Springfield"},
                {"Michael Miller", "1005", "Class 11", "A", "michael.m@email.com", "555-0105", "2009-02-17", "654 Birch Ln, Ogdenville"},
                {"Sarah Wilson", "1006", "Class 11", "A", "sarah.w@email.com", "555-0106", "2009-07-25", "987 Elm St, Waverly Hills"},
                {"William Taylor", "1007", "Class 11", "B", "william.t@email.com", "555-0107", "2009-12-04", "159 Walnut Ct, Springfield"},
                {"Olivia Anderson", "1008", "Class 11", "B", "olivia.a@email.com", "555-0108", "2009-09-19", "753 Cherry Blvd, North Haverbrook"},
                {"James Thomas", "1009", "Class 12", "A", "james.t@email.com", "555-0109", "2008-04-11", "258 Plum Way, Cypress Creek"},
                {"Sophia Jackson", "1010", "Class 12", "A", "sophia.j@email.com", "555-0110", "2008-06-29", "369 Peach St, Springfield"},
                {"Daniel White", "1011", "Class 12", "B", "daniel.w@email.com", "555-0111", "2008-01-15", "147 Apple Ave, Brockway"},
                {"Isabella Harris", "1012", "Class 12", "B", "isabella.h@email.com", "555-0112", "2008-10-05", "258 Pear Dr, Shelbyville"},
                {"Lucas Martin", "1013", "Class 12", "C", "lucas.m@email.com", "555-0113", "2008-05-23", "951 Grape Rd, Capital City"},
                {"Mia Garcia", "1014", "Class 12", "C", "mia.g@email.com", "555-0114", "2008-07-02", "357 Melon Ln, Ogdenville"},
                {"Ethan Clark", "1015", "Class 10", "C", "ethan.c@email.com", "555-0115", "2010-02-28", "852 Berry St, Waverly Hills"}
        };

        Random rand = new Random(42); // Seed for reproducible mock grades
        List<Student> students = new ArrayList<>();

        for (int i = 0; i < studentData.length; i++) {
            String stuId = "STU" + (1001 + i);
            String[] data = studentData[i];
            
            Student stu = new Student(stuId, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            studentDAO.save(stu);
            students.add(stu);

            // Auto-provision Student Portal login (e.g. stu1001 / student123)
            userDAO.save(new User(
                    stuId.toLowerCase(),
                    AuthService.hashPassword("student123"),
                    "STUDENT",
                    "Security validation?",
                    stuId,
                    stuId
            ));

            // Enroll in 3-4 subjects depending on class
            subjectDAO.assignSubjectToStudent(stuId, math.getSubjectId());
            subjectDAO.assignSubjectToStudent(stuId, engl.getSubjectId());
            subjectDAO.assignSubjectToStudent(stuId, comp.getSubjectId());

            if (i % 2 == 0) {
                subjectDAO.assignSubjectToStudent(stuId, phys.getSubjectId());
            } else {
                subjectDAO.assignSubjectToStudent(stuId, hist.getSubjectId());
            }
        }

        // 5. Seed assessments for all enrollments
        List<Subject> subjects = Arrays.asList(math, phys, engl, hist, comp);
        for (Student stu : students) {
            List<String> assignedSubIds = subjectDAO.getAssignedSubjects(stu.getStudentId());
            for (String subId : assignedSubIds) {
                // Generate realistic marks: A grade students, B grade students, and a couple struggling
                double baseGrade;
                String name = stu.getName();
                
                if (name.equals("Jane Smith") || name.equals("Sophia Jackson") || name.equals("William Taylor")) {
                    baseGrade = 88.0 + rand.nextDouble() * 10.0; // High achievers (88-98)
                } else if (name.equals("Robert Johnson") || name.equals("Mia Garcia")) {
                    baseGrade = 42.0 + rand.nextDouble() * 15.0; // Struggling students (42-57)
                } else {
                    baseGrade = 62.0 + rand.nextDouble() * 23.0; // Average (62-85)
                }

                double assign = clamp(baseGrade + rand.nextDouble() * 6 - 3);
                double quiz = clamp(baseGrade + rand.nextDouble() * 8 - 4);
                double midterm = clamp(baseGrade + rand.nextDouble() * 10 - 5);
                double finExam = clamp(baseGrade + rand.nextDouble() * 12 - 6);
                double internal = clamp(baseGrade + rand.nextDouble() * 4 - 2);

                Assessment a = new Assessment(stu.getStudentId(), subId, assign, quiz, midterm, finExam, internal);
                assessmentDAO.save(a);
            }
        }

        // 6. Seed Attendance logs for last 10 days
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (int day = 0; day < 10; day++) {
            String dateStr = sdf.format(cal.getTime());
            
            for (Student stu : students) {
                String status = "PRESENT";
                
                // Specific attendance modifiers
                if (stu.getName().equals("Emily Davis")) {
                    // Extremely low attendance (approx 50% present)
                    status = rand.nextDouble() < 0.5 ? "PRESENT" : "ABSENT";
                } else if (stu.getName().equals("Michael Miller")) {
                    // Borderline attendance (approx 70% present)
                    status = rand.nextDouble() < 0.7 ? "PRESENT" : (rand.nextDouble() < 0.3 ? "LATE" : "ABSENT");
                } else {
                    // Good student attendance (90% present)
                    status = rand.nextDouble() < 0.9 ? "PRESENT" : (rand.nextDouble() < 0.6 ? "LATE" : "ABSENT");
                }

                attendanceDAO.save(new Attendance(stu.getStudentId(), dateStr, status));
            }
            cal.add(Calendar.DAY_OF_YEAR, -1); // Go back one day
        }

        // 7. Seed Notifications
        String today = sdf.format(new Date());
        notificationDAO.save(new Notification("NOT1001", "STU1001", "Welcome to Portal", "Welcome to your Student Performance Suite. Check your AI Recommendations daily.", today, false));
        
        // Low performance warning for Robert Johnson (STU1003)
        notificationDAO.save(new Notification("NOT1002", "STU1003", "Performance Notice", "Low marks recorded in Mathematics. Click AI recommendations tab for guidance.", today, false));
        
        // Low attendance warning for Emily Davis (STU1004)
        notificationDAO.save(new Notification("NOT1003", "STU1004", "Attendance Warning", "Your attendance is 50.00%. A minimum of 75% is required for exams.", today, false));

        logDAO.log("SYSTEM", "SYSTEM", "Mock database successfully seeded with initial mock sets");
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
