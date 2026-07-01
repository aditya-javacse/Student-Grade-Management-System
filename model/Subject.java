package model;

public class Subject {
    private String subjectId;
    private String name;
    private String code;
    private int creditHours;

    public Subject() {}

    public Subject(String subjectId, String name, String code, int creditHours) {
        this.subjectId = subjectId;
        this.name = name;
        this.code = code;
        this.creditHours = creditHours;
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
