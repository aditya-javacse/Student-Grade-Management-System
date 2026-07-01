package view;

import dao.NotificationDAO;
import model.Notification;
import service.StudentService;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class StudentAlertsPanel extends JPanel {
    private final String studentId;
    private final StudentService studentService;
    private final NotificationDAO notificationDAO;

    private JTable tblAlerts;
    private DefaultTableModel modelAlerts;

    public StudentAlertsPanel(String studentId, StudentService studentService) {
        this.studentId = studentId;
        this.studentService = studentService;
        this.notificationDAO = new NotificationDAO();

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadAlerts();
    }

    private void initComponents() {
        // --- 1. Top Controls Banner ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Alerts & Notifications");
        lblTitle.setFont(UIComponents.FONT_SUBTITLE);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        titlePanel.add(lblTitle);

        JLabel lblSub = new JLabel("Track low performance alerts, low attendance Warnings, and rank updates");
        lblSub.setFont(UIComponents.FONT_SMALL);
        lblSub.setForeground(UIComponents.COLOR_TEXT_MUTED);
        titlePanel.add(lblSub);
        
        topBar.add(titlePanel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        UIComponents.CustomButton btnRead = new UIComponents.CustomButton("Mark as Read", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnRead.addActionListener(this::handleMarkRead);
        actions.add(btnRead);

        UIComponents.CustomButton btnRefresh = new UIComponents.CustomButton("Refresh Alerts", UIComponents.COLOR_ACCENT, UIComponents.COLOR_ACCENT.brighter());
        btnRefresh.addActionListener(e -> loadAlerts());
        actions.add(btnRefresh);

        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // --- 2. Central Table Grid ---
        String[] cols = {"Alert ID", "Date Received", "Title / Alert Category", "Message Details", "Read Status"};
        modelAlerts = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAlerts = new JTable(modelAlerts);
        UIComponents.styleTable(tblAlerts);
        
        tblAlerts.getColumnModel().getColumn(3).setPreferredWidth(480);

        JScrollPane scroll = new JScrollPane(tblAlerts);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);

        JPanel tableCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadAlerts() {
        modelAlerts.setRowCount(0);
        List<Notification> list = notificationDAO.getByStudentId(studentId);
        
        // Reverse list to show newest first
        for (int i = list.size() - 1; i >= 0; i--) {
            Notification n = list.get(i);
            modelAlerts.addRow(new Object[]{
                    n.getNotificationId(),
                    n.getDate(),
                    n.getTitle(),
                    n.getMessage(),
                    n.isRead() ? "Read" : "UNREAD"
            });
        }
    }

    private void handleMarkRead(ActionEvent e) {
        int row = tblAlerts.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an alert from the table to mark as read", "Select Alert", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String alertId = (String) tblAlerts.getValueAt(row, 0);
        for (Notification n : notificationDAO.getAll()) {
            if (n.getNotificationId().equalsIgnoreCase(alertId)) {
                n.setRead(true);
                notificationDAO.save(n);
                break;
            }
        }
        loadAlerts();
    }
}
