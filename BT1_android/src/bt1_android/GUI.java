package bt1_android;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

public class GUI extends JFrame {
    private JComboBox<String> classDropdown;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea rankSummaryArea;
    private QLSV qlsv;
    private Map<String, LopHoc> classes;

    public GUI(QLSV qlsv) {
        this.qlsv = qlsv;
        this.classes = qlsv.getClasses();
        
        setTitle("Quản lý lớp học CNTT");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        classDropdown = new JComboBox<>(this.classes.keySet().toArray(new String[0]));
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
        
        //Phần hiển thị phần tổng kết theo rank:
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
            tableModel.setRowCount(0);
            
            Map<String, Integer> rankCount = new HashMap<>();
            rankCount.put("A", 0);
            rankCount.put("B", 0);
            rankCount.put("C", 0);
            rankCount.put("D", 0);
            rankCount.put("<D", 0);

            for (SinhVien student : classroom.DSSV) {
                double averageGrade = student.getAverageGrade();
                String rank = student.getRank();
                rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
                tableModel.addRow(new Object[]{
                    student.getFirstName(),
                    student.getLastName(),
                    student.getBirthdate(),
                    student.getAddress(),
                    averageGrade,
                    rank
                });
            }
            
            StringBuilder rankSummary = new StringBuilder();
            rankCount.forEach((rank, count) -> rankSummary.append(rank).append(": ").append(count).append("\n"));
            rankSummaryArea.setText(rankSummary.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QLSV qlsv = new QLSV();
            new GUI(qlsv).setVisible(true);
        });
    }
}
