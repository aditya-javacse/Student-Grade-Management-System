package model;

public class User {
    private String username;
    private String passwordHash;
    private String role; // "ADMIN", "TEACHER", "STUDENT"
    private String securityQuestion;
    private String securityAnswer;
    private String linkedId; // ID of Student or Teacher, if applicable

    public User() {}

    public User(String username, String passwordHash, String role, String securityQuestion, String securityAnswer, String linkedId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.linkedId = linkedId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public String getLinkedId() { return linkedId; }
    public void setLinkedId(String linkedId) { this.linkedId = linkedId; }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", linkedId='" + linkedId + '\'' +
                '}';
    }
}
