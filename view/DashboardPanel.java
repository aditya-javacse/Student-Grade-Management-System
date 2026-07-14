package view;

import service.*;
import analytics.AnalyticsService;
import util.UIComponents;
import util.AnalyticsChart;
import model.ActivityLog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final AssessmentService assessmentService;
    private final AttendanceService attendanceService;
    private final AnalyticsService analyticsService;

    // UI elements
    private JLabel lblTotalStudents;
    private JLabel lblTotalSubjects;
    private JLabel lblPassRate;
    private JLabel lblAvgGPA;
    private AnalyticsChart chartSubjectAverages;
    private AnalyticsChart chartGradeDist;
    private JTable logTable;
    private DefaultTableModel logModel;

    public DashboardPanel(StudentService studentService, SubjectService subjectService,
                          AssessmentService assessmentService, AttendanceService attendanceService,
                          AnalyticsService analyticsService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.assessmentService = assessmentService;
        this.attendanceService = attendanceService;
        this.analyticsService = analyticsService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        // --- 1. Top Metrics Banner ---
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);

        lblTotalStudents = new JLabel("0", SwingConstants.CENTER);
        metricsPanel.add(createMetricCard("TOTAL STUDENTS", lblTotalStudents, UIComponents.COLOR_ACCENT));

        lblTotalSubjects = new JLabel("0", SwingConstants.CENTER);
        metricsPanel.add(createMetricCard("TOTAL SUBJECTS", lblTotalSubjects, UIComponents.COLOR_ACCENT));

        lblPassRate = new JLabel("0.0%", SwingConstants.CENTER);
        metricsPanel.add(createMetricCard("PASS RATE", lblPassRate, UIComponents.COLOR_SUCCESS));

        lblAvgGPA = new JLabel("0.00", SwingConstants.CENTER);
        metricsPanel.add(createMetricCard("AVERAGE CGPA", lblAvgGPA, UIComponents.COLOR_ACCENT));

        add(metricsPanel, BorderLayout.NORTH);

        // --- 2. Center Panel: Visual Analytics charts ---
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsContainer.setOpaque(false);

        chartSubjectAverages = new AnalyticsChart(AnalyticsChart.TYPE_BAR);
        chartSubjectAverages.setTitle("Subject Averages (%)");
        chartsContainer.add(chartSubjectAverages);

        chartGradeDist = new AnalyticsChart(AnalyticsChart.TYPE_PIE);
        chartGradeDist.setTitle("Grade Distribution");
        chartsContainer.add(chartGradeDist);

        // --- 3. Bottom Panel: Recent Activities Log Stream ---
        JPanel bottomPanel = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        bottomPanel.setPreferredSize(new Dimension(getWidth(), 200));

        JLabel lblLogTitle = new JLabel("Recent System Logs");
        lblLogTitle.setFont(UIComponents.FONT_SUBTITLE);
        lblLogTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblLogTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        bottomPanel.add(lblLogTitle, BorderLayout.NORTH);

        String[] cols = {"Timestamp", "User", "Role", "Action"};
        logModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable = new JTable(logModel);
        UIComponents.styleTable(logTable);

        JScrollPane scroll = new JScrollPane(logTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);
        bottomPanel.add(scroll, BorderLayout.CENTER);

        // Combine Center and Bottom
        JPanel centerContainer = new JPanel(new BorderLayout(0, 20));
        centerContainer.setOpaque(false);
        centerContainer.add(chartsContainer, BorderLayout.CENTER);
        centerContainer.add(bottomPanel, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title, SwingConstants.LEFT);
        lblTitle.setFont(UIComponents.FONT_SMALL);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Re-queries statistics to update values and plots
     */
    public void loadDashboardData() {
        int studentsCount = studentService.getAllStudents().size();
        int subjectsCount = subjectService.getAllSubjects().size();
        
        lblTotalStudents.setText(String.valueOf(studentsCount));
        lblTotalSubjects.setText(String.valueOf(subjectsCount));

        // Aggregate overall class pass ratios and grades
        double sumCgpa = 0.0;
        int activeGrades = 0;
        int passCount = 0;
        int failCount = 0;

        Map<String, Integer> dist = new HashMap<>();
        dist.put("A+", 0);
        dist.put("A", 0);
        dist.put("B", 0);
        dist.put("C", 0);
        dist.put("D", 0);
        dist.put("F", 0);

        Map<String, Double> subSums = new HashMap<>();
        Map<String, Integer> subCounts = new HashMap<>();

        List<model.Student> students = studentService.getAllStudents();
        for (model.Student s : students) {
            double cgpa = assessmentService.getStudentCGPA(s.getStudentId());
            if (cgpa > 0 || !assessmentService.getAssessmentsForStudent(s.getStudentId()).isEmpty()) {
                sumCgpa += cgpa;
                activeGrades++;
            }

            for (model.Assessment a : assessmentService.getAssessmentsForStudent(s.getStudentId())) {
                String g = a.getGrade();
                dist.put(g, dist.getOrDefault(g, 0) + 1);

                if (a.getTotalMarks() >= 50.0) {
                    passCount++;
                } else {
                    failCount++;
                }

                // Add to subject average charts
                model.Subject sub = subjectService.getSubjectById(a.getSubjectId());
                if (sub != null) {
                    subSums.put(sub.getCode(), subSums.getOrDefault(sub.getCode(), 0.0) + a.getTotalMarks());
                    subCounts.put(sub.getCode(), subCounts.getOrDefault(sub.getCode(), 0) + 1);
                }
            }
        }

        // Set GPA
        double avgGpa = activeGrades > 0 ? (sumCgpa / activeGrades) : 0.0;
        lblAvgGPA.setText(String.format(java.util.Locale.US, "%.2f", avgGpa));

        // Set Pass Rate
        int totalAssessments = passCount + failCount;
        double passRate = totalAssessments > 0 ? (((double) passCount / totalAssessments) * 100.0) : 100.0;
        lblPassRate.setText(String.format(java.util.Locale.US, "%.1f%%", passRate));
        if (passRate < 75.0) {
            lblPassRate.setForeground(UIComponents.COLOR_DANGER);
        } else {
            lblPassRate.setForeground(UIComponents.COLOR_SUCCESS);
        }

        // Setup custom charts
        // 1. Bar Chart: Subject averages
        Map<String, Double> chartAverages = new HashMap<>();
        for (String code : subSums.keySet()) {
            chartAverages.put(code, subSums.get(code) / subCounts.get(code));
        }
        chartSubjectAverages.setData(chartAverages);

        // 2. Pie Chart: Grade breakdown
        chartGradeDist.setIntData(dist);

        // 3. Load activities log (last 10 rows)
        logModel.setRowCount(0);
        List<ActivityLog> logs = new dao.ActivityLogDAO().getAll();
        int startIndex = Math.max(0, logs.size() - 8);
        for (int i = logs.size() - 1; i >= startIndex; i--) {
            ActivityLog log = logs.get(i);
            logModel.addRow(new Object[]{
                    log.getTimestamp(),
                    log.getUsername(),
                    log.getRole(),
                    log.getActionDetails()
            });
        }
    }
}
