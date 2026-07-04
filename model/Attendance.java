package model;

public class Attendance {
    private String studentId;
    private String date; // YYYY-MM-DD
    private String status; // "PRESENT", "ABSENT", "LATE"

    public Attendance() {}

    public Attendance(String studentId, String date, String status) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Attendance{" +
                "studentId='" + studentId + '\'' +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
