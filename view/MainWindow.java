package view;

import service.*;
import analytics.AnalyticsService;
import util.UIComponents;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends JFrame {
    private final AuthService authService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final AssessmentService assessmentService;
    private final AttendanceService attendanceService;
    private final AnalyticsService analyticsService;
    private final ReportService reportService;

    // Layout components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel lblUserWelcome;
    private JLabel lblUserRole;
    private JLabel lblTime;

    // Track buttons to manage active tabs
    private final Map<String, UIComponents.SidebarButton> sidebarButtons = new HashMap<>();

    public MainWindow(AuthService authService, StudentService studentService, SubjectService subjectService,
                      AssessmentService assessmentService, AttendanceService attendanceService,
                      AnalyticsService analyticsService, ReportService reportService) {
        this.authService = authService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.assessmentService = assessmentService;
        this.attendanceService = attendanceService;
        this.analyticsService = analyticsService;
        this.reportService = reportService;

        setTitle("AI Student Performance & Analytics Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIComponents.COLOR_BG);
        setLayout(new BorderLayout());

        initComponents();
        setupClock();
        
        // Load Login Panel first
        showLoginView();
    }

    private void initComponents() {
        // --- Left Sidebar ---
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(240, getHeight()));
        sidebarPanel.setBackground(UIComponents.COLOR_CARD);
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setVisible(false); // Hidden until logged in

        // Top brand logo area
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(25, 20, 20, 20));
        
        JLabel lblLogo = new JLabel("METROPOLITAN");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(UIComponents.COLOR_TEXT_MAIN);
        
        JLabel lblLogoSub = new JLabel("ANALYTICS SYSTEM");
        lblLogoSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblLogoSub.setForeground(UIComponents.COLOR_ACCENT);
        
        brandPanel.add(lblLogo, BorderLayout.NORTH);
        brandPanel.add(lblLogoSub, BorderLayout.SOUTH);
        sidebarPanel.add(brandPanel, BorderLayout.NORTH);

        // Sidebar Navigation links container
        JPanel navContainer = new JPanel();
        navContainer.setOpaque(false);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(new EmptyBorder(20, 0, 20, 0));
        sidebarPanel.add(navContainer, BorderLayout.CENTER);

        // --- Center Content Pane (CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- Top Status Bar ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIComponents.COLOR_CARD);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.COLOR_BORDER));
        topBar.setPreferredSize(new Dimension(getWidth(), 60));
        topBar.setVisible(false); // Hidden until logged in

        JPanel userPanel = new JPanel(new GridLayout(2, 1));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(8, 20, 8, 20));
        
        lblUserWelcome = new JLabel("Guest User");
        lblUserWelcome.setFont(UIComponents.FONT_BOLD);
        lblUserWelcome.setForeground(UIComponents.COLOR_TEXT_MAIN);
        
        lblUserRole = new JLabel("ROLE: GUEST");
        lblUserRole.setFont(UIComponents.FONT_SMALL);
        lblUserRole.setForeground(UIComponents.COLOR_TEXT_MUTED);
        
        userPanel.add(lblUserWelcome);
        userPanel.add(lblUserRole);
        topBar.add(userPanel, BorderLayout.WEST);

        // Clock display on right
        lblTime = new JLabel();
        lblTime.setFont(UIComponents.FONT_BOLD);
        lblTime.setForeground(UIComponents.COLOR_TEXT_SEC);
        lblTime.setBorder(new EmptyBorder(0, 20, 0, 20));
        topBar.add(lblTime, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Add topBar toggle logic on showPanel
    }

    /**
     * Instantiates Login Panel and injects services
     */
    public void showLoginView() {
        sidebarPanel.setVisible(false);
        if (getLayout() instanceof BorderLayout) {
            Component north = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.NORTH);
            if (north != null) north.setVisible(false);
        }
        
        contentPanel.removeAll();
        LoginPanel loginPanel = new LoginPanel(authService, this);
        contentPanel.add(loginPanel, "LOGIN");
        cardLayout.show(contentPanel, "LOGIN");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Configures the sidebar options based on role authorization
     */
    public void handlePostLogin() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        lblUserWelcome.setText(user.getUsername().toUpperCase());
        lblUserRole.setText("ROLE: " + user.getRole());

        // Toggle visibility
        sidebarPanel.setVisible(true);
        if (getLayout() instanceof BorderLayout) {
            Component north = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.NORTH);
            if (north != null) north.setVisible(true);
        }

        // Clear existing navigation and card components
        JPanel navContainer = (JPanel) ((BorderLayout) sidebarPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        navContainer.removeAll();
        contentPanel.removeAll();
        sidebarButtons.clear();

        // Register Panels
        // 1. Dashboard (All)
        DashboardPanel dash = new DashboardPanel(studentService, subjectService, assessmentService, attendanceService, analyticsService);
        contentPanel.add(dash, "DASHBOARD");
        addNavigationTab(navContainer, "Dashboard", "DASHBOARD");

        // Role-based layout switches
        if (user.getRole().equalsIgnoreCase("ADMIN")) {
            // Student CRUD
            StudentPanel stu = new StudentPanel(studentService, subjectService);
            contentPanel.add(stu, "STUDENTS");
            addNavigationTab(navContainer, "Students Manager", "STUDENTS");

            // Subject CRUD
            SubjectPanel sub = new SubjectPanel(subjectService, studentService);
            contentPanel.add(sub, "SUBJECTS");
            addNavigationTab(navContainer, "Subjects Manager", "SUBJECTS");

            // Analytics Panel
            AnalyticsPanel analytic = new AnalyticsPanel(studentService, analyticsService, reportService);
            contentPanel.add(analytic, "ANALYTICS");
            addNavigationTab(navContainer, "Performance Analytics", "ANALYTICS");

            // Audit Logs
            LogsPanel logs = new LogsPanel(studentService, authService); // Simulates checking data logs
            contentPanel.add(logs, "LOGS");
            addNavigationTab(navContainer, "Activity Logs", "LOGS");

        } else if (user.getRole().equalsIgnoreCase("TEACHER")) {
            // Grade Entry Panel
            MarksPanel marks = new MarksPanel(studentService, subjectService, assessmentService);
            contentPanel.add(marks, "MARKS");
            addNavigationTab(navContainer, "Grade Entry", "MARKS");

            // Attendance Panel
            AttendancePanel att = new AttendancePanel(studentService, attendanceService);
            contentPanel.add(att, "ATTENDANCE");
            addNavigationTab(navContainer, "Attendance Sheet", "ATTENDANCE");

            // Leaderboard Panel
            LeaderboardPanel leader = new LeaderboardPanel(studentService, subjectService, assessmentService);
            contentPanel.add(leader, "LEADERBOARD");
            addNavigationTab(navContainer, "Leaderboard", "LEADERBOARD");

            // Analytics Panel
            AnalyticsPanel analytic = new AnalyticsPanel(studentService, analyticsService, reportService);
            contentPanel.add(analytic, "ANALYTICS");
            addNavigationTab(navContainer, "Analytics Charts", "ANALYTICS");

        } else if (user.getRole().equalsIgnoreCase("STUDENT")) {
            String studentId = user.getLinkedId();
            
            // AI Study recommendation panel
            StudentRecommendationsPanel recPanel = new StudentRecommendationsPanel(studentId, subjectService, assessmentService, attendanceService);
            contentPanel.add(recPanel, "RECOMMENDATIONS");
            addNavigationTab(navContainer, "AI Recommendations", "RECOMMENDATIONS");

            // Alerts Panel
            StudentAlertsPanel alertPanel = new StudentAlertsPanel(studentId, studentService);
            contentPanel.add(alertPanel, "ALERTS");
            addNavigationTab(navContainer, "My Notifications", "ALERTS");
            
            // Report card panel for export
            StudentReportCardPanel reportPanel = new StudentReportCardPanel(studentId, studentService, reportService);
            contentPanel.add(reportPanel, "REPORTS");
            addNavigationTab(navContainer, "Export Report Card", "REPORTS");
        }

        // Add Spacer and Logout to sidebar bottom
        navContainer.add(Box.createVerticalGlue());
        
        UIComponents.SidebarButton btnLogout = new UIComponents.SidebarButton("Logout");
        btnLogout.addActionListener(e -> {
            authService.logout();
            showLoginView();
        });
        navContainer.add(btnLogout);

        // Highlight first page
        switchPanel("DASHBOARD");

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void addNavigationTab(JPanel container, String name, final String cardName) {
        final UIComponents.SidebarButton btn = new UIComponents.SidebarButton(name);
        btn.addActionListener(e -> switchPanel(cardName));
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebarButtons.put(cardName, btn);
    }

    public void switchPanel(String cardName) {
        cardLayout.show(contentPanel, cardName);
        
        // Reset active markers
        for (Map.Entry<String, UIComponents.SidebarButton> entry : sidebarButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(cardName));
        }
    }

    private void setupClock() {
        Timer timer = new Timer(1000, e -> {
            lblTime.setText(new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss").format(new Date()));
        });
        timer.start();
    }
}
