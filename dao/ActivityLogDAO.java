package dao;

import model.ActivityLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ActivityLogDAO {
    private static final String FILE_NAME = "activity_logs.csv";
    private static final List<String> HEADER = Arrays.asList("timestamp", "username", "role", "actionDetails");

    public ActivityLogDAO() {
        if (!CSVDatabase.fileExists(FILE_NAME)) {
            CSVDatabase.writeTable(FILE_NAME, HEADER, new ArrayList<>());
        }
    }

    public List<ActivityLog> getAll() {
        List<ActivityLog> logs = new ArrayList<>();
        List<List<String>> rows = CSVDatabase.readTable(FILE_NAME);
        
        // Skip header
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 4) continue;
            logs.add(new ActivityLog(
                    row.get(0),
                    row.get(1),
                    row.get(2),
                    row.get(3)
            ));
        }
        return logs;
    }

    /**
     * Appends a log entry directly to the file without rewriting the entire dataset.
     */
    public void log(String username, String role, String actionDetails) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        CSVDatabase.appendRow(FILE_NAME, Arrays.asList(timestamp, username, role, actionDetails));
    }
}
