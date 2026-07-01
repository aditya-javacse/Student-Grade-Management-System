# ðŸŽ“ AI-Powered Smart Student Performance Analytics System

<p align="center">

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![Swing](https://img.shields.io/badge/Java%20Swing-Desktop-blue?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-MVC-success?style=for-the-badge)
![Persistence](https://img.shields.io/badge/Database-CSV%20Flat%20Files-green?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)

</p>

A modern **institutional-grade desktop application** built entirely with **Core Java** and **Java Swing** for managing student performance, attendance, grading, analytics, and academic reporting.

The project follows professional software engineering principles including **MVC Architecture**, **DAO Pattern**, **Role-Based Access Control (RBAC)**, **SHA-256 Authentication**, **multithreading**, and **custom Graphics2D visual analytics** without relying on external libraries.

---

# âœ¨ Highlights

- ðŸ” Secure Role-Based Authentication (Admin, Teacher, Student)
- ðŸ“Š Real-time Academic Analytics Dashboard
- ðŸ“ˆ Interactive Graphics2D Charts
- ðŸ¤– AI-Based Study Recommendations
- ðŸŽ¯ Automatic GPA & Grade Calculation
- ðŸ† Student & Subject Ranking System
- ðŸ“… Attendance Tracking & Monitoring
- ðŸ“„ HTML Report Card Generator
- ðŸ“‘ CSV Report Export
- âš¡ Multithreaded Report Processing
- ðŸ”’ SHA-256 Password Encryption
- ðŸ’¾ Native CSV Database (No SQL Required)
- ðŸ§© Clean MVC + DAO Architecture

---

# ðŸ–¼ï¸ Application Screenshots

> Replace the image paths below with your own Screenshots.

## ðŸ” Login Screen

<p align="center">
<img src="Screenshots/login.png" width="800">
</p>

---

## ðŸ“Š Admin Dashboard

<p align="center">
<img src="Screenshots/admin-dashboard.png" width="800">
</p>

---

## ðŸ‘¨â€ðŸ« Teacher Dashboard

<p align="center">
<img src="Screenshots/teacher-dashboard.png" width="800">
</p>

---

## ðŸŽ“ Student Dashboard

<p align="center">
<img src="Screenshots/student-dashboard.png" width="800">
</p>

---

## ðŸ“ˆ Analytics Dashboard

<p align="center">
<img src="Screenshots/analytics.png" width="800">
</p>

---

## ðŸ“‰ Performance Charts

<p align="center">
<img src="Screenshots/charts.png" width="800">
</p>

---

## ðŸ“ Marks Management

<p align="center">
<img src="Screenshots/marks.png" width="800">
</p>

---

## ðŸ“… Attendance Management

<p align="center">
<img src="Screenshots/attendance.png" width="800">
</p>

---

## ðŸ¤– AI Study Recommendations

<p align="center">
<img src="Screenshots/ai-recommendation.png" width="800">
</p>

---

## ðŸ“„ Generated HTML Report Card

<p align="center">
<img src="Screenshots/report-card.png" width="800">
</p>

---

# ðŸš€ Features

## ðŸ” Authentication & Security

- Role-Based Access Control (RBAC)
- Secure login system
- SHA-256 password hashing
- Session management
- Activity logging
- Password recovery using security questions

---

## ðŸ‘¨â€ðŸŽ“ Student Management

- Student CRUD
- Auto-generated Student IDs
- Automatic portal account creation
- Student search & filtering
- Cascade deletion support

---

## ðŸ“š Subject Management

- Subject CRUD
- Automatic Subject IDs
- Enrollment management
- Student-Subject junction table
- Cascade cleanup

---

## ðŸ“ Assessment & Grading

### Weighted Assessment System

| Component | Weight |
|-----------|---------|
| Assignments | 15% |
| Quizzes | 15% |
| Mid-Term | 30% |
| Final Exam | 30% |
| Internal Evaluation | 10% |

Features

- Automatic percentage calculation
- Letter grade generation
- GPA calculation
- Class ranking
- Subject ranking
- Performance trends

---

## ðŸŽ“ GPA Scale

| Grade | GPA |
|------|------|
| A+ | 4.0 |
| A | 3.7 |
| B | 3.0 |
| C | 2.0 |
| D | 1.0 |
| F | 0.0 |

---

## ðŸ“… Attendance System

- Daily attendance
- Present / Absent / Late
- Attendance percentage
- Warning notifications
- Attendance history

---

## ðŸ“Š Analytics Dashboard

Custom Graphics2D visualizations include:

- ðŸ“Š Bar Charts
- ðŸ“ˆ Line Graphs
- ðŸ© Donut Charts

Analytics include:

- Subject averages
- Grade distributions
- Student comparisons
- Performance trends
- Class statistics

---

## ðŸ¤– AI Performance Analysis

The built-in AI module automatically analyzes student performance and provides intelligent recommendations.

### Features

- Detects weak subjects
- Identifies poor-performing assessments
- Predicts final grades
- Personalized study advice
- Homework completion suggestions
- Test preparation recommendations

---

## ðŸ“„ Report Generation

Runs on background threads to ensure the Swing interface remains responsive.

### CSV Reports

- Student Grades
- GPA Reports
- Subject Statistics
- Attendance Reports

### HTML Report Cards

Professional printable report cards containing:

- Student Profile
- Grades
- GPA
- Attendance
- Charts
- AI Recommendations
- Signature Blocks

---

# ðŸ—ï¸ Project Architecture

```
                +-------------------+
                |       View        |
                |   Java Swing UI   |
                +---------+---------+
                          |
                          |
                +---------v---------+
                |     Services      |
                | Business Logic    |
                +---------+---------+
                          |
                +---------v---------+
                |        DAO        |
                | CSV File Storage  |
                +---------+---------+
                          |
                +---------v---------+
                |      Models       |
                |      POJOs         |
                +-------------------+
```

Architecture Patterns

- MVC
- DAO
- Service Layer
- Object-Oriented Design
- Layered Architecture

---

# ðŸ“‚ Project Structure

```text
Student-Grade-Management-System/
â”‚
â”œâ”€â”€ analytics/
â”œâ”€â”€ dao/
â”œâ”€â”€ data/
â”œâ”€â”€ model/
â”œâ”€â”€ reports/
â”œâ”€â”€ service/
â”œâ”€â”€ util/
â”œâ”€â”€ view/
â”‚
â”œâ”€â”€ Screenshots/
â”‚   â”œâ”€â”€ login.png
â”‚   â”œâ”€â”€ admin-dashboard.png
â”‚   â”œâ”€â”€ teacher-dashboard.png
â”‚   â”œâ”€â”€ student-dashboard.png
â”‚   â”œâ”€â”€ analytics.png
â”‚   â”œâ”€â”€ charts.png
â”‚   â”œâ”€â”€ attendance.png
â”‚   â”œâ”€â”€ marks.png
â”‚   â”œâ”€â”€ ai-recommendation.png
â”‚   â””â”€â”€ report-card.png
â”‚
â”œâ”€â”€ Main.java
â”œâ”€â”€ run.ps1
â”œâ”€â”€ run.bat
â””â”€â”€ README.md
```

---

# âš™ï¸ Requirements

- Java JDK 17+
- Windows 10/11
- PowerShell or Command Prompt

Tested on

- OpenJDK 26

---

# â–¶ï¸ Running the Project

## PowerShell (Recommended)

```powershell
.\run.ps1
```

If PowerShell blocks execution:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

---

## Command Prompt

```cmd
run.bat
```

Or simply double-click **run.bat**.

---

# ðŸ”‘ Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| ðŸ‘‘ Admin | admin | admin123 |
| ðŸ‘¨â€ðŸ« Teacher | teacher | teacher123 |
| ðŸŽ“ Student | stu1001 | student123 |

All student accounts (`stu1001` â†’ `stu1015`) use:

```
Password: student123
```

---

# ðŸ§ª Automated Testing

The project includes a built-in unit testing framework.

Execute:

```powershell
Get-ChildItem -Recurse -Filter *.java |
Resolve-Path -Relative |
Out-File -Encoding ascii sources.txt

& "C:\Users\Computer solution\.jdks\openjdk-26.0.1\bin\javac.exe" `
-encoding UTF-8 -d bin "@sources.txt"

Remove-Item sources.txt

& "C:\Users\Computer solution\.jdks\openjdk-26.0.1\bin\java.exe" `
-cp bin util.TestRunner
```

Expected Output

```
TEST RUN COMPLETE

30 / 30 ASSERTS PASSED

SUCCESS
```

---

# ðŸ’¡ Technologies Used

- Java
- Java Swing
- Graphics2D
- Core Java
- MVC Architecture
- DAO Pattern
- CSV File Storage
- SHA-256
- Multithreading
- HTML/CSS
- Object-Oriented Programming

---

# ðŸŽ¯ Future Enhancements

- ðŸ“± Mobile Companion App
- â˜ Cloud Database Support
- ðŸ“§ Email Notifications
- ðŸ“Š PDF Report Generation
- ðŸ” Advanced Search Filters
- ðŸŒ™ Dark Mode
- ðŸŒ REST API Integration
- ðŸ”” Live Notifications
- ðŸ“ˆ Machine Learning Grade Prediction

---

# â­ Project Showcase

This project demonstrates practical implementation of:

- Enterprise Desktop Development
- MVC Architecture
- DAO Pattern
- File-Based Database Systems
- Data Visualization
- AI-Based Analytics
- Java Multithreading
- Authentication & Security
- Professional UI Development
- Academic Performance Analytics

---

<p align="center">

### â­ If you found this project helpful, consider giving it a star!

**Made with â¤ï¸ using Java & Java Swing**

</p>