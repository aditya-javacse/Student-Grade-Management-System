package dao;

import model.Student;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentDAO {
    private static final String FILE_NAME = "students.csv";
    private static final List<String> HEADER = Arrays.asList(
            "studentId", "name", "rollNumber", "class", "section", "email", "phoneNumber", "dateOfBirth", "address"
    );

    public StudentDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
    }

    public List<Student> getAll() {
        List<Student> students = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 9) continue;
            students.add(new Student(
                    row.get(0),
                    row.get(1),
                    row.get(2),
                    row.get(3),
                    row.get(4),
                    row.get(5),
                    row.get(6),
                    row.get(7),
                    row.get(8)
            ));
        }
        return students;
    }

    public Student getById(String studentId) {
        if (studentId == null) return null;
        for (Student s : getAll()) {
            if (s.getStudentId().equalsIgnoreCase(studentId)) {
                return s;
            }
        }
        return null;
    }

    public void save(Student student) {
        List<Student> students = getAll();
        boolean exists = false;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentId().equalsIgnoreCase(student.getStudentId())) {
                students.set(i, student);
                exists = true;
                break;
            }
        }
        if (!exists) {
            students.add(student);
        }
        saveAll(students);
    }

    public void delete(String studentId) {
        List<Student> students = getAll();
        students.removeIf(s -> s.getStudentId().equalsIgnoreCase(studentId));
        saveAll(students);
    }

    public String generateNextId() {
        List<Student> students = getAll();
        int maxId = 1000;
        for (Student s : students) {
            String idStr = s.getStudentId();
            if (idStr != null && idStr.startsWith("STU")) {
                try {
                    int idVal = Integer.parseInt(idStr.substring(3));
                    if (idVal > maxId) {
                        maxId = idVal;
                    }
                } catch (NumberFormatException e) {
                    // Ignore malformed ID
                }
            }
        }
        return "STU" + (maxId + 1);
    }

    private void saveAll(List<Student> students) {
        List<List<String>> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(Arrays.asList(
                    s.getStudentId(),
                    s.getName(),
                    s.getRollNumber(),
                    s.getStudentClass(),
                    s.getSection(),
                    s.getEmail(),
                    s.getPhoneNumber(),
                    s.getDateOfBirth(),
                    s.getAddress()
            ));
        }
        CSVDatabase.writeTable(FILE_NAME, HEADER, rows);
    }
}
