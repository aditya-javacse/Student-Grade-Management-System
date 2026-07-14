package view;

import service.AttendanceService;
import service.StudentService;
import model.Student;
import model.Attendance;
import util.UIComponents;
import util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final StudentService studentService;
    private final AttendanceService attendanceService;

    private JComboBox<String> cbClass;
    private UIComponents.CustomTextField txtDate;
    private JTable tblRoster;
    private DefaultTableModel modelRoster;
    
    // Status selectors
    private UIComponents.CustomButton btnPresent;
    private UIComponents.CustomButton btnAbsent;
    private UIComponents.CustomButton btnLate;
    private JLabel lblSelectedRosterStudent;
    
    private DefaultListModel<String> modelAlerts;
    private JList<String> listAlerts;

    // Track active roster records in memory
    private List<Attendance> currentRoster = new ArrayList<>();

    public AttendancePanel(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadRoster();
        loadLowAttendanceAlerts();
    }

    private void initComponents() {
        // --- 1. Top Filter Header ---
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
        cbClass.addActionListener(e -> {
            loadRoster();
            loadLowAttendanceAlerts();
        });
        topPanel.add(cbClass);

        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(UIComponents.FONT_BOLD);
        lblDate.setForeground(UIComponents.COLOR_TEXT_SEC);
        topPanel.add(lblDate);

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        txtDate = new UIComponents.CustomTextField(today);
        txtDate.setText(today);
        txtDate.setPreferredSize(new Dimension(120, 36));
        txtDate.addActionListener(e -> loadRoster());
        topPanel.add(txtDate);

        UIComponents.CustomButton btnRefresh = new UIComponents.CustomButton("Fetch Sheet", UIComponents.COLOR_CARD_LIGHT, UIComponents.COLOR_CARD_LIGHT.brighter());
        btnRefresh.addActionListener(e -> loadRoster());
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Panel: Attendance Grid ---
        JPanel centerGrid = new JPanel(new BorderLayout(0, 15));
        centerGrid.setOpaque(false);

        String[] cols = {"Student ID", "Name", "Roll No", "Attendance Status"};
        modelRoster = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRoster = new JTable(modelRoster);
        UIComponents.styleTable(tblRoster);
        tblRoster.getSelectionModel().addListSelectionListener(e -> handleRosterSelected());

        JScrollPane scroll = new JScrollPane(tblRoster);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);

        JPanel tableCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(scroll, BorderLayout.CENTER);

        // Roster marker actions
        JPanel rosterActions = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        rosterActions.setBorder(new EmptyBorder(10, 20, 10, 20));
        rosterActions.setLayout(new BorderLayout());
        rosterActions.setPreferredSize(new Dimension(getWidth(), 90));

        lblSelectedRosterStudent = new JLabel("Select a student to toggle attendance");
        lblSelectedRosterStudent.setFont(UIComponents.FONT_BOLD);
        lblSelectedRosterStudent.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblSelectedRosterStudent.setBorder(new EmptyBorder(0, 0, 5, 0));
        rosterActions.add(lblSelectedRosterStudent, BorderLayout.NORTH);

        JPanel statusBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusBtns.setOpaque(false);

        btnPresent = new UIComponents.CustomButton("Present", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnPresent.setEnabled(false);
        btnPresent.addActionListener(e -> setStatusForSelected("PRESENT"));
        statusBtns.add(btnPresent);

        btnLate = new UIComponents.CustomButton("Late", UIComponents.COLOR_ACCENT, UIComponents.COLOR_ACCENT.brighter());
        btnLate.setEnabled(false);
        btnLate.addActionListener(e -> setStatusForSelected("LATE"));
        statusBtns.add(btnLate);

        btnAbsent = new UIComponents.CustomButton("Absent", UIComponents.COLOR_DANGER, UIComponents.COLOR_DANGER.brighter());
        btnAbsent.setEnabled(false);
        btnAbsent.addActionListener(e -> setStatusForSelected("ABSENT"));
        statusBtns.add(btnAbsent);

        UIComponents.CustomButton btnSaveAll = new UIComponents.CustomButton("Save Attendance Sheet", new Color(79, 70, 229), new Color(99, 102, 241));
        btnSaveAll.addActionListener(this::handleSaveRoster);
        statusBtns.add(Box.createHorizontalStrut(50));
        statusBtns.add(btnSaveAll);

        rosterActions.add(statusBtns, BorderLayout.CENTER);

        centerGrid.add(tableCard, BorderLayout.CENTER);
        centerGrid.add(rosterActions, BorderLayout.SOUTH);
        add(centerGrid, BorderLayout.CENTER);

        // --- 3. Right Sidebar: Low Attendance Alerts (< 75%) ---
        JPanel rightPanel = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        rightPanel.setPreferredSize(new Dimension(280, getHeight()));
        rightPanel.setLayout(new BorderLayout(0, 10));

        JLabel lblAlertTitle = new JLabel("Attendance Warnings (<75%)");
        lblAlertTitle.setFont(UIComponents.FONT_BOLD);
        lblAlertTitle.setForeground(UIComponents.COLOR_DANGER);
        rightPanel.add(lblAlertTitle, BorderLayout.NORTH);

        modelAlerts = new DefaultListModel<>();
        listAlerts = new JList<>(modelAlerts);
        listAlerts.setBackground(new Color(23, 33, 48));
        listAlerts.setForeground(UIComponents.COLOR_DANGER);
        listAlerts.setFont(UIComponents.FONT_BODY);
        listAlerts.setBorder(BorderFactory.createLineBorder(UIComponents.COLOR_BORDER));

        JScrollPane alertScroll = new JScrollPane(listAlerts);
        alertScroll.setBorder(null);
        rightPanel.add(alertScroll, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.EAST);
    }

    private void loadRoster() {
        modelRoster.setRowCount(0);
        currentRoster.clear();

        String className = (String) cbClass.getSelectedItem();
        String dateStr = txtDate.getText().trim();

        if (className == null || dateStr.isEmpty()) return;
        if (!DateUtil.isValidDate(dateStr)) {
            JOptionPane.showMessageDialog(this, "Date must match format YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentRoster = attendanceService.getRosterForClassAndDate(className, dateStr);
        for (Attendance att : currentRoster) {
            Student stu = studentService.getStudentById(att.getStudentId());
            if (stu != null) {
                modelRoster.addRow(new Object[]{
                        att.getStudentId(),
                        stu.getName(),
                        stu.getRollNumber(),
                        att.getStatus().isEmpty() ? "UNMARKED" : att.getStatus()
                });
            }
        }
        resetForm();
    }

    private void loadLowAttendanceAlerts() {
        modelAlerts.clear();
        String className = (String) cbClass.getSelectedItem();
        if (className == null) return;

        List<Student> students = studentService.getAllStudents();
        for (Student s : students) {
            if (s.getStudentClass().equalsIgnoreCase(className)) {
                // Check if we have at least some logs to calculate rate
                double rate = attendanceService.calculateAttendancePercentage(s.getStudentId());
                List<Attendance> recs = attendanceService.getAttendanceForStudent(s.getStudentId());
                
                // Only alert if we have records registered and rate < 75%
                if (!recs.isEmpty() && rate < 75.0) {
                    modelAlerts.addElement(String.format("%s: %.1f%%", s.getName(), rate));
                }
            }
        }
    }

    private void handleRosterSelected() {
        int row = tblRoster.getSelectedRow();
        if (row == -1) {
            resetForm();
            return;
        }

        String stuId = (String) tblRoster.getValueAt(row, 0);
        String name = (String) tblRoster.getValueAt(row, 1);
        lblSelectedRosterStudent.setText("Toggle Status: " + name + " (" + stuId + ")");

        btnPresent.setEnabled(true);
        btnLate.setEnabled(true);
        btnAbsent.setEnabled(true);
    }

    private void setStatusForSelected(String status) {
        int row = tblRoster.getSelectedRow();
        if (row == -1) return;

        // Update in-memory roster
        currentRoster.get(row).setStatus(status);

        // Update table UI
        tblRoster.setValueAt(status, row, 3);
    }

    private void resetForm() {
        lblSelectedRosterStudent.setText("Select a student to toggle attendance");
        btnPresent.setEnabled(false);
        btnLate.setEnabled(false);
        btnAbsent.setEnabled(false);
    }

    private void handleSaveRoster(ActionEvent e) {
        if (currentRoster.isEmpty()) return;
        
        attendanceService.saveRoster(currentRoster);
        JOptionPane.showMessageDialog(this, "Attendance sheet saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        
        loadRoster();
        loadLowAttendanceAlerts();
    }
}
