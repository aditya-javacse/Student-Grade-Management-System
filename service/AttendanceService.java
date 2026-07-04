package service;

import dao.AttendanceDAO;
import dao.StudentDAO;
import dao.NotificationDAO;
import dao.ActivityLogDAO;
import model.Attendance;
import model.Student;
import model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO;
    private final StudentDAO studentDAO;
    private final NotificationDAO notificationDAO;
    private final ActivityLogDAO logDAO;

    public AttendanceService(AttendanceDAO attendanceDAO, StudentDAO studentDAO, 
                             NotificationDAO notificationDAO, ActivityLogDAO logDAO) {
        this.attendanceDAO = attendanceDAO;
        this.studentDAO = studentDAO;
        this.notificationDAO = notificationDAO;
        this.logDAO = logDAO;
    }

    /**
     * Mark or update student attendance. Checks for low attendance warnings (< 75%).
     */
    public void saveAttendance(Attendance attendance) {
        attendanceDAO.save(attendance);

        // Re-evaluate overall attendance percentage
        double rate = calculateAttendancePercentage(attendance.getStudentId());
        
        // Count records - only trigger if we have at least 5 classes logged (to avoid early false alerts)
        List<Attendance> records = attendanceDAO.getByStudentId(attendance.getStudentId());
        if (records.size() >= 5 && rate < 75.0) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String notId = notificationDAO.generateNextId();
            String title = "Low Attendance Warning";
            String message = String.format("Your attendance rate is %.2f%%. A minimum of 75%% is required to qualify for exams.", rate);

            // Avoid duplicating notifications
            boolean exists = false;
            for (Notification n : notificationDAO.getByStudentId(attendance.getStudentId())) {
                if (n.getTitle().equalsIgnoreCase(title) && !n.isRead()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                notificationDAO.save(new Notification(notId, attendance.getStudentId(), title, message, dateStr, false));
            }
        }
    }

    public List<Attendance> getAttendanceForStudent(String studentId) {
        return attendanceDAO.getByStudentId(studentId);
    }

    /**
     * Calculates the attendance rate.
     * PRESENT and LATE are counted as attended, ABSENT is not.
     */
    public double calculateAttendancePercentage(String studentId) {
        List<Attendance> records = attendanceDAO.getByStudentId(studentId);
        if (records.isEmpty()) return 100.0; // Default to 100 if no records yet

        long attended = records.stream()
                .filter(r -> r.getStatus().equalsIgnoreCase("PRESENT") || r.getStatus().equalsIgnoreCase("LATE"))
                .count();

        return ((double) attended / records.size()) * 100.0;
    }

    /**
     * Generates a roster for marking attendance for a class on a specific date.
     * If records exist in the database, it loads them, otherwise returns empty slots.
     */
    public List<Attendance> getRosterForClassAndDate(String className, String date) {
        List<Student> students = studentDAO.getAll().stream()
                .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                .collect(Collectors.toList());

        List<Attendance> roster = new ArrayList<>();
        List<Attendance> allAttendance = attendanceDAO.getAll();

        for (Student s : students) {
            // Find existing
            Attendance match = null;
            for (Attendance a : allAttendance) {
                if (a.getStudentId().equalsIgnoreCase(s.getStudentId()) && a.getDate().equalsIgnoreCase(date)) {
                    match = a;
                    break;
                }
            }
            if (match == null) {
                match = new Attendance(s.getStudentId(), date, ""); // Unmarked
            }
            roster.add(match);
        }
        return roster;
    }

    /**
     * Quick save of a whole class attendance sheet
     */
    public void saveRoster(List<Attendance> roster) {
        for (Attendance a : roster) {
            if (a.getStatus() != null && !a.getStatus().isEmpty()) {
                saveAttendance(a);
            }
        }
        logDAO.log(getActorUsername(), getActorRole(), "Marked attendance sheet for " + roster.size() + " slots");
    }

    private String getActorUsername() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUsername() : "SYSTEM";
    }

    private String getActorRole() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getRole() : "SYSTEM";
    }
}
