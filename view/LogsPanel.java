package view;

import dao.ActivityLogDAO;
import model.ActivityLog;
import service.AuthService;
import service.StudentService;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LogsPanel extends JPanel {
    private final StudentService studentService;
    private final AuthService authService;
    private final ActivityLogDAO logDAO;

    private JTable tblLogs;
    private DefaultTableModel modelLogs;
    private UIComponents.CustomTextField txtSearch;

    public LogsPanel(StudentService studentService, AuthService authService) {
        this.studentService = studentService;
        this.authService = authService;
        this.logDAO = new ActivityLogDAO();

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadLogs();
    }

    private void initComponents() {
        // --- 1. Top Controls Bar ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topBar.setOpaque(false);

        txtSearch = new UIComponents.CustomTextField("Search log details...");
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.addActionListener(e -> loadLogs());
        topBar.add(txtSearch);

        UIComponents.CustomButton btnSearch = new UIComponents.CustomButton("Search Logs", UIComponents.COLOR_CARD_LIGHT, UIComponents.COLOR_CARD_LIGHT.brighter());
        btnSearch.addActionListener(e -> loadLogs());
        topBar.add(btnSearch);

        UIComponents.CustomButton btnRefresh = new UIComponents.CustomButton("Refresh List", UIComponents.COLOR_ACCENT, UIComponents.COLOR_ACCENT.brighter());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadLogs();
        });
        topBar.add(btnRefresh);

        add(topBar, BorderLayout.NORTH);

        // --- 2. Central Table Grid ---
        String[] cols = {"Timestamp", "Username", "User Role", "Transaction / Action details"};
        modelLogs = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLogs = new JTable(modelLogs);
        UIComponents.styleTable(tblLogs);

        // Make details column wider
        tblLogs.getColumnModel().getColumn(3).setPreferredWidth(450);

        JScrollPane scroll = new JScrollPane(tblLogs);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);

        JPanel tableCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadLogs() {
        modelLogs.setRowCount(0);
        List<ActivityLog> logs = logDAO.getAll();
        String query = txtSearch.getText().trim().toLowerCase();

        for (int i = logs.size() - 1; i >= 0; i--) {
            ActivityLog log = logs.get(i);
            
            // Search query filter
            if (!query.isEmpty()) {
                boolean match = log.getUsername().toLowerCase().contains(query) ||
                                log.getRole().toLowerCase().contains(query) ||
                                log.getActionDetails().toLowerCase().contains(query) ||
                                log.getTimestamp().toLowerCase().contains(query);
                if (!match) continue;
            }

            modelLogs.addRow(new Object[]{
                    log.getTimestamp(),
                    log.getUsername(),
                    log.getRole(),
                    log.getActionDetails()
            });
        }
    }
}
