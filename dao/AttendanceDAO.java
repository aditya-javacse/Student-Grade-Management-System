package dao;

import model.Attendance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttendanceDAO {
    private static final String FILE_NAME = "attendance.csv";
    private static final List<String> HEADER = Arrays.asList("studentId", "date", "status");

    public AttendanceDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
    }

    public List<Attendance> getAll() {
        List<Attendance> attendances = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 3) continue;
            attendances.add(new Attendance(
                    row.get(0),
                    row.get(1),
                    row.get(2)
            ));
        }
        return attendances;
    }

    public List<Attendance> getByStudentId(String studentId) {
        List<Attendance> results = new ArrayList<>();
        if (studentId == null) return results;
        for (Attendance a : getAll()) {
            if (a.getStudentId().equalsIgnoreCase(studentId)) {
                results.add(a);
            }
        }
        return results;
    }

    public void save(Attendance attendance) {
        List<Attendance> attendances = getAll();
        boolean exists = false;
        for (int i = 0; i < attendances.size(); i++) {
            Attendance a = attendances.get(i);
            if (a.getStudentId().equalsIgnoreCase(attendance.getStudentId()) && 
                a.getDate().equalsIgnoreCase(attendance.getDate())) {
                attendances.set(i, attendance);
                exists = true;
                break;
            }
        }
        if (!exists) {
            attendances.add(attendance);
        }
        saveAll(attendances);
    }

    public void delete(String studentId, String date) {
        List<Attendance> attendances = getAll();
        attendances.removeIf(a -> a.getStudentId().equalsIgnoreCase(studentId) && a.getDate().equalsIgnoreCase(date));
        saveAll(attendances);
    }

    public void deleteAllForStudent(String studentId) {
        List<Attendance> attendances = getAll();
        attendances.removeIf(a -> a.getStudentId().equalsIgnoreCase(studentId));
        saveAll(attendances);
    }

    private void saveAll(List<Attendance> attendances) {
        List<List<String>> rows = new ArrayList<>();
        for (Attendance a : attendances) {
            rows.add(Arrays.asList(
                    a.getStudentId(),
                    a.getDate(),
                    a.getStatus()
            ));
        }
        CSVDatabase.writeTable(FILE_NAME, HEADER, rows);
    }
}
