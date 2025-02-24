package bt1_android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LopHoc {
    String className;
    List<SinhVien> DSSV;

    public String getClassName(){
        return className;
    }

    public List<SinhVien> getDSSV(){
        return DSSV;
    }

    public LopHoc(String className) {
        this.className = className;
        this.DSSV = new ArrayList<>();
    }

    public void addStudent(SinhVien sv) {
        DSSV.add(sv);
    }

    public Map<String, Long> getRankSummary() {
        Map<String, Long> summary = new HashMap<>();
        for (String rank : new String[]{"A", "B", "C", "D", "<D"}) {
            summary.put(rank, DSSV.stream().filter(s -> s.getRank().equals(rank)).count());
        }
        return summary;
    }

    public void displayStudents() {
        DSSV.forEach(System.out::println);
    }
}
