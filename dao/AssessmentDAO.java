package dao;

import model.Assessment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssessmentDAO {
    private static final String FILE_NAME = "assessments.csv";
    private static final List<String> HEADER = Arrays.asList(
            "studentId", "subjectId", "assignmentMarks", "quizMarks", "midTermMarks", "finalExamMarks", "internalAssessment"
    );

    public AssessmentDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
    }

    public List<Assessment> getAll() {
        List<Assessment> assessments = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 7) continue;
            try {
                assessments.add(new Assessment(
                        row.get(0),
                        row.get(1),
                        Double.parseDouble(row.get(2)),
                        Double.parseDouble(row.get(3)),
                        Double.parseDouble(row.get(4)),
                        Double.parseDouble(row.get(5)),
                        Double.parseDouble(row.get(6))
                ));
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        return assessments;
    }

    public List<Assessment> getByStudentId(String studentId) {
        List<Assessment> results = new ArrayList<>();
        if (studentId == null) return results;
        for (Assessment a : getAll()) {
            if (a.getStudentId().equalsIgnoreCase(studentId)) {
                results.add(a);
            }
        }
        return results;
    }

    public Assessment getByStudentAndSubject(String studentId, String subjectId) {
        if (studentId == null || subjectId == null) return null;
        for (Assessment a : getAll()) {
            if (a.getStudentId().equalsIgnoreCase(studentId) && a.getSubjectId().equalsIgnoreCase(subjectId)) {
                return a;
            }
        }
        return null;
    }

    public void save(Assessment assessment) {
        List<Assessment> assessments = getAll();
        boolean exists = false;
        for (int i = 0; i < assessments.size(); i++) {
            Assessment a = assessments.get(i);
            if (a.getStudentId().equalsIgnoreCase(assessment.getStudentId()) && 
                a.getSubjectId().equalsIgnoreCase(assessment.getSubjectId())) {
                assessments.set(i, assessment);
                exists = true;
                break;
            }
        }
        if (!exists) {
            assessments.add(assessment);
        }
        saveAll(assessments);
    }

    public void delete(String studentId, String subjectId) {
        List<Assessment> assessments = getAll();
        assessments.removeIf(a -> a.getStudentId().equalsIgnoreCase(studentId) && a.getSubjectId().equalsIgnoreCase(subjectId));
        saveAll(assessments);
    }

    public void deleteAllForStudent(String studentId) {
        List<Assessment> assessments = getAll();
        assessments.removeIf(a -> a.getStudentId().equalsIgnoreCase(studentId));
        saveAll(assessments);
    }

    public void deleteAllForSubject(String subjectId) {
        List<Assessment> assessments = getAll();
        assessments.removeIf(a -> a.getSubjectId().equalsIgnoreCase(subjectId));
        saveAll(assessments);
    }

    private void saveAll(List<Assessment> assessments) {
        List<List<String>> rows = new ArrayList<>();
        for (Assessment a : assessments) {
            rows.add(Arrays.asList(
                    a.getStudentId(),
                    a.getSubjectId(),
                    String.valueOf(a.getAssignmentMarks()),
                    String.valueOf(a.getQuizMarks()),
                    String.valueOf(a.getMidTermMarks()),
                    String.valueOf(a.getFinalExamMarks()),
                    String.valueOf(a.getInternalAssessment())
            ));
        }
        CSVDatabase.writeTable(FILE_NAME, HEADER, rows);
    }
}
