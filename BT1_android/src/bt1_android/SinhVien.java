package bt1_android;

import java.util.Map;

public class SinhVien {
    private String firstName;
    private String lastName;
    private String birthdate;
    private String address;
    private String className;
    private Map<String, Double> grades;

    public SinhVien(String firstName, String lastName, String birthdate, String address, String className, Map<String, Double> grades) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.address = address;
        this.className = className;
        this.grades = grades;
    }

    // Getter methods
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getAddress() {
        return address;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, Double> getGrades() {
        return grades;
    }

    // Tính điểm trung bình của sinh viên
    public double getAverageGrade() {
        return grades.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // Xếp hạng sinh viên dựa trên điểm trung bình
    public String getRank() {
        double avg = getAverageGrade();
        if (avg >= 9) return "A";
        else if (avg >= 8) return "B";
        else if (avg >= 6.5) return "C";
        else if (avg >= 5) return "D";
        else return "<D";
    }

    // Chuyển thông tin sinh viên thành chuỗi
    @Override
    public String toString() {
        return firstName + " " + lastName + " - " + className + " - Ngày sinh: " + birthdate + " - Địa chỉ: " + address + " - Rank: " + getRank();
    }
}
