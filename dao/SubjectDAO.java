package dao;

import model.Subject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubjectDAO {
    private static final String FILE_NAME = "subjects.csv";
    private static final List<String> HEADER = Arrays.asList("subjectId", "name", "code", "creditHours");

    private static final String ENROLLMENT_FILE = "student_subjects.csv";
    private static final List<String> ENROLLMENT_HEADER = Arrays.asList("studentId", "subjectId");

    public SubjectDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
        if (!CSVDatabase.fileExists(ENROLLMENT_FILE)) {
            CSVDatabase.writeTable(ENROLLMENT_FILE, ENROLLMENT_HEADER, new ArrayList<>());
        }
    }

    public List<Subject> getAll() {
        List<Subject> subjects = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 4) continue;
            try {
                subjects.add(new Subject(
                        row.get(0),
                        row.get(1),
                        row.get(2),
                        Integer.parseInt(row.get(3))
                ));
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        return subjects;
    }

    public Subject getById(String subjectId) {
        if (subjectId == null) return null;
        for (Subject s : getAll()) {
            if (s.getSubjectId().equalsIgnoreCase(subjectId)) {
                return s;
            }
        }
        return null;
    }

    public Subject getByCode(String code) {
        if (code == null) return null;
        for (Subject s : getAll()) {
            if (s.getCode().equalsIgnoreCase(code)) {
                return s;
            }
        }
        return null;
    }

    public void save(Subject subject) {
        List<Subject> subjects = getAll();
        boolean exists = false;
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).getSubjectId().equalsIgnoreCase(subject.getSubjectId())) {
                subjects.set(i, subject);
                exists = true;
                break;
            }
        }
        if (!exists) {
            subjects.add(subject);
        }
        saveAll(subjects);
    }

    public void delete(String subjectId) {
        List<Subject> subjects = getAll();
        subjects.removeIf(s -> s.getSubjectId().equalsIgnoreCase(subjectId));
        saveAll(subjects);
        unassignAllForSubject(subjectId);
    }

    public String generateNextId() {
        List<Subject> subjects = getAll();
        int maxId = 100;
        for (Subject s : subjects) {
            String idStr = s.getSubjectId();
            if (idStr != null && idStr.startsWith("SUB")) {
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
        return "SUB" + (maxId + 1);
    }

    private void saveAll(List<Subject> subjects) {
        List<List<String>> rows = new ArrayList<>();
        for (Subject s : subjects) {
            rows.add(Arrays.asList(
                    s.getSubjectId(),
                    s.getName(),
                    s.getCode(),
                    String.valueOf(s.getCreditHours())
            ));
        }
        CSVDatabase.writeTable(FILE_NAME, HEADER, rows);
    }

    // --- ENROLLMENTS / ASSIGNMENTS (Many-to-Many) ---

    public List<String> getAssignedSubjects(String studentId) {
        List<String> subjectIds = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(ENROLLMENT_FILE);
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) continue;
            if (row.get(0).equalsIgnoreCase(studentId)) {
                subjectIds.add(row.get(1));
            }
        }
        return subjectIds;
    }

    public List<String> getStudentsInSubject(String subjectId) {
        List<String> studentIds = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(ENROLLMENT_FILE);
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) continue;
            if (row.get(1).equalsIgnoreCase(subjectId)) {
                studentIds.add(row.get(0));
            }
        }
        return studentIds;
    }

    public void assignSubjectToStudent(String studentId, String subjectId) {
        List<List<String>> rows = CSVDatabase.readTable(ENROLLMENT_FILE);
        boolean exists = false;
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) continue;
            if (row.get(0).equalsIgnoreCase(studentId) && row.get(1).equalsIgnoreCase(subjectId)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            CSVDatabase.appendRow(ENROLLMENT_FILE, Arrays.asList(studentId, subjectId));
        }
    }

    public void unassignSubjectFromStudent(String studentId, String subjectId) {
        List<List<String>> rows = CSVDatabase.readTable(ENROLLMENT_FILE);
        List<List<String>> newRows = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) continue;
            if (row.get(0).equalsIgnoreCase(studentId) && row.get(1).equalsIgnoreCase(subjectId)) {
                continue; // skip
            }
            newRows.add(row);
        }
        CSVDatabase.writeTable(ENROLLMENT_FILE, ENROLLMENT_HEADER, newRows);
    }

    public void unassignAllForStudent(String studentId) {
        List<List<String>> rows = CSVDatabase.readTable(ENROLLMENT_FILE);
        List<List<String>> newRows = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) continue;
            if (row.get(0).equalsIgnoreCase(studentId)) {
                continue; // skip
            }
            newRows.add(row);
        }
        CSVDatabase.writeTable(ENROLLMENT_FILE, ENROLLMENT_HEADER, newRows);
    }

    public void unassignAllForSubject(String subjectId) {
        List<List<String>> rows = CSVDatabase.readTable(ENROLLMENT_FILE);
        List<List<String>> newRows = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) continue;
            if (row.get(1).equalsIgnoreCase(subjectId)) {
                continue; // skip
            }
            newRows.add(row);
        }
        CSVDatabase.writeTable(ENROLLMENT_FILE, ENROLLMENT_HEADER, newRows);
    }
}
