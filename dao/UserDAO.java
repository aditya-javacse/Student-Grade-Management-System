package dao;

import model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDAO {
    private static final String FILE_NAME = "users.csv";
    private static final List<String> HEADER = Arrays.asList(
            "username", "passwordHash", "role", "securityQuestion", "securityAnswer", "linkedId"
    );

    public UserDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 6) continue;
            users.add(new User(
                    row.get(0),
                    row.get(1),
                    row.get(2),
                    row.get(3),
                    row.get(4),
                    row.get(5)
            ));
        }
        return users;
    }

    public User getByUsername(String username) {
        if (username == null) return null;
        for (User user : getAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public User getByLinkedId(String linkedId) {
        if (linkedId == null) return null;
        for (User user : getAll()) {
            if (linkedId.equalsIgnoreCase(user.getLinkedId())) {
                return user;
            }
        }
        return null;
    }

    public void save(User user) {
        List<User> users = getAll();
        // Check if user already exists
        boolean exists = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(user.getUsername())) {
                users.set(i, user);
                exists = true;
                break;
            }
        }
        if (!exists) {
            users.add(user);
        }
        saveAll(users);
    }

    public void delete(String username) {
        List<User> users = getAll();
        users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        saveAll(users);
    }

    private void saveAll(List<User> users) {
        List<List<String>> rows = new ArrayList<>();
        for (User u : users) {
            rows.add(Arrays.asList(
                    u.getUsername(),
                    u.getPasswordHash(),
                    u.getRole(),
                    u.getSecurityQuestion(),
                    u.getSecurityAnswer(),
                    u.getLinkedId()
            ));
        }
        CSVDatabase.writeTable(FILE_NAME, HEADER, rows);
    }
}
