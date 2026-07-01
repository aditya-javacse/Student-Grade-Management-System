package service;

import dao.StudentDAO;
import dao.SubjectDAO;
import dao.AssessmentDAO;
import dao.AttendanceDAO;
import model.Student;
import model.Subject;
import model.Assessment;
import model.Attendance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportService {
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;
    private final AssessmentDAO assessmentDAO;
    private final AttendanceDAO attendanceDAO;
    private final AssessmentService assessmentService;
    private final AttendanceService attendanceService;
    private final AIService aiService;

    private static final String REPORTS_DIR = "reports/";

    public interface ReportCallback {
        void onSuccess(String path);
        void onFailure(Exception e);
    }

    public ReportService(StudentDAO studentDAO, SubjectDAO subjectDAO, AssessmentDAO assessmentDAO, 
                         AttendanceDAO attendanceDAO, AssessmentService assessmentService, 
                         AttendanceService attendanceService, AIService aiService) {
        this.studentDAO = studentDAO;
        this.subjectDAO = subjectDAO;
        this.assessmentDAO = assessmentDAO;
        this.attendanceDAO = attendanceDAO;
        this.assessmentService = assessmentService;
        this.attendanceService = attendanceService;
        this.aiService = aiService;

        // Ensure reports directory exists
        File dir = new File(REPORTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Exports a premium HTML/CSS Report Card for a student on a background thread.
     */
    public void exportReportCardAsync(String studentId, ReportCallback callback) {
        new Thread(() -> {
            try {
                // Simulate processing delay for smooth UI transition
                Thread.sleep(1000);
                
                Student student = studentDAO.getById(studentId);
                if (student == null) {
                    throw new IllegalArgumentException("Student not found: " + studentId);
                }

                String path = REPORTS_DIR + "report_card_" + studentId + ".html";
                File file = new File(path);
                
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    bw.write(buildHTMLReportCard(student));
                }
                
                if (callback != null) {
                    callback.onSuccess(file.getAbsolutePath());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        }).start();
    }

    /**
     * Exports a spreadsheet performance grid for a whole class on a background thread.
     */
    public void exportClassReportAsync(String className, ReportCallback callback) {
        new Thread(() -> {
            try {
                Thread.sleep(1200);

                List<Student> students = new ArrayList<>();
                for (Student s : studentDAO.getAll()) {
                    if (s.getStudentClass().equalsIgnoreCase(className)) {
                        students.add(s);
                    }
                }

                if (students.isEmpty()) {
                    throw new IllegalArgumentException("No students found in class: " + className);
                }

                String path = REPORTS_DIR + "class_" + className.replace(" ", "_") + "_performance_report.csv";
                File file = new File(path);

                List<Subject> subjects = subjectDAO.getAll();

                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    
                    // Write header: ID, Name, Roll, [Subjects...], Avg Marks, CGPA, Attendance %, Rank
                    StringBuilder header = new StringBuilder("Student ID,Student Name,Roll Number");
                    for (Subject sub : subjects) {
                        header.append(",").append(sub.getName()).append(" (").append(sub.getCode()).append(")");
                    }
                    header.append(",Average Marks,CGPA,Attendance Rate,Class Rank");
                    bw.write(header.toString());
                    bw.newLine();

                    // Write student rows
                    for (Student s : students) {
                        StringBuilder row = new StringBuilder();
                        row.append(s.getStudentId()).append(",")
                           .append(escapeCSV(s.getName())).append(",")
                           .append(escapeCSV(s.getRollNumber()));

                        for (Subject sub : subjects) {
                            Assessment a = assessmentDAO.getByStudentAndSubject(s.getStudentId(), sub.getSubjectId());
                            if (a != null) {
                                row.append(",").append(String.format(Locale.US, "%.1f", a.getTotalMarks()));
                            } else {
                                row.append(",N/A");
                            }
                        }

                        double avg = assessmentService.getStudentAveragePercentage(s.getStudentId());
                        double cgpa = assessmentService.getStudentCGPA(s.getStudentId());
                        double att = attendanceService.calculateAttendancePercentage(s.getStudentId());
                        int rank = assessmentService.getStudentRankInClass(s.getStudentId(), className);

                        row.append(",").append(String.format(Locale.US, "%.1f", avg))
                           .append(",").append(String.format(Locale.US, "%.2f", cgpa))
                           .append(",").append(String.format(Locale.US, "%.1f%%", att))
                           .append(",").append(rank);

                        bw.write(row.toString());
                        bw.newLine();
                    }
                }

                if (callback != null) {
                    callback.onSuccess(file.getAbsolutePath());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        }).start();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Builds a visually stunning, responsive HTML/CSS Report Card print template.
     */
    private String buildHTMLReportCard(Student student) {
        String studentId = student.getStudentId();
        double avgPercent = assessmentService.getStudentAveragePercentage(studentId);
        double cgpa = assessmentService.getStudentCGPA(studentId);
        double attendanceRate = attendanceService.calculateAttendancePercentage(studentId);
        int classRank = assessmentService.getStudentRankInClass(studentId, student.getStudentClass());
        
        List<Assessment> assessments = assessmentDAO.getByStudentId(studentId);
        List<AIService.AIRecommendation> recs = aiService.generateRecommendations(studentId);

        StringBuilder marksRows = new StringBuilder();
        for (Assessment a : assessments) {
            Subject sub = subjectDAO.getById(a.getSubjectId());
            String subName = (sub != null) ? sub.getName() : "Unknown";
            String subCode = (sub != null) ? sub.getCode() : "N/A";
            int credits = (sub != null) ? sub.getCreditHours() : 0;
            
            marksRows.append(String.format(Locale.US,
                "<tr>" +
                "  <td><strong>%s</strong> <span class='sub-code'>(%s)</span></td>" +
                "  <td class='center'>%d</td>" +
                "  <td class='center'>%.1f</td>" +
                "  <td class='center'>%.1f</td>" +
                "  <td class='center'>%.1f</td>" +
                "  <td class='center'>%.1f</td>" +
                "  <td class='center'>%.1f</td>" +
                "  <td class='center bold'>%.1f</td>" +
                "  <td class='center bold grade-cell'>%s</td>" +
                "</tr>",
                subName, subCode, credits,
                a.getAssignmentMarks(), a.getQuizMarks(), a.getMidTermMarks(),
                a.getFinalExamMarks(), a.getInternalAssessment(), a.getTotalMarks(),
                a.getGrade()
            ));
        }

        StringBuilder aiBlock = new StringBuilder();
        if (recs.isEmpty()) {
            aiBlock.append("<p class='no-weakness'>🎉 Excellent! AI has detected no significant study gaps. Maintain your current learning schedule.</p>");
        } else {
            for (AIService.AIRecommendation r : recs) {
                aiBlock.append(String.format(
                    "<div class='ai-card'>" +
                    "  <h4>%s (%s) — Target Grade: <span class='projected'>%s</span></h4>" +
                    "  <div class='ai-details'>" +
                    "    <p><strong>Focus Areas:</strong> %s</p>" +
                    "    <p><strong>Actionable Recommendations:</strong></p>" +
                    "    <ul>",
                    r.subjectName, r.subjectCode, r.projectedGrade,
                    String.join(", ", r.weakAreas)
                ));
                for (String tip : r.studyTips) {
                    aiBlock.append("<li>").append(tip).append("</li>");
                }
                aiBlock.append("    </ul>" +
                               "  </div>" +
                               "</div>");
            }
        }

        String printDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Master premium CSS and HTML card
        return "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Academic Report Card - " + student.getName() + "</title>\n" +
                "    <style>\n" +
                "        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap');\n" +
                "        body { font-family: 'Inter', sans-serif; background-color: #f3f4f6; color: #1f2937; margin: 0; padding: 40px 20px; }\n" +
                "        .report-container { max-width: 900px; margin: 0 auto; background: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05), 0 4px 6px -2px rgba(0,0,0,0.02); border-top: 8px solid #1e3a8a; }\n" +
                "        .header-section { display: flex; justify-content: space-between; border-bottom: 2px solid #e5e7eb; padding-bottom: 20px; margin-bottom: 30px; }\n" +
                "        .institution-details h1 { margin: 0; font-size: 24px; color: #1e3a8a; font-weight: 700; letter-spacing: -0.025em; }\n" +
                "        .institution-details p { margin: 5px 0 0; color: #6b7280; font-size: 14px; }\n" +
                "        .report-title { text-align: right; }\n" +
                "        .report-title h2 { margin: 0; font-size: 22px; color: #111827; font-weight: 700; }\n" +
                "        .report-title p { margin: 5px 0 0; color: #9ca3af; font-size: 12px; }\n" +
                "        .info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; background-color: #f8fafc; padding: 20px; border-radius: 10px; margin-bottom: 30px; border: 1px solid #f1f5f9; }\n" +
                "        .info-item { font-size: 14px; }\n" +
                "        .info-item span { display: block; font-size: 11px; text-transform: uppercase; color: #64748b; font-weight: 600; letter-spacing: 0.05em; margin-bottom: 4px; }\n" +
                "        .info-item strong { color: #0f172a; font-size: 15px; }\n" +
                "        table { width: 100%; border-collapse: collapse; text-align: left; margin-bottom: 30px; font-size: 14px; }\n" +
                "        th { background-color: #1e3a8a; color: #ffffff; padding: 12px 16px; font-weight: 600; text-transform: uppercase; font-size: 11px; letter-spacing: 0.05em; }\n" +
                "        td { padding: 12px 16px; border-bottom: 1px solid #e2e8f0; }\n" +
                "        tr:nth-child(even) td { background-color: #f8fafc; }\n" +
                "        .center { text-align: center; }\n" +
                "        .bold { font-weight: 600; }\n" +
                "        .sub-code { font-size: 12px; color: #64748b; }\n" +
                "        .grade-cell { color: #1e3a8a; }\n" +
                "        .summary-banner { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 35px; text-align: center; }\n" +
                "        .summary-card { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 12px; padding: 15px; }\n" +
                "        .summary-card.accent { background: #f0fdf4; border-color: #bbf7d0; }\n" +
                "        .summary-card span { font-size: 11px; text-transform: uppercase; color: #475569; font-weight: 600; letter-spacing: 0.05em; display: block; margin-bottom: 5px; }\n" +
                "        .summary-card h3 { margin: 0; font-size: 24px; color: #1e40af; font-weight: 700; }\n" +
                "        .summary-card.accent h3 { color: #166534; }\n" +
                "        .section-title { font-size: 16px; font-weight: 600; color: #1e3a8a; border-left: 4px solid #1e3a8a; padding-left: 10px; margin-bottom: 20px; margin-top: 10px; }\n" +
                "        .ai-recommendations { background-color: #fcfcfd; border: 1px dashed #d1d5db; border-radius: 12px; padding: 25px; margin-bottom: 35px; }\n" +
                "        .ai-card { background: #ffffff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 15px; margin-bottom: 15px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }\n" +
                "        .ai-card h4 { margin: 0 0 10px 0; color: #374151; font-size: 15px; border-bottom: 1px solid #f3f4f6; padding-bottom: 8px; }\n" +
                "        .ai-card h4 .projected { color: #16a34a; font-weight: 700; }\n" +
                "        .ai-details p { margin: 5px 0; font-size: 13px; color: #4b5563; }\n" +
                "        .ai-details ul { margin: 8px 0 0 0; padding-left: 20px; font-size: 13px; color: #4b5563; }\n" +
                "        .ai-details li { margin-bottom: 4px; }\n" +
                "        .no-weakness { color: #166534; font-weight: 500; font-size: 14px; margin: 0; }\n" +
                "        .remarks-signatures { display: grid; grid-template-columns: 2fr 1fr; gap: 40px; margin-top: 40px; border-top: 1px solid #e5e7eb; padding-top: 30px; }\n" +
                "        .remarks-box { font-size: 14px; color: #4b5563; background: #fafafa; padding: 15px; border-radius: 8px; border: 1px solid #f0f0f0; min-height: 80px; }\n" +
                "        .signature-box { display: flex; flex-direction: column; justify-content: flex-end; align-items: center; border-top: 1px solid #9ca3af; height: 80px; font-size: 12px; color: #6b7280; margin-top: 35px; }\n" +
                "        .footer { text-align: center; font-size: 11px; color: #9ca3af; margin-top: 40px; border-top: 1px solid #f3f4f6; padding-top: 15px; }\n" +
                "        @media print {\n" +
                "            body { background: #ffffff; padding: 0; }\n" +
                "            .report-container { box-shadow: none; border: none; padding: 0; }\n" +
                "            button { display: none; }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='report-container'>\n" +
                "        <div class='header-section'>\n" +
                "            <div class='institution-details'>\n" +
                "                <h1>METROPOLITAN ACADEMY</h1>\n" +
                "                <p>Central Institute of Analytics and Academics</p>\n" +
                "            </div>\n" +
                "            <div class='report-title'>\n" +
                "                <h2>OFFICIAL GRADE CARD</h2>\n" +
                "                <p>Generated: " + printDate + "</p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='info-grid'>\n" +
                "            <div class='info-item'><span>Student Name</span><strong>" + student.getName() + "</strong></div>\n" +
                "            <div class='info-item'><span>Student ID</span><strong>" + studentId + "</strong></div>\n" +
                "            <div class='info-item'><span>Roll Number</span><strong>" + student.getRollNumber() + "</strong></div>\n" +
                "            <div class='info-item'><span>Class / Grade</span><strong>" + student.getStudentClass() + "</strong></div>\n" +
                "            <div class='info-item'><span>Section</span><strong>" + student.getSection() + "</strong></div>\n" +
                "            <div class='info-item'><span>Contact Email</span><strong>" + student.getEmail() + "</strong></div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='summary-banner'>\n" +
                "            <div class='summary-card'>\n" +
                "                <span>Average Percentage</span>\n" +
                "                <h3>" + String.format(Locale.US, "%.1f%%", avgPercent) + "</h3>\n" +
                "            </div>\n" +
                "            <div class='summary-card'>\n" +
                "                <span>Cumulative GPA</span>\n" +
                "                <h3>" + String.format(Locale.US, "%.2f", cgpa) + "</h3>\n" +
                "            </div>\n" +
                "            <div class='summary-card accent'>\n" +
                "                <span>Attendance Rate</span>\n" +
                "                <h3>" + String.format(Locale.US, "%.1f%%", attendanceRate) + "</h3>\n" +
                "            </div>\n" +
                "            <div class='summary-card'>\n" +
                "                <span>Class Rank</span>\n" +
                "                <h3>#" + classRank + "</h3>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='section-title'>ACADEMIC PERFORMANCE LOG</div>\n" +
                "        <table>\n" +
                "            <thead>\n" +
                "                <tr>\n" +
                "                    <th>Subject</th>\n" +
                "                    <th class='center'>Credits</th>\n" +
                "                    <th class='center'>Assignment (15%)</th>\n" +
                "                    <th class='center'>Quiz (15%)</th>\n" +
                "                    <th class='center'>Mid-Term (30%)</th>\n" +
                "                    <th class='center'>Final (30%)</th>\n" +
                "                    <th class='center'>Internal (10%)</th>\n" +
                "                    <th class='center'>Total (100)</th>\n" +
                "                    <th class='center'>Grade</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n" +
                "                " + marksRows.toString() + "\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "        \n" +
                "        <div class='section-title'>AI PERFORMANCE RECOMMENDATIONS</div>\n" +
                "        <div class='ai-recommendations'>\n" +
                "            " + aiBlock.toString() + "\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='remarks-signatures'>\n" +
                "            <div>\n" +
                "                <div class='bold' style='margin-bottom: 8px;'>Class Teacher Remarks:</div>\n" +
                "                <div class='remarks-box'>\n" +
                "                    " + (avgPercent >= 75 ? 
                                       "Demonstrates strong capability and comprehension. Keep up the consistent effort and active classroom engagement." : 
                                       "Needs targeted support in weaker subjects. Attendance must be maintained and suggestions by the AI Recommendation system should be closely followed.") + "\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div>\n" +
                "                <div class='signature-box'>\n" +
                "                    Class Advisor Signature\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='footer'>\n" +
                "            <p>Metropolitan Academy Student Grade Management System • Confidential Educational Record</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
