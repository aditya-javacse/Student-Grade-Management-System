package dao;

import model.Notification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationDAO {
    private static final String FILE_NAME = "notifications.csv";
    private static final List<String> HEADER = Arrays.asList(
            "notificationId", "studentId", "title", "message", "date", "isRead"
    );

    public NotificationDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
    }

    public List<Notification> getAll() {
        List<Notification> notifications = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 6) continue;
            notifications.add(new Notification(
                    row.get(0),
                    row.get(1),
                    row.get(2),
                    row.get(3),
                    row.get(4),
                    Boolean.parseBoolean(row.get(5))
            ));
        }
        return notifications;
    }

    public List<Notification> getByStudentId(String studentId) {
        List<Notification> results = new ArrayList<>();
        if (studentId == null) return results;
        for (Notification n : getAll()) {
            if (n.getStudentId().equalsIgnoreCase(studentId)) {
                results.add(n);
            }
        }
        return results;
    }

    public void save(Notification notification) {
        List<Notification> notifications = getAll();
        boolean exists = false;
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getNotificationId().equalsIgnoreCase(notification.getNotificationId())) {
                notifications.set(i, notification);
                exists = true;
                break;
            }
        }
        if (!exists) {
            notifications.add(notification);
        }
        saveAll(notifications);
    }

    public void delete(String notificationId) {
        List<Notification> notifications = getAll();
        notifications.removeIf(n -> n.getNotificationId().equalsIgnoreCase(notificationId));
        saveAll(notifications);
    }

    public void deleteAllForStudent(String studentId) {
        List<Notification> notifications = getAll();
        notifications.removeIf(n -> n.getStudentId().equalsIgnoreCase(studentId));
        saveAll(notifications);
    }

    public String generateNextId() {
        List<Notification> notifications = getAll();
        int maxId = 1000;
        for (Notification n : notifications) {
            String idStr = n.getNotificationId();
            if (idStr != null && idStr.startsWith("NOT")) {
                try {
                    int idVal = Integer.parseInt(idStr.substring(3));
                    if (idVal > maxId) {
                        maxId = idVal;
                    }
                } catch (NumberFormatException e) {
                    // Ignore malformed ID
                }
            }
        }
        return "NOT" + (maxId + 1);
    }

    private void saveAll(List<Notification> notifications) {
        List<List<String>> rows = new ArrayList<>();
        for (Notification n : notifications) {
            rows.add(Arrays.asList(
                    n.getNotificationId(),
                    n.getStudentId(),
                    n.getTitle(),
                    n.getMessage(),
                    n.getDate(),
                    String.valueOf(n.isRead())
            ));
        }
        CSVDatabase.writeTable(FILE_NAME, HEADER, rows);
    }
}
