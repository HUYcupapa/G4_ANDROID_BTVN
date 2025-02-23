package bt1_android;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class QLSV extends JFrame {
    private JComboBox<String> classDropdown;
    private JTable table;
    private DefaultTableModel tableModel;
    private Map<String, LopHoc> classes;
    private JTextArea rankSummaryArea;

    public QLSV() {
        setTitle("Quản lý lớp học CNTT");
        setSize(900, 600);  // Tăng kích thước giao diện để chứa bảng và phần tổng kết
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Cấu hình giao diện
        getContentPane().setBackground(new Color(240, 240, 240)); // Đặt màu nền cho toàn bộ giao diện
        
        classes = new HashMap<>();
        LopHoc class1 = new LopHoc("CNTT1");
        LopHoc class2 = new LopHoc("CNTT2");

        // Thêm sinh viên 1:
        Map<String, Double> grades1 = Map.of(
            "Lập trình hướng đối tượng", 9.0,
            "Quản lý dự án", 8.5,
            "Học Máy", 7.0,
            "Cơ sở dữ liệu", 6.5,
            "Lập trình ứng dụng cho TBDĐ", 9.0
        );
        class1.addStudent(new SinhVien("Nguyen", "An", "2000-01-01", "Hanoi", "CNTT1", grades1));

        //Thêm sinh viên 2:
        Map<String, Double> grades2 = Map.of(
            "Lập trình hướng đối tượng", 7.0,
            "Quản lý dự án", 6.0,
            "Học Máy", 5.5,
            "Cơ sở dữ liệu", 6.0,
            "Lập trình ứng dụng cho TBDĐ", 5.5
        );
         Map<String, Double> grades2_1 = Map.of(
            "Lập trình hướng đối tượng", 9.0,
            "Quản lý dự án", 9.0,
            "Học Máy", 9.5,
            "Cơ sở dữ liệu", 9.0,
            "Lập trình ứng dụng cho TBDĐ", 9.5
        );
         Map<String, Double> grades2_2 = Map.of(
            "Lập trình hướng đối tượng", 7.0,
            "Quản lý dự án", 8.0,
            "Học Máy", 7.5,
            "Cơ sở dữ liệu", 7.0,
            "Lập trình ứng dụng cho TBDĐ", 6.5
        );

        
        class2.addStudent(new SinhVien("Tran", "Binh", "2001-02-02", "HCM", "CNTT2", grades2));
        class2.addStudent(new SinhVien("Nguyen", "Duc", "2001-03-03", "HCM", "CNTT2", grades2_1));
        class2.addStudent(new SinhVien("Nguyen", "Trung Kien", "2001-04-04", "HCM", "CNTT2", grades2));
        class2.addStudent(new SinhVien("Hoang", "Quoc Anh", "2001-02-02", "HCM", "CNTT2", grades2_2));
        class2.addStudent(new SinhVien("Pham", "Ha My", "2001-02-01", "HCM", "CNTT2", grades2_1));
        class2.addStudent(new SinhVien("Pham", "Gia Bach", "2001-12-02", "HCM", "CNTT2", grades2_1));
        class2.addStudent(new SinhVien("Nguyen", "Quoc Bao", "2001-04-02", "HCM", "CNTT2", grades2_1));
        class2.addStudent(new SinhVien("Le", "Duc Loi", "2001-11-09", "HCM", "CNTT2", grades2));
        class2.addStudent(new SinhVien("Le", "Bao Han", "2001-02-21", "HCM", "CNTT2", grades2_2));

        classes.put("CNTT1", class1);
        classes.put("CNTT2", class2);

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        classDropdown = new JComboBox<>(classes.keySet().toArray(new String[0]));
        classDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton viewButton = new JButton("Xem Lớp");
        viewButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewButton.setBackground(new Color(0, 122, 204));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        topPanel.add(new JLabel("Chọn lớp: "));
        topPanel.add(classDropdown);
        topPanel.add(viewButton);
        add(topPanel, BorderLayout.NORTH);

        // Khởi tạo DefaultTableModel và JTable
        String[] columnNames = {"Họ", "Tên", "Ngày sinh", "Địa chỉ", "Điểm trung bình", "Rank"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(0, 122, 204));
        table.setRowHeight(30);
        table.setGridColor(Color.GRAY);
        table.getTableHeader().setBackground(new Color(0, 122, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Khu vực tổng kết số lượng sinh viên theo rank
        rankSummaryArea = new JTextArea(10, 30);
        rankSummaryArea.setEditable(false);
        rankSummaryArea.setFont(new Font("Arial", Font.PLAIN, 14));
        rankSummaryArea.setText("Tổng số sinh viên theo rank: ");
        rankSummaryArea.setBackground(new Color(240, 240, 240));
        rankSummaryArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JScrollPane rankScrollPane = new JScrollPane(rankSummaryArea);
        add(rankScrollPane, BorderLayout.SOUTH);

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedClass = (String) classDropdown.getSelectedItem();
                displayClassInfo(selectedClass);
            }
        });
    }

    private void displayClassInfo(String className) {
        LopHoc classroom = classes.get(className);
        if (classroom != null) {
            // Xóa tất cả dữ liệu cũ trong bảng
            tableModel.setRowCount(0);
            
            // Lưu trữ thông tin sinh viên và rank
            Map<String, Integer> rankCount = new HashMap<>();
            
            // Đảm bảo các rank từ "A" đến "<D" đều có mặt trong map với giá trị 0
            rankCount.put("A", 0);
            rankCount.put("B", 0);
            rankCount.put("C", 0);
            rankCount.put("D", 0);
            rankCount.put("<D", 0);

            for (SinhVien student : classroom.DSSV) {
                // Tính điểm trung bình và rank của sinh viên
                double averageGrade = student.getAverageGrade();
                String rank = student.getRank();

                // Cập nhật tổng số sinh viên theo rank
                rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
                
                // Thêm thông tin sinh viên vào bảng
                tableModel.addRow(new Object[] {
                    student.getFirstName(),
                    student.getLastName(),
                    student.getBirthdate(),
                    student.getAddress(),
                    averageGrade,  // Điểm trung bình
                    rank  // Rank
                });
            }

            // Hiển thị tổng số sinh viên theo rank trong JTextArea
            StringBuilder rankSummary = new StringBuilder();
            rankCount.forEach((rank, count) -> rankSummary.append(rank).append(": ").append(count).append("\n"));
            rankSummaryArea.setText(rankSummary.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QLSV().setVisible(true));
    }
}
