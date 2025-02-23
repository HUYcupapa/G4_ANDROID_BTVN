# I.YÊU CẦU: 
## Viết chương trình quản lý lớp học CNTT:
1. Mỗi sinh viên sẽ có các thông tin:
  - Thông tin sinh viên: FirstName, LastName, Birthdate, Address, Class,
  - Điểm môn học:
    + Lập trình hướng đối tượng
    + Quản lý dự án
    + Học Máy
    + Cơ sở dữ liệu
    + Lập trình ứng dụng cho TBDĐ
2. Cần có danh sách lớp, và mỗi lớp có danh sách sinh viên
3. Mỗi lớp sẽ có các phương thức xác định số sinh viên theo rank điểm: A, B, C, D & <D
4. Chương trình chính sẽ hiển thị ra danh sách các lớp, khi người dùng nhập mã 1 lớp sẽ hiển thị danh sách sinh viên và tổng kết số người theo rank: A, B, C, D & <D

# II. CHƯƠNG TRÌNH:
## 1. Mô tả tổng quan
- Chương trình Quản lý lớp học CNTT giúp theo dõi danh sách sinh viên, tính điểm trung bình, xếp loại sinh viên theo các mức A, B, C, D, <D, và thống kê số lượng sinh viên theo từng xếp hạng trong mỗi lớp học.
- Giao diện đồ họa (GUI) được thiết kế bằng Java Swing, hiển thị danh sách lớp học, sinh viên và tổng kết số lượng sinh viên theo từng loại điểm.

## 2. Chức năng chính
- 🔹 Quản lý danh sách sinh viên trong từng lớp học.
- 🔹 Tính toán điểm trung bình của sinh viên dựa trên điểm số của các môn học.
- 🔹 Xếp loại sinh viên theo thang điểm: A, B, C, D, <D.
- 🔹 Thống kê số lượng sinh viên theo từng xếp loại trong lớp.
- 🔹 Hiển thị thông tin sinh viên dưới dạng bảng có thể lọc theo lớp.

## 3. Cấu trúc chương trình
Chương trình được tổ chức thành 3 file chính:
- SinhVien.java: Định nghĩa lớp SinhVien, chứa thông tin cá nhân và điểm số của sinh viên.
- LopHoc.java: Quản lý danh sách sinh viên trong một lớp học và thực hiện thống kê số lượng theo rank.
- QLSV.java (chương trình chính): Hiển thị giao diện người dùng (GUI), cho phép lựa chọn lớp học, xem danh sách sinh viên và tổng kết số lượng theo từng hạng điểm.

## 4. Chi tiết các lớp
### 4.1. Lớp SinhVien (SinhVien.java)
- Lưu trữ thông tin của một sinh viên, bao gồm:
  + ✔️ Họ và tên (FirstName, LastName)
  + ✔️ Ngày sinh (Birthdate)
  + ✔️ Địa chỉ (Address)
  + ✔️ Tên lớp (Class)
  + ✔️ Điểm số của các môn học (Lập trình hướng đối tượng, Quản lý dự án, Học máy, Cơ sở dữ liệu, Lập trình ứng dụng cho TBDĐ).

- Phương thức chính:
- ✅ getAverageGrade() – Tính điểm trung bình dựa trên tất cả môn học.
- ✅ getRank() – Xếp loại sinh viên theo điểm trung bình đã tính ở hàm getAverageGrade():
  + A: ĐTB >= 9.0
  + B: ĐTB >= 8.0
  + C: ĐTB >= 6.5
  + D: ĐTB >= 5.0
  + <D: ĐTB < 5.0
    
### 4.2. Lớp LopHoc (LopHoc.java)
- Quản lý danh sách sinh viên của từng lớp.
- Phương thức chính:
+ ✅ addStudent(SinhVien sv) – Thêm một sinh viên vào danh sách lớp.
+ ✅ getRankSummary() – Thống kê số lượng sinh viên theo từng xếp loại A, B, C, D, <D.
+ ✅ displayStudents() – In danh sách sinh viên của lớp ra màn hình console.

### 4.3. Chương trình chính (QLSV.java)
- Giao diện đồ họa (GUI) được thiết kế bằng Java Swing, có các tính năng chính:
  + ✔️ Chọn lớp từ danh sách thả xuống (JComboBox)
  + ✔️ Hiển thị danh sách sinh viên trong lớp (JTable)
  + ✔️ Hiển thị tổng số sinh viên theo từng rank (JTextArea)
  + ✔️ Bảng có thể lọc, hiển thị điểm trung bình và xếp hạng của từng sinh viên

## 5.Giao diện chương trình:
- Giao diện khi khởi động:
  
![image](https://github.com/user-attachments/assets/d171c5b6-181a-44a8-9208-27da9e04f581)

- Bấm chọn 1 lớp trên combobox "Chọn lớp" và bấm button "Xem lớp", chương trình sẽ hiển thị danh sách các sinh viên trong lớp dưới dạng bảng và thống kê rank của lớp:

![image](https://github.com/user-attachments/assets/ad7b9f96-43fd-4369-b46e-4a5c1127cc6b)
![image](https://github.com/user-attachments/assets/3ee4f70a-3549-4365-ab36-c3d3b5bf17ca)
