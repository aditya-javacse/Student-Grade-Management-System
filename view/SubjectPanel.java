package view;

import service.SubjectService;
import service.StudentService;
import model.Subject;
import model.Student;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SubjectPanel extends JPanel {
    private final SubjectService subjectService;
    private final StudentService studentService;

    private JTable tblSubjects;
    private DefaultTableModel modelSubjects;

    private JList<String> listEnrolledStudents;
    private DefaultListModel<String> modelEnrolledStudents;
    
    private JLabel lblSelectedSubject;

    public SubjectPanel(SubjectService subjectService, StudentService studentService) {
        this.subjectService = subjectService;
        this.studentService = studentService;

        setLayout(new GridLayout(1, 2, 20, 0));
        setOpaque(false);

        initComponents();
        loadSubjects();
    }

    private void initComponents() {
        // --- Left Panel: Subjects Manager Table ---
        JPanel leftPanel = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        leftPanel.setLayout(new BorderLayout(0, 15));

        JLabel lblTitleLeft = new JLabel("Curriculum Subjects");
        lblTitleLeft.setFont(UIComponents.FONT_SUBTITLE);
        lblTitleLeft.setForeground(UIComponents.COLOR_TEXT_MAIN);
        leftPanel.add(lblTitleLeft, BorderLayout.NORTH);

        String[] cols = {"Subject ID", "Name", "Code", "Credits"};
        modelSubjects = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSubjects = new JTable(modelSubjects);
        UIComponents.styleTable(tblSubjects);
        tblSubjects.getSelectionModel().addListSelectionListener(e -> handleSubjectSelected());

        JScrollPane scroll = new JScrollPane(tblSubjects);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.COLOR_CARD);
        leftPanel.add(scroll, BorderLayout.CENTER);

        // CRUD buttons for subjects
        JPanel btnsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnsPanel.setOpaque(false);

        UIComponents.CustomButton btnAdd = new UIComponents.CustomButton("Add", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnAdd.addActionListener(e -> showSubjectForm(null));

        UIComponents.CustomButton btnEdit = new UIComponents.CustomButton("Edit", UIComponents.COLOR_ACCENT, UIComponents.COLOR_ACCENT.brighter());
        btnEdit.addActionListener(e -> {
            Subject s = getSelectedSubject();
            if (s != null) showSubjectForm(s);
        });

        UIComponents.CustomButton btnDelete = new UIComponents.CustomButton("Delete", UIComponents.COLOR_DANGER, UIComponents.COLOR_DANGER.brighter());
        btnDelete.addActionListener(e -> handleDeleteSubject());

        btnsPanel.add(btnAdd);
        btnsPanel.add(btnEdit);
        btnsPanel.add(btnDelete);
        leftPanel.add(btnsPanel, BorderLayout.SOUTH);

        // --- Right Panel: Enrollment Settings ---
        JPanel rightPanel = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        rightPanel.setLayout(new BorderLayout(0, 15));

        lblSelectedSubject = new JLabel("Select a Subject to manage enrollments");
        lblSelectedSubject.setFont(UIComponents.FONT_SUBTITLE);
        lblSelectedSubject.setForeground(UIComponents.COLOR_TEXT_MAIN);
        rightPanel.add(lblSelectedSubject, BorderLayout.NORTH);

        modelEnrolledStudents = new DefaultListModel<>();
        listEnrolledStudents = new JList<>(modelEnrolledStudents);
        listEnrolledStudents.setBackground(new Color(23, 33, 48));
        listEnrolledStudents.setForeground(UIComponents.COLOR_TEXT_MAIN);
        listEnrolledStudents.setFont(UIComponents.FONT_BODY);
        listEnrolledStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listEnrolledStudents.setBorder(BorderFactory.createLineBorder(UIComponents.COLOR_BORDER));

        JScrollPane enrolledScroll = new JScrollPane(listEnrolledStudents);
        enrolledScroll.setBorder(null);
        rightPanel.add(enrolledScroll, BorderLayout.CENTER);

        // Enrollment Action Buttons
        JPanel enrollActions = new JPanel(new GridLayout(1, 2, 10, 0));
        enrollActions.setOpaque(false);

        UIComponents.CustomButton btnAssign = new UIComponents.CustomButton("Enroll Student", UIComponents.COLOR_ACCENT, UIComponents.COLOR_ACCENT.brighter());
        btnAssign.addActionListener(this::handleEnrollStudent);

        UIComponents.CustomButton btnUnassign = new UIComponents.CustomButton("Unenroll Student", UIComponents.COLOR_DANGER, UIComponents.COLOR_DANGER.brighter());
        btnUnassign.addActionListener(this::handleUnenrollStudent);

        enrollActions.add(btnAssign);
        enrollActions.add(btnUnassign);
        rightPanel.add(enrollActions, BorderLayout.SOUTH);

        // Add panels to Grid Layout
        add(leftPanel);
        add(rightPanel);
    }

    private void loadSubjects() {
        modelSubjects.setRowCount(0);
        for (Subject s : subjectService.getAllSubjects()) {
            modelSubjects.addRow(new Object[]{
                    s.getSubjectId(),
                    s.getName(),
                    s.getCode(),
                    s.getCreditHours()
            });
        }
        lblSelectedSubject.setText("Select a Subject to manage enrollments");
        modelEnrolledStudents.clear();
    }

    private Subject getSelectedSubject() {
        int row = tblSubjects.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table first", "Select Subject", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = (String) tblSubjects.getValueAt(row, 0);
        return subjectService.getSubjectById(id);
    }

    private void handleSubjectSelected() {
        int row = tblSubjects.getSelectedRow();
        if (row == -1) return;

        String id = (String) tblSubjects.getValueAt(row, 0);
        Subject s = subjectService.getSubjectById(id);
        if (s == null) return;

        lblSelectedSubject.setText("Enrolled in: " + s.getName());
        loadEnrollments(s.getSubjectId());
    }

    private void loadEnrollments(String subjectId) {
        modelEnrolledStudents.clear();
        List<String> enrolledIds = new dao.SubjectDAO().getStudentsInSubject(subjectId);
        for (String stuId : enrolledIds) {
            Student student = studentService.getStudentById(stuId);
            if (student != null) {
                modelEnrolledStudents.addElement(student.getName() + " (" + stuId + ")");
            }
        }
    }

    private void handleDeleteSubject() {
        Subject s = getSelectedSubject();
        if (s == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete subject: " + s.getName() + "?\nAll grade mappings and student enrollments will be wiped cascadingly!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            subjectService.deleteSubject(s.getSubjectId());
            loadSubjects();
        }
    }

    private void handleEnrollStudent(ActionEvent e) {
        Subject s = getSelectedSubject();
        if (s == null) return;

        // Input ID dialog or combobox
        List<Student> allStudents = studentService.getAllStudents();
        if (allStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students registered in the database to enroll.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] choices = new String[allStudents.size()];
        for (int i = 0; i < allStudents.size(); i++) {
            choices[i] = allStudents.get(i).getName() + " (" + allStudents.get(i).getStudentId() + ")";
        }

        String selection = (String) JOptionPane.showInputDialog(this,
                "Select student to enroll:",
                "Subject Enrollment",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]);

        if (selection != null) {
            String stuId = selection.substring(selection.lastIndexOf('(') + 1, selection.lastIndexOf(')'));
            subjectService.assignSubjectToStudent(stuId, s.getSubjectId());
            loadEnrollments(s.getSubjectId());
        }
    }

    private void handleUnenrollStudent(ActionEvent e) {
        Subject s = getSelectedSubject();
        if (s == null) return;

        String selectedValue = listEnrolledStudents.getSelectedValue();
        if (selectedValue == null) {
            JOptionPane.showMessageDialog(this, "Select a student from the enrollment list first", "Select Student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stuId = selectedValue.substring(selectedValue.lastIndexOf('(') + 1, selectedValue.lastIndexOf(')'));

        int choice = JOptionPane.showConfirmDialog(this,
                "Unenroll this student? Any grades entered for this subject will be removed.",
                "Confirm Unenrollment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            subjectService.unassignSubjectFromStudent(stuId, s.getSubjectId());
            loadEnrollments(s.getSubjectId());
        }
    }

    private void showSubjectForm(Subject editTarget) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, editTarget == null ? "Add Subject" : "Edit Subject Details", true);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(parent);

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(UIComponents.COLOR_CARD);
        container.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.add(container);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        UIComponents.CustomTextField txtName = new UIComponents.CustomTextField("Subject Name");
        UIComponents.CustomTextField txtCode = new UIComponents.CustomTextField("Subject Code");
        UIComponents.CustomTextField txtCredits = new UIComponents.CustomTextField("Credit Hours (e.g. 3)");

        if (editTarget != null) {
            txtName.setText(editTarget.getName());
            txtCode.setText(editTarget.getCode());
            txtCredits.setText(String.valueOf(editTarget.getCreditHours()));
        }

        addFormItem(container, "Subject Name", txtName, gbc, 0);
        addFormItem(container, "Subject Code", txtCode, gbc, 1);
        addFormItem(container, "Credit Weight", txtCredits, gbc, 2);

        UIComponents.CustomButton btnSave = new UIComponents.CustomButton("Save Details", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String code = txtCode.getText().trim();
            String credStr = txtCredits.getText().trim();

            if (name.isEmpty() || code.isEmpty() || credStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int credits;
            try {
                credits = Integer.parseInt(credStr);
                if (credits <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Credit weight must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editTarget == null) {
                subjectService.addSubject(name, code, credits);
            } else {
                editTarget.setName(name);
                editTarget.setCode(code);
                editTarget.setCreditHours(credits);
                subjectService.updateSubject(editTarget);
            }

            dialog.dispose();
            loadSubjects();
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 8, 8);
        container.add(btnSave, gbc);

        dialog.setVisible(true);
    }

    private void addFormItem(JPanel p, String text, Component comp, GridBagConstraints gbc, int y) {
        JLabel label = new JLabel(text);
        label.setFont(UIComponents.FONT_BOLD);
        label.setForeground(UIComponents.COLOR_TEXT_SEC);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.3;
        p.add(label, gbc);

        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.7;
        p.add(comp, gbc);
    }
}
