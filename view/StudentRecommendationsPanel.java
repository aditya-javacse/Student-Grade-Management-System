package view;

import service.AIService;
import service.AssessmentService;
import service.AttendanceService;
import service.SubjectService;
import model.Subject;
import model.Assessment;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StudentRecommendationsPanel extends JPanel {
    private final String studentId;
    private final SubjectService subjectService;
    private final AssessmentService assessmentService;
    private final AttendanceService attendanceService;
    private final AIService aiService;

    private JPanel cardsPanel;

    public StudentRecommendationsPanel(String studentId, SubjectService subjectService,
                                      AssessmentService assessmentService, AttendanceService attendanceService) {
        this.studentId = studentId;
        this.subjectService = subjectService;
        this.assessmentService = assessmentService;
        this.attendanceService = attendanceService;
        
        // Instantiate DAO dependencies directly
        this.aiService = new AIService(new dao.AssessmentDAO(), new dao.SubjectDAO());

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadRecommendations();
    }

    private void initComponents() {
        // Title banner
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("AI-Powered Performance Analyzer & Recommendations");
        lblTitle.setFont(UIComponents.FONT_SUBTITLE);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        titlePanel.add(lblTitle, BorderLayout.NORTH);

        JLabel lblSub = new JLabel("Smart algorithmic analysis of grades to pinpoint learning gaps and project scores");
        lblSub.setFont(UIComponents.FONT_SMALL);
        lblSub.setForeground(UIComponents.COLOR_TEXT_MUTED);
        titlePanel.add(lblSub, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);

        // Center scroll area for recommendations cards
        cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(UIComponents.COLOR_BG);

        add(scroll, BorderLayout.CENTER);
    }

    private void loadRecommendations() {
        cardsPanel.removeAll();
        List<AIService.AIRecommendation> recs = aiService.generateRecommendations(studentId);

        if (recs.isEmpty()) {
            JPanel emptyCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
            emptyCard.setBorder(new EmptyBorder(30, 30, 30, 30));
            emptyCard.setLayout(new BorderLayout());
            
            JLabel lblMsg = new JLabel("🎉 Excellent Job! No learning gaps detected. All your subject percentages exceed 85%. Keep it up!", SwingConstants.CENTER);
            lblMsg.setFont(UIComponents.FONT_BOLD);
            lblMsg.setForeground(UIComponents.COLOR_SUCCESS);
            emptyCard.add(lblMsg, BorderLayout.CENTER);
            
            cardsPanel.add(emptyCard);
        } else {
            for (AIService.AIRecommendation r : recs) {
                cardsPanel.add(createRecommendationCard(r));
                cardsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createRecommendationCard(AIService.AIRecommendation r) {
        JPanel card = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setLayout(new BorderLayout(15, 10));

        // Card header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblSubName = new JLabel(r.subjectName.toUpperCase() + " (" + r.subjectCode + ")");
        lblSubName.setFont(UIComponents.FONT_SUBTITLE);
        lblSubName.setForeground(UIComponents.COLOR_TEXT_MAIN);

        JLabel lblScore = new JLabel("Current Score: " + String.format(java.util.Locale.US, "%.1f%%", r.currentMarks) + " [" + r.currentGrade + "]");
        lblScore.setFont(UIComponents.FONT_BOLD);
        lblScore.setForeground(r.currentMarks < 50.0 ? UIComponents.COLOR_DANGER : (r.currentMarks < 70.0 ? Color.ORANGE : UIComponents.COLOR_SUCCESS));

        header.add(lblSubName, BorderLayout.WEST);
        header.add(lblScore, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Body content
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 10));
        body.setOpaque(false);

        // Weak areas
        StringBuilder weakText = new StringBuilder("<html><body style='width: 700px;'><span style='color:#ef4444; font-weight:600; font-size:10px; text-transform:uppercase;'>Identified Challenges:</span><br/>");
        if (r.weakAreas.isEmpty()) {
            weakText.append("<strong style='color:#f8fafc; font-size:12px;'>None</strong>");
        } else {
            for (int i = 0; i < r.weakAreas.size(); i++) {
                weakText.append("<strong style='color:#f8fafc; font-size:12px;'>• ").append(r.weakAreas.get(i)).append("</strong>");
                if (i < r.weakAreas.size() - 1) weakText.append("<br/>");
            }
        }
        weakText.append("</body></html>");
        JLabel lblWeak = new JLabel(weakText.toString());
        body.add(lblWeak);

        // Study recommendations
        StringBuilder tipText = new StringBuilder("<html><body style='width: 700px;'><span style='color:#10b981; font-weight:600; font-size:10px; text-transform:uppercase;'>Study Recommendations:</span><br/>");
        for (int i = 0; i < r.studyTips.size(); i++) {
            tipText.append("<strong style='color:#f8fafc; font-size:12px;'>• ").append(r.studyTips.get(i)).append("</strong>");
            if (i < r.studyTips.size() - 1) tipText.append("<br/>");
        }
        tipText.append("</body></html>");
        JLabel lblTips = new JLabel(tipText.toString());
        body.add(lblTips);

        card.add(body, BorderLayout.CENTER);

        // Footer projected grade
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.COLOR_BORDER));

        JLabel lblProj = new JLabel("Projected Final Grade:  " + r.projectedGrade);
        lblProj.setFont(UIComponents.FONT_BOLD);
        lblProj.setForeground(UIComponents.COLOR_SUCCESS);
        lblProj.setBorder(new EmptyBorder(10, 0, 0, 0));

        footer.add(lblProj, BorderLayout.WEST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }
}
