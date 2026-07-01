package view;

import service.AuthService;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    private final AuthService authService;
    private final MainWindow parentFrame;

    private UIComponents.CustomTextField txtUsername;
    private UIComponents.CustomPasswordField txtPassword;
    private JLabel lblError;

    // Reset Panel fields
    private JPanel mainLoginSubPanel;
    private JPanel resetSubPanel;
    private UIComponents.CustomTextField txtResetUser;
    private JLabel lblResetQuestion;
    private UIComponents.CustomTextField txtResetAnswer;
    private UIComponents.CustomPasswordField txtResetNewPass;
    private UIComponents.CustomButton btnSubmitReset;

    public LoginPanel(AuthService authService, MainWindow parentFrame) {
        this.authService = authService;
        this.parentFrame = parentFrame;

        setLayout(new GridBagLayout());
        setBackground(UIComponents.COLOR_BG);

        initLoginLayout();
        initResetLayout();
        
        // Show main login screen first
        showSubPanel(mainLoginSubPanel);
    }

    private void initLoginLayout() {
        mainLoginSubPanel = new UIComponents.RoundedPanel(16, UIComponents.COLOR_CARD);
        mainLoginSubPanel.setLayout(new BoxLayout(mainLoginSubPanel, BoxLayout.Y_AXIS));
        mainLoginSubPanel.setBorder(new EmptyBorder(35, 45, 35, 45));
        mainLoginSubPanel.setPreferredSize(new Dimension(420, 480));
        mainLoginSubPanel.setMaximumSize(new Dimension(420, 480));

        // Header Title
        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setFont(UIComponents.FONT_TITLE);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Please enter your credentials to login");
        lblSubtitle.setFont(UIComponents.FONT_SMALL);
        lblSubtitle.setForeground(UIComponents.COLOR_TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitle.setBorder(new EmptyBorder(5, 0, 25, 0));

        // Form Fields
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UIComponents.FONT_BOLD);
        lblUser.setForeground(UIComponents.COLOR_TEXT_SEC);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new UIComponents.CustomTextField("Enter username");
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setMaximumSize(new Dimension(380, 40));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UIComponents.FONT_BOLD);
        lblPass.setForeground(UIComponents.COLOR_TEXT_SEC);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPass.setBorder(new EmptyBorder(15, 0, 0, 0));

        txtPassword = new UIComponents.CustomPasswordField("Enter password");
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(380, 40));

        // Error message placeholder
        lblError = new JLabel(" ");
        lblError.setFont(UIComponents.FONT_SMALL);
        lblError.setForeground(UIComponents.COLOR_DANGER);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setBorder(new EmptyBorder(15, 0, 5, 0));

        // Login Button
        UIComponents.CustomButton btnLogin = new UIComponents.CustomButton("Sign In");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(380, 45));
        btnLogin.addActionListener(this::handleLogin);

        // Forgot password option
        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(UIComponents.FONT_SMALL);
        btnForgot.setForeground(UIComponents.COLOR_ACCENT);
        btnForgot.setFocusPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorder(new EmptyBorder(15, 0, 0, 0));
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnForgot.addActionListener(e -> showSubPanel(resetSubPanel));

        // Assemble Login Screen
        mainLoginSubPanel.add(lblTitle);
        mainLoginSubPanel.add(lblSubtitle);
        mainLoginSubPanel.add(lblUser);
        mainLoginSubPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainLoginSubPanel.add(txtUsername);
        mainLoginSubPanel.add(lblPass);
        mainLoginSubPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainLoginSubPanel.add(txtPassword);
        mainLoginSubPanel.add(lblError);
        mainLoginSubPanel.add(btnLogin);
        mainLoginSubPanel.add(btnForgot);
    }

    private void initResetLayout() {
        resetSubPanel = new UIComponents.RoundedPanel(16, UIComponents.COLOR_CARD);
        resetSubPanel.setLayout(new BoxLayout(resetSubPanel, BoxLayout.Y_AXIS));
        resetSubPanel.setBorder(new EmptyBorder(35, 45, 35, 45));
        resetSubPanel.setPreferredSize(new Dimension(420, 520));
        resetSubPanel.setMaximumSize(new Dimension(420, 520));

        JLabel lblTitle = new JLabel("Password Recovery");
        lblTitle.setFont(UIComponents.FONT_TITLE);
        lblTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Answer security question to reset");
        lblSubtitle.setFont(UIComponents.FONT_SMALL);
        lblSubtitle.setForeground(UIComponents.COLOR_TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitle.setBorder(new EmptyBorder(5, 0, 20, 0));

        txtResetUser = new UIComponents.CustomTextField("Username");
        txtResetUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtResetUser.setMaximumSize(new Dimension(380, 40));

        UIComponents.CustomButton btnFetch = new UIComponents.CustomButton("Fetch Security Question", UIComponents.COLOR_CARD_LIGHT, UIComponents.COLOR_CARD_LIGHT.brighter());
        btnFetch.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFetch.setMaximumSize(new Dimension(380, 35));
        btnFetch.addActionListener(this::handleFetchQuestion);

        lblResetQuestion = new JLabel("No question loaded");
        lblResetQuestion.setFont(UIComponents.FONT_BOLD);
        lblResetQuestion.setForeground(UIComponents.COLOR_TEXT_SEC);
        lblResetQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblResetQuestion.setBorder(new EmptyBorder(15, 0, 5, 0));

        txtResetAnswer = new UIComponents.CustomTextField("Your Answer");
        txtResetAnswer.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtResetAnswer.setMaximumSize(new Dimension(380, 40));
        txtResetAnswer.setEnabled(false);

        txtResetNewPass = new UIComponents.CustomPasswordField("New Password");
        txtResetNewPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtResetNewPass.setMaximumSize(new Dimension(380, 40));
        txtResetNewPass.setEnabled(false);
        txtResetNewPass.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(15, 0, 0, 0), txtResetNewPass.getBorder()));

        btnSubmitReset = new UIComponents.CustomButton("Reset Password");
        btnSubmitReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmitReset.setMaximumSize(new Dimension(380, 45));
        btnSubmitReset.setEnabled(false);
        btnSubmitReset.addActionListener(this::handleResetPassword);

        JButton btnBack = new JButton("Back to Login");
        btnBack.setFont(UIComponents.FONT_SMALL);
        btnBack.setForeground(UIComponents.COLOR_TEXT_MUTED);
        btnBack.setFocusPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorder(new EmptyBorder(15, 0, 0, 0));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> showSubPanel(mainLoginSubPanel));

        resetSubPanel.add(lblTitle);
        resetSubPanel.add(lblSubtitle);
        resetSubPanel.add(txtResetUser);
        resetSubPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resetSubPanel.add(btnFetch);
        resetSubPanel.add(lblResetQuestion);
        resetSubPanel.add(txtResetAnswer);
        resetSubPanel.add(txtResetNewPass);
        resetSubPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resetSubPanel.add(btnSubmitReset);
        resetSubPanel.add(btnBack);
    }

    private void showSubPanel(JPanel panel) {
        removeAll();
        add(panel);
        revalidate();
        repaint();
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter username and password");
            return;
        }

        boolean success = authService.login(username, password);
        if (success) {
            lblError.setText(" ");
            txtUsername.setText("");
            txtPassword.setText("");
            parentFrame.handlePostLogin();
        } else {
            lblError.setText("Invalid username or password");
        }
    }

    private void handleFetchQuestion(ActionEvent e) {
        String username = txtResetUser.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check user
        model.User u = new dao.UserDAO().getByUsername(username);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblResetQuestion.setText("Q: " + u.getSecurityQuestion());
        txtResetAnswer.setEnabled(true);
        txtResetNewPass.setEnabled(true);
        btnSubmitReset.setEnabled(true);
        txtResetAnswer.requestFocus();
    }

    private void handleResetPassword(ActionEvent e) {
        String username = txtResetUser.getText().trim();
        String answer = txtResetAnswer.getText().trim();
        String newPass = txtResetNewPass.getActualPassword().trim();

        if (answer.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all recovery fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = authService.resetPassword(username, answer, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Reset state
            txtResetUser.setText("");
            lblResetQuestion.setText("No question loaded");
            txtResetAnswer.setText("");
            txtResetNewPass.setText("");
            txtResetAnswer.setEnabled(false);
            txtResetNewPass.setEnabled(false);
            btnSubmitReset.setEnabled(false);
            showSubPanel(mainLoginSubPanel);
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect answer. Verification failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
