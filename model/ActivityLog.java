package model;

public class ActivityLog {
    private String timestamp;
    private String username;
    private String role;
    private String actionDetails;

    public ActivityLog() {}

    public ActivityLog(String timestamp, String username, String role, String actionDetails) {
        this.timestamp = timestamp;
        this.username = username;
        this.role = role;
        this.actionDetails = actionDetails;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getActionDetails() { return actionDetails; }
    public void setActionDetails(String actionDetails) { this.actionDetails = actionDetails; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + username + " (" + role + "): " + actionDetails;
    }
}
