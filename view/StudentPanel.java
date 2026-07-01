package view;

import service.StudentService;
import service.SubjectService;
import model.Student;
import util.UIComponents;
import util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class StudentPanel extends JPanel {
    private final StudentService studentService;
    private final SubjectService subjectService;

    private JTable tblStudents;
    private DefaultTableModel modelStudents;
    private UIComponents.CustomTextField txtSearch;
    private JComboBox<String> cbClass;
    private JComboBox<String> cbSection;

    // Detailed side panel components
    private JLabel lblDetId, lblDetName, lblDetRoll, lblDetClass, lblDetEmail, lblDetPhone, lblDetDOB, lblDetAge, lblDetAddress;

    public StudentPanel(StudentService studentService, SubjectService subjectService) {
        this.studentService = studentService;
        this.subjectService = subjectService;

        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        initComponents();
        loadStudents();
    }

    private void initComponents() {
        // --- 1. Top Controls Bar ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topBar.setOpaque(false);

        txtSearch = new UIComponents.CustomTextField("Search name, ID, roll...");
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.addActionListener(this::handleFilter);
        topBar.add(txtSearch);

        JLabel lblClass = new JLabel("Class:");
        lblClass.setFont(UIComponents.FONT_BOLD);
        lblClass.setForeground(UIComponents.COLOR_TEXT_SEC);
        topBar.add(lblClass);

        cbClass = new JComboBox<>(new String[]{"All", "Class 10", "Class 11", "Class 12"});
        cbClass.setFont(UIComponents.FONT_BODY);
        cbClass.setBackground(UIComponents.COLOR_CARD);
        cbClass.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbClass.addActionListener(this::handleFilter);
        topBar.add(cbClass);

        JLabel lblSec = new JLabel("Section:");
        lblSec.setFont(UIComponents.FONT_BOLD);
        lblSec.setForeground(UIComponents.COLOR_TEXT_SEC);
        topBar.add(lblSec);

        cbSection = new JComboBox<>(new String[]{"All", "A", "B", "C"});
        cbSection.setFont(UIComponents.FONT_BODY);
        cbSection.setBackground(UIComponents.COLOR_CARD);
        cbSection.setForeground(UIComponents.COLOR_TEXT_MAIN);
        cbSection.addActionListener(this::handleFilter);
        topBar.add(cbSection);

        UIComponents.CustomButton btnSearch = new UIComponents.CustomButton("Apply Filters", UIComponents.COLOR_CARD_LIGHT, UIComponents.COLOR_CARD_LIGHT.brighter());
        btnSearch.addActionListener(this::handleFilter);
        topBar.add(btnSearch);

        add(topBar, BorderLayout.NORTH);

        // --- 2. Center Panel: Student Table Grid ---
        String[] cols = {"Student ID", "Name", "Roll No", "Class", "Section", "Email"};
        modelStudents = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStudents = new JTable(modelStudents);
        UIComponents.styleTable(tblStudents);
        tblStudents.getSelectionModel().addListSelectionListener(e -> handleStudentSelected());

        JScrollPane tableScroll = new JScrollPane(tblStudents);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(UIComponents.COLOR_CARD);
        
        JPanel tableContainer = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        // --- 3. Right Panel: Details view & Actions ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(340, getHeight()));

        // Profile Details Card
        JPanel detailsCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        detailsCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));

        JLabel lblDetTitle = new JLabel("Student Profile");
        lblDetTitle.setFont(UIComponents.FONT_SUBTITLE);
        lblDetTitle.setForeground(UIComponents.COLOR_TEXT_MAIN);
        lblDetTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDetTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        detailsCard.add(lblDetTitle);

        lblDetId = createDetailLabel("Student ID", "Select a student");
        lblDetName = createDetailLabel("Name", "-");
        lblDetRoll = createDetailLabel("Roll Number", "-");
        lblDetClass = createDetailLabel("Class / Section", "-");
        lblDetEmail = createDetailLabel("Email", "-");
        lblDetPhone = createDetailLabel("Phone Number", "-");
        lblDetDOB = createDetailLabel("Date of Birth", "-");
        lblDetAge = createDetailLabel("Age (Years)", "-");
        lblDetAddress = createDetailLabel("Address", "-");

        detailsCard.add(lblDetId);
        detailsCard.add(lblDetName);
        detailsCard.add(lblDetRoll);
        detailsCard.add(lblDetClass);
        detailsCard.add(lblDetEmail);
        detailsCard.add(lblDetPhone);
        detailsCard.add(lblDetDOB);
        detailsCard.add(lblDetAge);
        detailsCard.add(lblDetAddress);
        detailsCard.add(Box.createVerticalGlue());

        // CRUD Action Buttons
        JPanel actionsCard = new UIComponents.RoundedPanel(12, UIComponents.COLOR_CARD);
        actionsCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        actionsCard.setLayout(new GridLayout(3, 1, 0, 10));

        UIComponents.CustomButton btnAdd = new UIComponents.CustomButton("Add New Student", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnAdd.addActionListener(e -> showStudentForm(null));
        
        UIComponents.CustomButton btnEdit = new UIComponents.CustomButton("Edit Student Profile", UIComponents.COLOR_ACCENT, UIComponents.COLOR_ACCENT.brighter());
        btnEdit.addActionListener(e -> {
            Student s = getSelectedStudent();
            if (s != null) showStudentForm(s);
        });

        UIComponents.CustomButton btnDelete = new UIComponents.CustomButton("Delete Record", UIComponents.COLOR_DANGER, UIComponents.COLOR_DANGER.brighter());
        btnDelete.addActionListener(e -> handleDeleteStudent());

        actionsCard.add(btnAdd);
        actionsCard.add(btnEdit);
        actionsCard.add(btnDelete);

        rightPanel.add(detailsCard, BorderLayout.CENTER);
        rightPanel.add(actionsCard, BorderLayout.SOUTH);

        // Assemble Panels
        add(tableContainer, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JLabel createDetailLabel(String title, String val) {
        JLabel label = new JLabel("<html><body style='width: 200px;'><span style='font-size:10px; font-weight:600; color:#94a3b8; text-transform:uppercase; letter-spacing:0.05em;'>" + title + "</span><br/><strong style='font-size:12px; color:#f8fafc;'>" + val + "</strong><br/><br/></body></html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void updateDetailLabel(JLabel label, String title, String val) {
        label.setText("<html><body style='width: 200px;'><span style='font-size:10px; font-weight:600; color:#94a3b8; text-transform:uppercase; letter-spacing:0.05em;'>" + title + "</span><br/><strong style='font-size:12px; color:#f8fafc;'>" + val + "</strong><br/><br/></body></html>");
    }

    private void loadStudents() {
        modelStudents.setRowCount(0);
        String query = txtSearch.getText().trim();
        String c = (String) cbClass.getSelectedItem();
        String s = (String) cbSection.getSelectedItem();

        List<Student> list = studentService.filterStudents(query, c, s);
        for (Student stu : list) {
            modelStudents.addRow(new Object[]{
                    stu.getStudentId(),
                    stu.getName(),
                    stu.getRollNumber(),
                    stu.getStudentClass(),
                    stu.getSection(),
                    stu.getEmail()
            });
        }
        clearDetails();
    }

    private Student getSelectedStudent() {
        int row = tblStudents.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first", "Select Student", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = (String) tblStudents.getValueAt(row, 0);
        return studentService.getStudentById(id);
    }

    private void handleStudentSelected() {
        int row = tblStudents.getSelectedRow();
        if (row == -1) {
            clearDetails();
            return;
        }
        String id = (String) tblStudents.getValueAt(row, 0);
        Student s = studentService.getStudentById(id);
        if (s != null) {
            updateDetailLabel(lblDetId, "Student ID", s.getStudentId());
            updateDetailLabel(lblDetName, "Name", s.getName());
            updateDetailLabel(lblDetRoll, "Roll Number", s.getRollNumber());
            updateDetailLabel(lblDetClass, "Class / Section", s.getStudentClass() + " - " + s.getSection());
            updateDetailLabel(lblDetEmail, "Email Address", s.getEmail());
            updateDetailLabel(lblDetPhone, "Phone Number", s.getPhoneNumber());
            updateDetailLabel(lblDetDOB, "Date of Birth", s.getDateOfBirth());
            
            int age = DateUtil.calculateAge(s.getDateOfBirth());
            updateDetailLabel(lblDetAge, "Age (Years)", age == -1 ? "N/A" : String.valueOf(age));
            updateDetailLabel(lblDetAddress, "Address", s.getAddress());
        }
    }

    private void clearDetails() {
        updateDetailLabel(lblDetId, "Student ID", "Select a student");
        updateDetailLabel(lblDetName, "Name", "-");
        updateDetailLabel(lblDetRoll, "Roll Number", "-");
        updateDetailLabel(lblDetClass, "Class / Section", "-");
        updateDetailLabel(lblDetEmail, "Email", "-");
        updateDetailLabel(lblDetPhone, "Phone Number", "-");
        updateDetailLabel(lblDetDOB, "Date of Birth", "-");
        updateDetailLabel(lblDetAge, "Age (Years)", "-");
        updateDetailLabel(lblDetAddress, "Address", "-");
    }

    private void handleFilter(ActionEvent e) {
        loadStudents();
    }

    private void handleDeleteStudent() {
        Student s = getSelectedStudent();
        if (s == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student: " + s.getName() + "?\nThis will cascade delete all grades, enrollments, credentials, and attendance!",
                "Confirm Cascading Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            studentService.deleteStudent(s.getStudentId());
            loadStudents();
        }
    }

    private void showStudentForm(Student editTarget) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, editTarget == null ? "Add Student" : "Edit Student Details", true);
        dialog.setSize(480, 600);
        dialog.setLocationRelativeTo(parent);
        
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(UIComponents.COLOR_CARD);
        container.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.add(container);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        UIComponents.CustomTextField txtFormName = new UIComponents.CustomTextField("Name");
        UIComponents.CustomTextField txtFormRoll = new UIComponents.CustomTextField("Roll Number");
        
        JComboBox<String> cbFormClass = new JComboBox<>(new String[]{"Class 10", "Class 11", "Class 12"});
        cbFormClass.setFont(UIComponents.FONT_BODY);
        cbFormClass.setBackground(UIComponents.COLOR_CARD_LIGHT);
        cbFormClass.setForeground(UIComponents.COLOR_TEXT_MAIN);
        
        JComboBox<String> cbFormSec = new JComboBox<>(new String[]{"A", "B", "C"});
        cbFormSec.setFont(UIComponents.FONT_BODY);
        cbFormSec.setBackground(UIComponents.COLOR_CARD_LIGHT);
        cbFormSec.setForeground(UIComponents.COLOR_TEXT_MAIN);

        UIComponents.CustomTextField txtFormEmail = new UIComponents.CustomTextField("Email");
        UIComponents.CustomTextField txtFormPhone = new UIComponents.CustomTextField("Phone (e.g. 555-0199)");
        UIComponents.CustomTextField txtFormDOB = new UIComponents.CustomTextField("DOB (yyyy-mm-dd)");
        UIComponents.CustomTextField txtFormAddr = new UIComponents.CustomTextField("Address");

        // Load values if editing
        if (editTarget != null) {
            txtFormName.setText(editTarget.getName());
            txtFormRoll.setText(editTarget.getRollNumber());
            cbFormClass.setSelectedItem(editTarget.getStudentClass());
            cbFormSec.setSelectedItem(editTarget.getSection());
            txtFormEmail.setText(editTarget.getEmail());
            txtFormPhone.setText(editTarget.getPhoneNumber());
            txtFormDOB.setText(editTarget.getDateOfBirth());
            txtFormAddr.setText(editTarget.getAddress());
        }

        // Layout items
        int rowIdx = 0;
        addFormItem(container, "Student Name", txtFormName, gbc, rowIdx++);
        addFormItem(container, "Roll Number", txtFormRoll, gbc, rowIdx++);
        addFormItem(container, "Class", cbFormClass, gbc, rowIdx++);
        addFormItem(container, "Section", cbFormSec, gbc, rowIdx++);
        addFormItem(container, "Email Address", txtFormEmail, gbc, rowIdx++);
        addFormItem(container, "Phone Number", txtFormPhone, gbc, rowIdx++);
        addFormItem(container, "Date of Birth", txtFormDOB, gbc, rowIdx++);
        addFormItem(container, "Address", txtFormAddr, gbc, rowIdx++);

        // Save Button
        UIComponents.CustomButton btnSave = new UIComponents.CustomButton("Save Details", UIComponents.COLOR_SUCCESS, UIComponents.COLOR_SUCCESS.brighter());
        btnSave.addActionListener(e -> {
            String name = txtFormName.getText().trim();
            String roll = txtFormRoll.getText().trim();
            String cls = (String) cbFormClass.getSelectedItem();
            String sec = (String) cbFormSec.getSelectedItem();
            String email = txtFormEmail.getText().trim();
            String phone = txtFormPhone.getText().trim();
            String dob = txtFormDOB.getText().trim();
            String addr = txtFormAddr.getText().trim();

            if (name.isEmpty() || roll.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.isEmpty() || addr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!DateUtil.isValidDate(dob)) {
                JOptionPane.showMessageDialog(dialog, "Invalid Birth Date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editTarget == null) {
                // Add
                studentService.addStudent(name, roll, cls, sec, email, phone, dob, addr);
            } else {
                // Update
                editTarget.setName(name);
                editTarget.setRollNumber(roll);
                editTarget.setStudentClass(cls);
                editTarget.setSection(sec);
                editTarget.setEmail(email);
                editTarget.setPhoneNumber(phone);
                editTarget.setDateOfBirth(dob);
                editTarget.setAddress(addr);
                studentService.updateStudent(editTarget);
            }

            dialog.dispose();
            loadStudents();
        });

        gbc.gridx = 0; gbc.gridy = rowIdx; gbc.gridwidth = 2;
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
