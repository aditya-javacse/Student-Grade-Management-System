package model;

public class Student {
    private String studentId;
    private String name;
    private String rollNumber;
    private String studentClass;
    private String section;
    private String email;
    private String phoneNumber;
    private String dateOfBirth; // YYYY-MM-DD
    private String address;

    public Student() {}

    public Student(String studentId, String name, String rollNumber, String studentClass, String section, String email, String phoneNumber, String dateOfBirth, String address) {
        this.studentId = studentId;
        this.name = name;
        this.rollNumber = rollNumber;
        this.studentClass = studentClass;
        this.section = section;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", rollNumber='" + rollNumber + '\'' +
                ", class='" + studentClass + '\'' +
                ", section='" + section + '\'' +
                '}';
    }
}
