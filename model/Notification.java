package model;

public class Notification {
    private String notificationId;
    private String studentId;
    private String title;
    private String message;
    private String date; // YYYY-MM-DD
    private boolean isRead;

    public Notification() {}

    public Notification(String notificationId, String studentId, String title, String message, String date, boolean isRead) {
        this.notificationId = notificationId;
        this.studentId = studentId;
        this.title = title;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
