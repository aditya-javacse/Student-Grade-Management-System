package model;

public class Assessment {
    private String studentId;
    private String subjectId;
    private double assignmentMarks;       // out of 100
    private double quizMarks;             // out of 100
    private double midTermMarks;          // out of 100
    private double finalExamMarks;         // out of 100
    private double internalAssessment;    // out of 100

    // Autocalculated
    private double totalMarks;             // out of 100
    private double percentage;             // equivalent to totalMarks
    private String grade;
    private double gpa;

    public Assessment() {}

    public Assessment(String studentId, String subjectId, double assignmentMarks, double quizMarks, double midTermMarks, double finalExamMarks, double internalAssessment) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.assignmentMarks = assignmentMarks;
        this.quizMarks = quizMarks;
        this.midTermMarks = midTermMarks;
        this.finalExamMarks = finalExamMarks;
        this.internalAssessment = internalAssessment;
        recalculate();
    }

    public void recalculate() {
        // Weighted formula: Assignment 15%, Quiz 15%, Mid-Term 30%, Final 30%, Internal 10%
        this.totalMarks = (assignmentMarks * 0.15) + 
                           (quizMarks * 0.15) + 
                           (midTermMarks * 0.30) + 
                           (finalExamMarks * 0.30) + 
                           (internalAssessment * 0.10);
        this.percentage = this.totalMarks;

        if (this.totalMarks >= 90) {
            this.grade = "A+";
            this.gpa = 4.0;
        } else if (this.totalMarks >= 80) {
            this.grade = "A";
            this.gpa = 3.7;
        } else if (this.totalMarks >= 70) {
            this.grade = "B";
            this.gpa = 3.0;
        } else if (this.totalMarks >= 60) {
            this.grade = "C";
            this.gpa = 2.0;
        } else if (this.totalMarks >= 50) {
            this.grade = "D";
            this.gpa = 1.0;
        } else {
            this.grade = "F";
            this.gpa = 0.0;
        }
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public double getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(double assignmentMarks) { 
        this.assignmentMarks = assignmentMarks; 
        recalculate();
    }

    public double getQuizMarks() { return quizMarks; }
    public void setQuizMarks(double quizMarks) { 
        this.quizMarks = quizMarks; 
        recalculate();
    }

    public double getMidTermMarks() { return midTermMarks; }
    public void setMidTermMarks(double midTermMarks) { 
        this.midTermMarks = midTermMarks; 
        recalculate();
    }

    public double getFinalExamMarks() { return finalExamMarks; }
    public void setFinalExamMarks(double finalExamMarks) { 
        this.finalExamMarks = finalExamMarks; 
        recalculate();
    }

    public double getInternalAssessment() { return internalAssessment; }
    public void setInternalAssessment(double internalAssessment) { 
        this.internalAssessment = internalAssessment; 
        recalculate();
    }

    public double getTotalMarks() { return totalMarks; }
    public double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public double getGpa() { return gpa; }

    @Override
    public String toString() {
        return "Assessment{" +
                "studentId='" + studentId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", totalMarks=" + totalMarks +
                ", grade='" + grade + '\'' +
                '}';
    }
}
