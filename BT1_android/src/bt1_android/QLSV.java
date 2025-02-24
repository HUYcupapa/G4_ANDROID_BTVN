package bt1_android;

import java.util.*;

public class QLSV {

    private Map<String, LopHoc> classes;
    public QLSV() {
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

        //Thêm 10 Sinh viên lớp CNTT1:
        // #1:
        Map<String, Double> grades1_1 = Map.of(
                "Lập trình hướng đối tượng", 4.0,
                "Quản lý dự án", 7.5,
                "Học Máy", 6.0,
                "Cơ sở dữ liệu", 6.5,
                "Lập trình ứng dụng cho TBDĐ", 9.0
        );
        class1.addStudent(new SinhVien("Nguyen", "Ba", "2001-01-02", "HN", "CNTT1", grades1_1));

        //#2:
        Map<String, Double> grades1_2 = Map.of(
                "Lập trình hướng đối tượng", 6.5,
                "Quản lý dự án", 5.0,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 7.5,
                "Lập trình ứng dụng cho TBDĐ", 9.5
        );
        class1.addStudent(new SinhVien("Tran", "An", "2001-02-15", "HCM", "CNTT1", grades1_2));

        //#3:
        Map<String, Double> grades1_3 = Map.of(
                "Lập trình hướng đối tượng", 7.0,
                "Quản lý dự án", 8.5,
                "Học Máy", 5.5,
                "Cơ sở dữ liệu", 4.5,
                "Lập trình ứng dụng cho TBDĐ", 6.0
        );
        class1.addStudent(new SinhVien("Le", "Duy", "2001-03-22", "DN", "CNTT1", grades1_3));

        //#4:
        Map<String, Double> grades1_4 = Map.of(
                "Lập trình hướng đối tượng", 5.5,
                "Quản lý dự án", 6.0,
                "Học Máy", 7.5,
                "Cơ sở dữ liệu", 8.0,
                "Lập trình ứng dụng cho TBDĐ", 4.0
        );
        class1.addStudent(new SinhVien("Pham", "Minh", "2001-04-10", "CT", "CNTT1", grades1_4));

        //#5:
        Map<String, Double> grades1_5 = Map.of(
                "Lập trình hướng đối tượng", 8.0,
                "Quản lý dự án", 9.5,
                "Học Máy", 6.5,
                "Cơ sở dữ liệu", 5.0,
                "Lập trình ứng dụng cho TBDĐ", 7.0
        );
        class1.addStudent(new SinhVien("Vo", "Tuan", "2001-05-18", "VT", "CNTT1", grades1_5));

        //#6:
        Map<String, Double> grades1_6 = Map.of(
                "Lập trình hướng đối tượng", 9.5,
                "Quản lý dự án", 7.0,
                "Học Máy", 4.5,
                "Cơ sở dữ liệu", 6.0,
                "Lập trình ứng dụng cho TBDĐ", 8.5
        );
        class1.addStudent(new SinhVien("Dang", "Hieu", "2001-06-25", "QN", "CNTT1", grades1_6));

        //#7:
        Map<String, Double> grades1_7 = Map.of(
                "Lập trình hướng đối tượng", 4.5,
                "Quản lý dự án", 6.5,
                "Học Máy", 9.0,
                "Cơ sở dữ liệu", 7.5,
                "Lập trình ứng dụng cho TBDĐ", 5.0
        );
        class1.addStudent(new SinhVien("Ho", "Phuc", "2001-07-30", "PY", "CNTT1", grades1_7));

        //#8:
        Map<String, Double> grades1_8 = Map.of(
                "Lập trình hướng đối tượng", 7.5,
                "Quản lý dự án", 5.5,
                "Học Máy", 8.5,
                "Cơ sở dữ liệu", 6.0,
                "Lập trình ứng dụng cho TBDĐ", 4.0
        );
        class1.addStudent(new SinhVien("Ngo", "Tam", "2001-08-14", "NT", "CNTT1", grades1_8));

        //#9:
        Map<String, Double> grades1_9 = Map.of(
                "Lập trình hướng đối tượng", 6.0,
                "Quản lý dự án", 4.5,
                "Học Máy", 5.0,
                "Cơ sở dữ liệu", 9.5,
                "Lập trình ứng dụng cho TBDĐ", 7.5
        );
        class1.addStudent(new SinhVien("Trinh", "Long", "2001-09-05", "LA", "CNTT1", grades1_9));

        //#10:
        Map<String, Double> grades1_10 = Map.of(
                "Lập trình hướng đối tượng", 9.0,
                "Quản lý dự án", 8.0,
                "Học Máy", 7.0,
                "Cơ sở dữ liệu", 5.5,
                "Lập trình ứng dụng cho TBDĐ", 6.5
        );
        class1.addStudent(new SinhVien("Ly", "Quang", "2001-10-12", "TG", "CNTT1", grades1_10));

        // Thêm sinh viên mới vào lớp CNTT1:
        Map<String, Double> gradesNew = Map.of(
                "Lập trình hướng đối tượng", 7.0,
                "Quản lý dự án", 7.7,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 8.0
        );
        class1.addStudent(new SinhVien("Tran", "An Ha", "2003-03-09", "ĐN", "CNTT1", gradesNew));

        // Thêm sinh viên mới vào lớp CNTT1:
        Map<String, Double> gradesNew2 = Map.of(
                "Lập trình hướng đối tượng", 7.0,
                "Quản lý dự án", 7.0,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 9.0
        );
        class1.addStudent(new SinhVien("Tran", "Duong Ha", "2003-03-21", "ĐA", "CNTT1", gradesNew2));

        // Thêm sinh viên mới vào lớp CNTT1:
        Map<String, Double> gradesNew3 = Map.of(
                "Lập trình hướng đối tượng", 6.0,
                "Quản lý dự án", 7.0,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 9.0
        );
        class1.addStudent(new SinhVien("Tran", "Lan Luy", "2003-06-21", "TA", "CNTT1", gradesNew3));

        // Thêm sinh viên mới vào lớp CNTT1:
        Map<String, Double> gradesNew4 = Map.of(
                "Lập trình hướng đối tượng", 6.0,
                "Quản lý dự án", 7.5,
                "Học Máy", 8.5,
                "Cơ sở dữ liệu", 9.0
        );
        class1.addStudent(new SinhVien("Tran", "Ha Lan", "2000-06-21", "HN", "CNTT1", gradesNew4));

        Map<String, Double> gradesNew5 = Map.of(
                "Lập trình hướng đối tượng", 6.0,
                "Quản lý dự án", 3.0,
                "Học Máy", 8.5,
                "Cơ sở dữ liệu", 9.5
        );
        class1.addStudent(new SinhVien("Tran", "Long Duy", "2000-09-21", "HN", "CNTT1", gradesNew5));

        //Thêm 10 Sinh viên lớp CNTT2:
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
        class2.addStudent(new SinhVien("Duc", "Hoang", "2001-03-21", "HCM", "CNTT2", grades2));

        Map<String, Double> gradesNe = Map.of(
                "Lập trình hướng đối tượng", 7.0,
                "Quản lý dự án", 6.5,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 8.5
        );
        class2.addStudent(new SinhVien("Le", "Mai Anh", "2004-03-22", "ĐA", "CNTT2", gradesNe));

        Map<String, Double> gradesNe2 = Map.of(
                "Lập trình hướng đối tượng", 5.0,
                "Quản lý dự án", 9.5,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 8.5
        );
        class2.addStudent(new SinhVien("Le", "Lan Duong", "2004-03-28", "ĐT", "CNTT2", gradesNe2));

        Map<String, Double> gradesNe3 = Map.of(
                "Lập trình hướng đối tượng", 7.0,
                "Quản lý dự án", 5.5,
                "Học Máy", 8.0,
                "Cơ sở dữ liệu", 9.0
        );
        class2.addStudent(new SinhVien("Le", "Mai Minh", "2008-08-22", "ĐA", "CNTT2", gradesNe3));
        //DONE THÊM SV CNTT2
        //Thêm lớp mới vào bảng:
        classes.put("CNTT1", class1);
        classes.put("CNTT2", class2);

    }

    // Trả về lớp học để gửi đến lớp GUI:
    public Map<String, LopHoc> getClasses() {
        return classes;
    }
    
    public static void main(String[] args) {
        //SwingUtilities.invokeLater(() -> new QLSV().setVisible(true));
    }
}
