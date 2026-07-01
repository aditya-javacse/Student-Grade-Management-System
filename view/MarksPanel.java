package view;

import service.AssessmentService;
import service.StudentService;
import service.SubjectService;
import model.Student;
import model.Subject;
import model.Assessment;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MarksPanel extends JPanel {
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final AssessmentService assessmentService;

    private JComboBox<String> cbClass;
    private JComboBox<Subject> cbSubject;
    private JTable tblGrades;
    private DefaultTableModel modelGrades;

    // Entry Form Fields
    private UIComponents.CustomTextField txtAssignment;
    private UIComponents.CustomTextField txtQuiz;
    private UIComponents.CustomTextField txtMidterm;
    private UIComponents.CustomTextField txtFinal;
    private UIComponents.CustomTextField txtInternal;
    private UIComponents.CustomButton btnSave;
    
    private JLabel lblSelectedStudentName;
    private Student currentSelectedStudent;

    public MarksPanel(StudentService studentService, SubjectService subjectService, AssessmentService assessmentService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadSelectors();
    }

    private void initComponents() {
        // --- 1. Selection Header ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setOpaque(false);

        JLabel lblClass = new JLabel("Class:");
        lblClass.setFont(UIComponents.FONT_BOLD);
        lblClass.setForeground(UIComponents.COLOR_TEXT_SEC);
        topPanel.add(lblClass);

        cbClass = new JComboBox<>(new String[]{"Class 10", "Class 11", "Class 12"});
        cbClass.setFont(UIComponents.FONT_BODY);
        cbClass.setBackground(UIComponents.COLOR_CARD);
        cbClass.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbClass.addActionListener(this::handleFilterChanged);
        topPanel.add(cbClass);

        JLabel lblSubject = new JLabel("Subject:");
        lblSubject.setFont(UIComponents.FONT_BOLD);
        lblSubject.setForeground(UIComponents.COLOR_TEXT_SEC);
        topPanel.add(lblSubject);

        cbSubject = new JComboBox<>();
        cbSubject.setFont(UIComponents.FONT_BODY);
        cbSubject.setBackground(UIComponents.COLOR_CARD);
        cbSubject.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbSubject.addActionListener(this::handleFilterChanged);
        topPanel.add(cbSubject);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Table of Student Grades ---
        String[] cols = {"Student ID", "Name", "Assignment", "Quiz", "Mid-Term", "Final", "Internal", "Total", "Grade", "GPA"};
        modelGrades = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblGrades = new JTable(modelGrades);
        UIComponents.styleTable(tblGrades);
        tblGrades.getSelectionModel().addListSelectionListener(e -> handleStudentSelected());

        JScrollPane scroll = new JScrollPane(tblGrades);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);

        JPanel tableCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // --- 3. Bottom Form: Marks Editor ---
        JPanel bottomCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        bottomCard.setLayout(new BorderLayout());
        bottomCard.setBorder(new EmptyBorder(15, 20, 15, 20));
        bottomCard.setPreferredSize(new Dimension(getWidth(), 150));

        lblSelectedStudentName = new JLabel("Select a student from the table above to enter marks");
        lblSelectedStudentName.setFont(UIComponents.FONT_BOLD);
        lblSelectedStudentName.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblSelectedStudentName.setBorder(new EmptyBorder(0, 0, 10, 0));
        bottomCard.add(lblSelectedStudentName, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        formFields.setOpaque(false);

        txtAssignment = new UIComponents.CustomTextField("Assign (15%)");
        txtAssignment.setPreferredSize(new Dimension(100, 36));
        txtAssignment.setEnabled(false);
        formFields.add(new JLabel("<html><span style='color:#94a3b8; font-size:10px;'>ASSIGNMENT</span></html>"));
        formFields.add(txtAssignment);

        txtQuiz = new UIComponents.CustomTextField("Quiz (15%)");
        txtQuiz.setPreferredSize(new Dimension(100, 36));
        txtQuiz.setEnabled(false);
        formFields.add(new JLabel("<html><span style='color:#94a3b8; font-size:10px;'>QUIZ</span></html>"));
        formFields.add(txtQuiz);

        txtMidterm = new UIComponents.CustomTextField("Mid (30%)");
        txtMidterm.setPreferredSize(new Dimension(100, 36));
        txtMidterm.setEnabled(false);
        formFields.add(new JLabel("<html><span style='color:#94a3b8; font-size:10px;'>MIDTERM</span></html>"));
        formFields.add(txtMidterm);

        txtFinal = new UIComponents.CustomTextField("Final (30%)");
        txtFinal.setPreferredSize(new Dimension(100, 36));
        txtFinal.setEnabled(false);
        formFields.add(new JLabel("<html><span style='color:#94a3b8; font-size:10px;'>FINAL</span></html>"));
        formFields.add(txtFinal);

        txtInternal = new UIComponents.CustomTextField("Internal (10%)");
        txtInternal.setPreferredSize(new Dimension(100, 36));
        txtInternal.setEnabled(false);
        formFields.add(new JLabel("<html><span style='color:#94a3b8; font-size:10px;'>INTERNAL</span></html>"));
        formFields.add(txtInternal);

        btnSave = new UIComponents.CustomButton("Save Grade", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnSave.setEnabled(false);
        btnSave.addActionListener(this::handleSaveGrades);
        formFields.add(btnSave);

        bottomCard.add(formFields, BorderLayout.CENTER);
        add(bottomCard, BorderLayout.SOUTH);
    }

    private void loadSelectors() {
        cbSubject.removeAllItems();
        for (Subject s : subjectService.getAllSubjects()) {
            cbSubject.addItem(s);
        }
        loadGradesTable();
    }

    private void loadGradesTable() {
        modelGrades.setRowCount(0);
        
        String className = (String) cbClass.getSelectedItem();
        Subject sub = (Subject) cbSubject.getSelectedItem();
        if (className == null || sub == null) return;

        // Get all students enrolled in this subject who belong to this class
        List<String> studentIds = new dao.SubjectDAO().getStudentsInSubject(sub.getSubjectId());
        
        for (String id : studentIds) {
            Student stu = studentService.getStudentById(id);
            if (stu != null && stu.getStudentClass().equalsIgnoreCase(className)) {
                Assessment a = assessmentService.getAssessment(id, sub.getSubjectId());
                if (a != null) {
                    modelGrades.addRow(new Object[]{
                            stu.getStudentId(),
                            stu.getName(),
                            a.getAssignmentMarks(),
                            a.getQuizMarks(),
                            a.getMidTermMarks(),
                            a.getFinalExamMarks(),
                            a.getInternalAssessment(),
                            String.format(java.util.Locale.US, "%.1f", a.getTotalMarks()),
                            a.getGrade(),
                            String.format(java.util.Locale.US, "%.2f", a.getGpa())
                    });
                } else {
                    modelGrades.addRow(new Object[]{
                            stu.getStudentId(),
                            stu.getName(),
                            "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A"
                    });
                }
            }
        }
        resetForm();
    }

    private void handleStudentSelected() {
        int row = tblGrades.getSelectedRow();
        if (row == -1) {
            resetForm();
            return;
        }

        String stuId = (String) tblGrades.getValueAt(row, 0);
        Student stu = studentService.getStudentById(stuId);
        if (stu == null) return;

        currentSelectedStudent = stu;
        lblSelectedStudentName.setText("Entering grades for: " + stu.getName() + " (" + stuId + ")");

        // Load existing values if they exist
        Subject sub = (Subject) cbSubject.getSelectedItem();
        if (sub == null) return;

        Assessment a = assessmentService.getAssessment(stuId, sub.getSubjectId());
        if (a != null) {
            txtAssignment.setText(String.valueOf(a.getAssignmentMarks()));
            txtQuiz.setText(String.valueOf(a.getQuizMarks()));
            txtMidterm.setText(String.valueOf(a.getMidTermMarks()));
            txtFinal.setText(String.valueOf(a.getFinalExamMarks()));
            txtInternal.setText(String.valueOf(a.getInternalAssessment()));
        } else {
            txtAssignment.setText("");
            txtQuiz.setText("");
            txtMidterm.setText("");
            txtFinal.setText("");
            txtInternal.setText("");
        }

        txtAssignment.setEnabled(true);
        txtQuiz.setEnabled(true);
        txtMidterm.setEnabled(true);
        txtFinal.setEnabled(true);
        txtInternal.setEnabled(true);
        btnSave.setEnabled(true);
    }

    private void resetForm() {
        currentSelectedStudent = null;
        lblSelectedStudentName.setText("Select a student from the table above to enter marks");
        txtAssignment.setText("");
        txtQuiz.setText("");
        txtMidterm.setText("");
        txtFinal.setText("");
        txtInternal.setText("");
        txtAssignment.setEnabled(false);
        txtQuiz.setEnabled(false);
        txtMidterm.setEnabled(false);
        txtFinal.setEnabled(false);
        txtInternal.setEnabled(false);
        btnSave.setEnabled(false);
    }

    private void handleFilterChanged(ActionEvent e) {
        loadGradesTable();
    }

    private void handleSaveGrades(ActionEvent e) {
        if (currentSelectedStudent == null) return;
        Subject sub = (Subject) cbSubject.getSelectedItem();
        if (sub == null) return;

        try {
            double assign = parseDoubleField(txtAssignment, "Assignment");
            double quiz = parseDoubleField(txtQuiz, "Quiz");
            double mid = parseDoubleField(txtMidterm, "Midterm");
            double fin = parseDoubleField(txtFinal, "Final");
            double intern = parseDoubleField(txtInternal, "Internal");

            Assessment a = new Assessment(currentSelectedStudent.getStudentId(), sub.getSubjectId(), assign, quiz, mid, fin, intern);
            assessmentService.saveAssessment(a);

            JOptionPane.showMessageDialog(this, "Grades updated successfully!", "Grades Saved", JOptionPane.INFORMATION_MESSAGE);
            loadGradesTable();
        } catch (NumberFormatException ex) {
            // Error alert already handled in parseDoubleField helper
        }
    }

    private double parseDoubleField(JTextField field, String fieldName) throws NumberFormatException {
        String txt = field.getText().trim();
        if (txt.isEmpty()) {
            JOptionPane.showMessageDialog(this, fieldName + " marks cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            throw new NumberFormatException();
        }
        try {
            double val = Double.parseDouble(txt);
            if (val < 0.0 || val > 100.0) {
                JOptionPane.showMessageDialog(this, fieldName + " marks must be between 0 and 100.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                throw new NumberFormatException();
            }
            return val;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, fieldName + " marks must be a valid numeric value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            throw new NumberFormatException();
        }
    }
}
