import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class HealthTrackerApp {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<HealthRecord> records;

    public HealthTrackerApp() {
        records = new ArrayList<>();

        // Setup frame
        frame = new JFrame("Aplikasi Catatan Kesehatan Harian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background

        // Setup table
        String[] columns = {"Tanggal", "Jenis Aktivitas", "Durasi (menit)", "Catatan", "Gambar"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {
                    return ImageIcon.class;
                }
                return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(100); // Set row height to accommodate image size
        table.setBackground(new Color(255, 250, 240)); // Light cream background
        table.setForeground(new Color(60, 60, 60)); // Dark text color

        // Customize table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(173, 216, 230)); // Light blue
        header.setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Panel button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 255, 240)); // Light green background
        buttonPanel.setBorder(BorderFactory.createLineBorder(new Color(173, 216, 230), 2)); // Add border
        JButton addButton = new JButton("Tambah");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Hapus");

        // Button styles
        addButton.setBackground(new Color(173, 216, 230)); // Light blue
        addButton.setForeground(Color.BLACK);
        editButton.setBackground(new Color(255, 228, 181)); // Light orange
        editButton.setForeground(Color.BLACK);
        deleteButton.setBackground(new Color(255, 182, 193)); // Light pink
        deleteButton.setForeground(Color.BLACK);

        // Add tooltips
        addButton.setToolTipText("Klik untuk menambahkan catatan baru");
        editButton.setToolTipText("Klik untuk mengedit catatan yang dipilih");
        deleteButton.setToolTipText("Klik untuk menghapus catatan yang dipilih");

        // Add hover effects
        addHoverEffect(addButton, new Color(135, 206, 250), new Color(173, 216, 230));
        addHoverEffect(editButton, new Color(255, 200, 150), new Color(255, 228, 181));
        addHoverEffect(deleteButton, new Color(255, 160, 170), new Color(255, 182, 193));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button listeners
        addButton.addActionListener(e -> openForm(null));
        editButton.addActionListener(e -> editSelectedRecord());
        deleteButton.addActionListener(e -> deleteSelectedRecord());

        frame.setVisible(true);
    }

    private void openForm(HealthRecord recordToEdit) {
        JDialog dialog = new JDialog(frame, "Form Catatan Kesehatan", true);
        dialog.setSize(400, 500);
        dialog.setLayout(new GridLayout(0, 1)); // Ensure all fields are vertically aligned
        dialog.getContentPane().setBackground(new Color(240, 255, 240)); // Light green background

        // Form fields
        JTextField dateField = new JTextField(recordToEdit != null ? recordToEdit.getDate() : "");
        JTextField activityField = new JTextField(recordToEdit != null ? recordToEdit.getActivity() : "");
        JTextField durationField = new JTextField(recordToEdit != null ? String.valueOf(recordToEdit.getDuration()) : "");
        JTextField notesField = new JTextField(recordToEdit != null ? recordToEdit.getNotes() : "");
        JLabel imageLabel = new JLabel("Tidak ada gambar");
        JButton chooseImageButton = new JButton("Pilih Gambar");

        // Set component styles
        dateField.setBackground(new Color(255, 250, 205)); // Light yellow
        activityField.setBackground(new Color(255, 250, 205));
        durationField.setBackground(new Color(255, 250, 205));
        notesField.setBackground(new Color(255, 250, 205));
        imageLabel.setForeground(new Color(70, 130, 180)); // Steel blue
        chooseImageButton.setBackground(new Color(173, 216, 230)); // Light blue

        // Add components to form
        dialog.add(new JLabel("Tanggal (YYYY-MM-DD):"));
        dialog.add(dateField);
        dialog.add(new JLabel("Jenis Aktivitas:"));
        dialog.add(activityField);
        dialog.add(new JLabel("Durasi (menit):"));
        dialog.add(durationField);
        dialog.add(new JLabel("Catatan:"));
        dialog.add(notesField);
        dialog.add(new JLabel("Gambar:"));
        dialog.add(imageLabel);
        dialog.add(chooseImageButton);

        final String[] imagePath = {recordToEdit != null ? recordToEdit.getImagePath() : null};
        if (imagePath[0] != null) {
            imageLabel.setText(imagePath[0]);
        }

        chooseImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                imagePath[0] = selectedFile.getAbsolutePath();
                imageLabel.setText(selectedFile.getName());
            }
        });

        JButton saveButton = new JButton("Simpan");
        saveButton.setBackground(new Color(144, 238, 144)); // Light green
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            try {
                String date = dateField.getText().trim();
                String activity = activityField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());
                String notes = notesField.getText().trim();

                if (date.isEmpty() || activity.isEmpty()) {
                    throw new IllegalArgumentException("Tanggal dan jenis aktivitas tidak boleh kosong.");
                }

                if (recordToEdit == null) {
                    HealthRecord newRecord = new HealthRecord(date, activity, duration, notes, imagePath[0]);
                    records.add(newRecord);
                    addRecordToTable(newRecord);
                } else {
                    recordToEdit.setDate(date);
                    recordToEdit.setActivity(activity);
                    recordToEdit.setDuration(duration);
                    recordToEdit.setNotes(notes);
                    recordToEdit.setImagePath(imagePath[0]);
                    updateTable();
                }

                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Durasi harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void addRecordToTable(HealthRecord record) {
        ImageIcon icon = null;
        if (record.getImagePath() != null) {
            ImageIcon originalIcon = new ImageIcon(record.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);
        }
        tableModel.addRow(new Object[]{record.getDate(), record.getActivity(), record.getDuration(), record.getNotes(), icon});
    }

    private void editSelectedRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            HealthRecord recordToEdit = records.get(selectedRow);
            openForm(recordToEdit);
        } else {
            JOptionPane.showMessageDialog(frame, "Pilih catatan untuk diedit.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            records.remove(selectedRow);
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(frame, "Pilih catatan untuk dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0); // Clear table
        for (HealthRecord record : records) {
            addRecordToTable(record);
        }
    }

    private void addHoverEffect(JButton button, Color hoverColor, Color defaultColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HealthTrackerApp::new);
    }
}

class HealthRecord {
    private String date;
    private String activity;
    private int duration;
    private String notes;
    private String imagePath;

    public HealthRecord(String date, String activity, int duration, String notes, String imagePath) {
        this.date = date;
        this.activity = activity;
        this.duration = duration;
        this.notes = notes;
        this.imagePath = imagePath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
}
}