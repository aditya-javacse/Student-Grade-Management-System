package view;

import service.ReportService;
import service.StudentService;
import model.Student;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class StudentReportCardPanel extends JPanel {
    private final String studentId;
    private final StudentService studentService;
    private final ReportService reportService;

    private JLabel lblStatus;
    private JProgressBar progressBar;
    private UIComponents.CustomButton btnExport;

    public StudentReportCardPanel(String studentId, StudentService studentService, ReportService reportService) {
        this.studentId = studentId;
        this.studentService = studentService;
        this.reportService = reportService;

        setLayout(new GridBagLayout());
        setOpaque(false);

        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JPanel container = new UIComponents.RoundedPanel(16, UIComponents.COLOR_CARD);
        container.setBorder(new EmptyBorder(30, 40, 30, 40));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setPreferredSize(new Dimension(500, 380));
        container.setMaximumSize(new Dimension(500, 380));

        Student s = studentService.getStudentById(studentId);
        String name = s != null ? s.getName() : "Student";

        JLabel lblTitle = new JLabel("Official Grade Card Portal");
        lblTitle.setFont(UIComponents.FONT_TITLE);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Generate official grade sheets with AI annotations");
        lblSubtitle.setFont(UIComponents.FONT_SMALL);
        lblSubtitle.setForeground(UIComponents.COLOR_TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitle.setBorder(new EmptyBorder(5, 0, 30, 0));
        container.add(lblSubtitle);

        // Info card details block
        JPanel infoBlock = new JPanel(new GridLayout(3, 1, 0, 8));
        infoBlock.setOpaque(false);
        infoBlock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIComponents.COLOR_BORDER, 1),
                new EmptyBorder(15, 20, 15, 20)
        ));
        
        infoBlock.add(new JLabel("<html><span style='color:#94a3b8;'>Student ID:</span> <strong style='color:#f8fafc;'>" + studentId + "</strong></html>"));
        infoBlock.add(new JLabel("<html><span style='color:#94a3b8;'>Student Name:</span> <strong style='color:#f8fafc;'>" + name + "</strong></html>"));
        infoBlock.add(new JLabel("<html><span style='color:#94a3b8;'>Class / Section:</span> <strong style='color:#f8fafc;'>" + (s != null ? s.getStudentClass() + " - " + s.getSection() : "N/A") + "</strong></html>"));
        infoBlock.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(infoBlock);

        // Progress components
        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(UIComponents.COLOR_SUCCESS);
        progressBar.setBackground(UIComponents.COLOR_CARD_LIGHT);
        progressBar.setBorder(null);
        progressBar.setPreferredSize(new Dimension(380, 24));
        progressBar.setMaximumSize(new Dimension(380, 24));
        progressBar.setVisible(false);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UIComponents.FONT_SMALL);
        lblStatus.setForeground(UIComponents.COLOR_TEXT_SEC);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setBorder(new EmptyBorder(15, 0, 5, 0));

        container.add(lblStatus);
        container.add(progressBar);
        container.add(Box.createRigidArea(new Dimension(0, 10)));

        // Export trigger button
        btnExport = new UIComponents.CustomButton("Export HTML Report Card", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExport.setMaximumSize(new Dimension(380, 45));
        btnExport.addActionListener(this::handleExportReportCard);
        container.add(btnExport);

        add(container, gbc);
    }

    private void handleExportReportCard(ActionEvent e) {
        btnExport.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        lblStatus.setText("Assembling official report card templates...");
        
        // Progress simulation timer
        Timer timer = new Timer(100, null);
        timer.addActionListener(evt -> {
            int val = progressBar.getValue();
            if (val < 90) {
                progressBar.setValue(val + 10);
            }
        });
        timer.start();

        reportService.exportReportCardAsync(studentId, new ReportService.ReportCallback() {
            @Override
            public void onSuccess(String path) {
                timer.stop();
                progressBar.setValue(100);
                lblStatus.setText("Export completed successfully!");
                btnExport.setEnabled(true);

                int openChoice = JOptionPane.showConfirmDialog(StudentReportCardPanel.this,
                        "Report card exported successfully!\nLocation: " + path + "\nWould you like to open it in your web browser now?",
                        "Export Success",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (openChoice == JOptionPane.YES_OPTION) {
                    try {
                        File file = new File(path);
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(file);
                        } else {
                            JOptionPane.showMessageDialog(StudentReportCardPanel.this,
                                    "Opening files automatically is not supported on this operating system.\nPlease navigate to: " + path,
                                    "Unsupported System Action", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(StudentReportCardPanel.this,
                                "Failed to open file: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
                progressBar.setVisible(false);
            }

            @Override
            public void onFailure(Exception ex) {
                timer.stop();
                progressBar.setVisible(false);
                lblStatus.setText("Export failed: " + ex.getMessage());
                btnExport.setEnabled(true);
                
                JOptionPane.showMessageDialog(StudentReportCardPanel.this,
                        "Failed to generate report card: " + ex.getMessage(),
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
