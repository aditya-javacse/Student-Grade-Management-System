package view;

import service.ReportService;
import analytics.AnalyticsService;
import service.StudentService;
import util.UIComponents;
import util.AnalyticsChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AnalyticsPanel extends JPanel {
    private final StudentService studentService;
    private final AnalyticsService analyticsService;
    private final ReportService reportService;

    private JComboBox<String> cbClass;
    
    // Aggregate text widgets
    private JLabel lblTopperName;
    private JLabel lblClassAvg;
    private JLabel lblPassPercent;
    
    // Graphical displays
    private AnalyticsChart chartSubjectAverages;
    private AnalyticsChart chartGradeDist;

    private UIComponents.CustomButton btnExportReport;

    public AnalyticsPanel(StudentService studentService, AnalyticsService analyticsService, ReportService reportService) {
        this.studentService = studentService;
        this.analyticsService = analyticsService;
        this.reportService = reportService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadAnalyticsData();
    }

    private void initComponents() {
        // --- 1. Selection Header ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setOpaque(false);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterGroup.setOpaque(false);

        JLabel lblSelectClass = new JLabel("Target Class:");
        lblSelectClass.setFont(UIComponents.FONT_BOLD);
        lblSelectClass.setForeground(UIComponents.COLOR_TEXT_SEC);
        filterGroup.add(lblSelectClass);

        cbClass = new JComboBox<>(new String[]{"Class 10", "Class 11", "Class 12"});
        cbClass.setFont(UIComponents.FONT_BODY);
        cbClass.setBackground(UIComponents.COLOR_CARD);
        cbClass.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbClass.addActionListener(e -> loadAnalyticsData());
        filterGroup.add(cbClass);
        
        topPanel.add(filterGroup, BorderLayout.WEST);

        // Export Excel Sheet button on right
        btnExportReport = new UIComponents.CustomButton("Export Performance CSV", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnExportReport.addActionListener(this::handleExportReport);
        topPanel.add(btnExportReport, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Stat summary banner ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(getWidth(), 90));

        lblTopperName = new JLabel("-", SwingConstants.LEFT);
        statsPanel.add(createCard("CLASS VALEDICTORIAN (TOPPER)", lblTopperName));

        lblClassAvg = new JLabel("0.0%", SwingConstants.LEFT);
        statsPanel.add(createCard("CLASS AVERAGE MARKS", lblClassAvg));

        lblPassPercent = new JLabel("0.0%", SwingConstants.LEFT);
        statsPanel.add(createCard("CLASS PASSING RATE", lblPassPercent));

        // --- 3. Custom visual charts ---
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsContainer.setOpaque(false);

        chartSubjectAverages = new AnalyticsChart(AnalyticsChart.TYPE_BAR);
        chartSubjectAverages.setTitle("Subject Averages (%)");
        chartsContainer.add(chartSubjectAverages);

        chartGradeDist = new AnalyticsChart(AnalyticsChart.TYPE_PIE);
        chartGradeDist.setTitle("Class Grade Distributions");
        chartsContainer.add(chartGradeDist);

        // Assemble main workspace
        JPanel centerGroup = new JPanel(new BorderLayout(0, 20));
        centerGroup.setOpaque(false);
        centerGroup.add(statsPanel, BorderLayout.NORTH);
        centerGroup.add(chartsContainer, BorderLayout.CENTER);

        add(centerGroup, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valLabel) {
        JPanel c = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        c.setLayout(new BorderLayout(5, 5));
        c.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIComponents.FONT_SMALL);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MUTED);

        valLabel.setFont(UIComponents.FONT_SUBTITLE);
        valLabel.setForeground(UIComponents.COLOR_TEXT_MAIN);

        c.add(lblTitle, BorderLayout.NORTH);
        c.add(valLabel, BorderLayout.CENTER);
        return c;
    }

    private void loadAnalyticsData() {
        String className = (String) cbClass.getSelectedItem();
        if (className == null) return;

        AnalyticsService.ClassSummary summary = analyticsService.getClassAnalytics(className);
        
        // Load metric values
        if (summary.topper != null) {
            lblTopperName.setText(summary.topper.getName() + " (" + String.format(java.util.Locale.US, "%.1f%%", summary.topperAverage) + ")");
            lblTopperName.setForeground(UIComponents.COLOR_ACCENT);
        } else {
            lblTopperName.setText("No grades entered yet");
            lblTopperName.setForeground(UIComponents.COLOR_TEXT_MUTED);
        }

        lblClassAvg.setText(String.format(java.util.Locale.US, "%.1f%%", summary.averagePercentage));
        lblPassPercent.setText(String.format(java.util.Locale.US, "%.1f%%", summary.passPercentage));

        if (summary.passPercentage < 75.0) {
            lblPassPercent.setForeground(UIComponents.COLOR_DANGER);
        } else {
            lblPassPercent.setForeground(UIComponents.COLOR_SUCCESS);
        }

        // Set chart values
        chartSubjectAverages.setData(summary.subjectAverages);
        chartGradeDist.setIntData(summary.gradeDistribution);
    }

    private void handleExportReport(ActionEvent e) {
        String className = (String) cbClass.getSelectedItem();
        if (className == null) return;

        btnExportReport.setEnabled(false);
        btnExportReport.setText("Exporting Class Report...");

        // Start background compilation thread
        reportService.exportClassReportAsync(className, new ReportService.ReportCallback() {
            @Override
            public void onSuccess(String path) {
                btnExportReport.setEnabled(true);
                btnExportReport.setText("Export Performance CSV");
                
                JOptionPane.showMessageDialog(AnalyticsPanel.this,
                        "Spreadsheet exported successfully!\nSaved to: " + path,
                        "Report Card Exported",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void onFailure(Exception ex) {
                btnExportReport.setEnabled(true);
                btnExportReport.setText("Export Performance CSV");
                
                JOptionPane.showMessageDialog(AnalyticsPanel.this,
                        "Export compilation failed: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
