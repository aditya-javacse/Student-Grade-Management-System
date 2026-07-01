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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardPanel extends JPanel {
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final AssessmentService assessmentService;

    private JComboBox<String> cbClass;
    private JComboBox<String> cbCategory; // "Overall Top 10", "Subject Rank"
    private JComboBox<Subject> cbSubject;
    private JLabel lblSubjectLabel;

    private JTable tblLeaderboard;
    private DefaultTableModel modelLeaderboard;

    public LeaderboardPanel(StudentService studentService, SubjectService subjectService, AssessmentService assessmentService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadCategoryControls();
        loadLeaderboard();
    }

    private void initComponents() {
        // --- 1. Top Filters Panel ---
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

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(UIComponents.FONT_BOLD);
        lblCat.setForeground(UIComponents.COLOR_TEXT_SEC);
        topPanel.add(lblCat);

        cbCategory = new JComboBox<>(new String[]{"Overall Top 10", "Subject-wise Rankings"});
        cbCategory.setFont(UIComponents.FONT_BODY);
        cbCategory.setBackground(UIComponents.COLOR_CARD);
        cbCategory.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbCategory.addActionListener(this::handleCategoryChanged);
        topPanel.add(cbCategory);

        lblSubjectLabel = new JLabel("Subject:");
        lblSubjectLabel.setFont(UIComponents.FONT_BOLD);
        lblSubjectLabel.setForeground(UIComponents.COLOR_TEXT_SEC);
        topPanel.add(lblSubjectLabel);

        cbSubject = new JComboBox<>();
        cbSubject.setFont(UIComponents.FONT_BODY);
        cbSubject.setBackground(UIComponents.COLOR_CARD);
        cbSubject.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbSubject.addActionListener(this::handleFilterChanged);
        topPanel.add(cbSubject);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Table for leaderboard list ---
        String[] cols = {"Rank", "Student ID", "Name", "Score Value", "Overall Grade", "CGPA / GPA"};
        modelLeaderboard = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLeaderboard = new JTable(modelLeaderboard);
        UIComponents.styleTable(tblLeaderboard);

        JScrollPane scroll = new JScrollPane(tblLeaderboard);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);

        JPanel tableCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadCategoryControls() {
        cbSubject.removeAllItems();
        for (Subject s : subjectService.getAllSubjects()) {
            cbSubject.addItem(s);
        }
        
        boolean showSubject = cbCategory.getSelectedIndex() == 1;
        lblSubjectLabel.setVisible(showSubject);
        cbSubject.setVisible(showSubject);
    }

    private void loadLeaderboard() {
        modelLeaderboard.setRowCount(0);
        String className = (String) cbClass.getSelectedItem();
        if (className == null) return;

        boolean isOverall = cbCategory.getSelectedIndex() == 0;

        if (isOverall) {
            // Rank by CGPA / Average Score
            List<Student> classStudents = studentService.getAllStudents().stream()
                    .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                    .collect(Collectors.toList());

            // Compute student ID to GPA mapping
            Map<Student, Double> studentGpaMap = new HashMap<>();
            for (Student s : classStudents) {
                double avg = assessmentService.getStudentAveragePercentage(s.getStudentId());
                if (avg > 0 || !assessmentService.getAssessmentsForStudent(s.getStudentId()).isEmpty()) {
                    studentGpaMap.put(s, avg);
                }
            }

            // Sort by average descending
            List<Map.Entry<Student, Double>> sorted = studentGpaMap.entrySet().stream()
                    .sorted(Map.Entry.<Student, Double>comparingByValue().reversed())
                    .limit(10) // Top 10
                    .collect(Collectors.toList());

            int rank = 1;
            for (Map.Entry<Student, Double> entry : sorted) {
                Student s = entry.getKey();
                double avg = entry.getValue();
                double cgpa = assessmentService.getStudentCGPA(s.getStudentId());

                // Find overall grade
                String grade = mapAvgToGrade(avg);
                modelLeaderboard.addRow(new Object[]{
                        "#" + rank,
                        s.getStudentId(),
                        s.getName(),
                        String.format(Locale.US, "%.1f%%", avg),
                        grade,
                        String.format(Locale.US, "%.2f", cgpa)
                });
                rank++;
            }
        } else {
            // Rank by Subject-specific marks
            Subject sub = (Subject) cbSubject.getSelectedItem();
            if (sub == null) return;

            List<Student> classStudents = studentService.getAllStudents().stream()
                    .filter(s -> s.getStudentClass().equalsIgnoreCase(className))
                    .collect(Collectors.toList());

            Map<Student, Assessment> studentGradeMap = new HashMap<>();
            for (Student s : classStudents) {
                Assessment a = assessmentService.getAssessment(s.getStudentId(), sub.getSubjectId());
                if (a != null) {
                    studentGradeMap.put(s, a);
                }
            }

            // Sort by assessment marks descending
            List<Map.Entry<Student, Assessment>> sorted = studentGradeMap.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalMarks(), e1.getValue().getTotalMarks()))
                    .collect(Collectors.toList());

            int rank = 1;
            for (Map.Entry<Student, Assessment> entry : sorted) {
                Student s = entry.getKey();
                Assessment a = entry.getValue();

                modelLeaderboard.addRow(new Object[]{
                        "#" + rank,
                        s.getStudentId(),
                        s.getName(),
                        String.format(Locale.US, "%.1f", a.getTotalMarks()),
                        a.getGrade(),
                        String.format(Locale.US, "%.2f", a.getGpa())
                });
                rank++;
            }
        }
    }

    private void handleCategoryChanged(ActionEvent e) {
        loadCategoryControls();
        loadLeaderboard();
    }

    private void handleFilterChanged(ActionEvent e) {
        loadLeaderboard();
    }

    private String mapAvgToGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }
}
