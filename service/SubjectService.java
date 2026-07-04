package service;

import dao.SubjectDAO;
import dao.AssessmentDAO;
import dao.ActivityLogDAO;
import model.Subject;
import java.util.ArrayList;
import java.util.List;

public class SubjectService {
    private final SubjectDAO subjectDAO;
    private final AssessmentDAO assessmentDAO;
    private final ActivityLogDAO logDAO;

    public SubjectService(SubjectDAO subjectDAO, AssessmentDAO assessmentDAO, ActivityLogDAO logDAO) {
        this.subjectDAO = subjectDAO;
        this.assessmentDAO = assessmentDAO;
        this.logDAO = logDAO;
    }

    public Subject addSubject(String name, String code, int creditHours) {
        String subjectId = subjectDAO.generateNextId();
        Subject subject = new Subject(subjectId, name, code, creditHours);
        subjectDAO.save(subject);
        logDAO.log(getActorUsername(), getActorRole(), "Added subject: " + name + " (" + code + ")");
        return subject;
    }

    public void updateSubject(Subject subject) {
        subjectDAO.save(subject);
        logDAO.log(getActorUsername(), getActorRole(), "Updated subject: " + subject.getName() + " (" + subject.getCode() + ")");
    }

    public void deleteSubject(String subjectId) {
        Subject s = subjectDAO.getById(subjectId);
        if (s == null) return;
        
        subjectDAO.delete(subjectId);
        // Cascade delete assessments for this subject
        assessmentDAO.deleteAllForSubject(subjectId);
        
        logDAO.log(getActorUsername(), getActorRole(), "Deleted subject: " + s.getName() + " (" + s.getCode() + ") and deleted associated grades");
    }

    public List<Subject> getAllSubjects() {
        return subjectDAO.getAll();
    }

    public Subject getSubjectById(String subjectId) {
        return subjectDAO.getById(subjectId);
    }

    public List<Subject> getSubjectsForStudent(String studentId) {
        List<String> ids = subjectDAO.getAssignedSubjects(studentId);
        List<Subject> list = new ArrayList<>();
        for (String id : ids) {
            Subject s = subjectDAO.getById(id);
            if (s != null) {
                list.add(s);
            }
        }
        return list;
    }

    public void assignSubjectToStudent(String studentId, String subjectId) {
        subjectDAO.assignSubjectToStudent(studentId, subjectId);
        logDAO.log(getActorUsername(), getActorRole(), "Assigned subject ID " + subjectId + " to student ID " + studentId);
    }

    public void unassignSubjectFromStudent(String studentId, String subjectId) {
        subjectDAO.unassignSubjectFromStudent(studentId, subjectId);
        // Also remove any marks entered for this combination to prevent dead entries
        assessmentDAO.delete(studentId, subjectId);
        logDAO.log(getActorUsername(), getActorRole(), "Unassigned subject ID " + subjectId + " from student ID " + studentId);
    }

    private String getActorUsername() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUsername() : "SYSTEM";
    }

    private String getActorRole() {
        return AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getRole() : "SYSTEM";
    }
}
