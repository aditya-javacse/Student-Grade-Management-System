package service;

import dao.UserDAO;
import dao.ActivityLogDAO;
import model.User;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class AuthService {
    private final UserDAO userDAO;
    private final ActivityLogDAO logDAO;
    private static User currentUser = null;
    private static long loginTime = 0;

    public AuthService(UserDAO userDAO, ActivityLogDAO logDAO) {
        this.userDAO = userDAO;
        this.logDAO = logDAO;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static long getLoginTime() {
        return loginTime;
    }

    /**
     * Checks if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Hashes password using SHA-256 algorithm.
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("SHA-256 Digest Error", ex);
        }
    }

    /**
     * Log in a user. Checks roles, compares SHA-256 hash.
     */
    public boolean login(String username, String password) {
        User user = userDAO.getByUsername(username);
        if (user == null) {
            return false;
        }

        String inputHash = hashPassword(password);
        if (user.getPasswordHash().equals(inputHash)) {
            currentUser = user;
            loginTime = System.currentTimeMillis();
            logDAO.log(user.getUsername(), user.getRole(), "User logged in successfully");
            return true;
        }
        
        logDAO.log(username, "UNKNOWN", "Failed login attempt: Invalid password");
        return false;
    }

    /**
     * Logs out the current session user.
     */
    public void logout() {
        if (currentUser != null) {
            logDAO.log(currentUser.getUsername(), currentUser.getRole(), "User logged out");
            currentUser = null;
            loginTime = 0;
        }
    }

    /**
     * Resets a user's password if the security answer is correct.
     */
    public boolean resetPassword(String username, String securityAnswer, String newPassword) {
        User user = userDAO.getByUsername(username);
        if (user == null) {
            return false;
        }

        if (user.getSecurityAnswer().equalsIgnoreCase(securityAnswer.trim())) {
            user.setPasswordHash(hashPassword(newPassword));
            userDAO.save(user);
            logDAO.log(username, user.getRole(), "Password reset via security question");
            return true;
        }
        
        logDAO.log(username, user.getRole(), "Failed password reset attempt: Wrong security answer");
        return false;
    }

    /**
     * Register a new user in the system (Admins only)
     */
    public boolean register(String username, String password, String role, String question, String answer, String linkedId) {
        if (userDAO.getByUsername(username) != null) {
            return false;
        }
        User newUser = new User(username, hashPassword(password), role, question, answer, linkedId);
        userDAO.save(newUser);
        
        String actor = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        String actorRole = (currentUser != null) ? currentUser.getRole() : "SYSTEM";
        logDAO.log(actor, actorRole, "Registered new user account: " + username + " with role: " + role);
        return true;
    }
}
