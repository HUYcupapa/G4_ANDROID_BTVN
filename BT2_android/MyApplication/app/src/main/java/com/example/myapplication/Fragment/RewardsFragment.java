package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapter.DailyTaskAdapter;
import com.example.myapplication.Model.DailyTask;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RewardsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvPoints;
    private RecyclerView rvMissions;
    private DailyTaskAdapter taskAdapter;
    private List<DailyTask> dailyTasks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        tvPoints = view.findViewById(R.id.tv_points);
        rvMissions = view.findViewById(R.id.rv_missions);

        setupTaskRecyclerView();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        // Kiểm tra và reset nhiệm vụ nếu cần
        checkAndResetDailyTasks(userId, userRef);

        // Lắng nghe thay đổi từ Firestore
        userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                tvPoints.setText("Lỗi khi tải dữ liệu");
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Long points = documentSnapshot.getLong("points");
                tvPoints.setText("Tổng điểm thưởng: " + points);

                Map<String, Object> dailyTasksMap = (Map<String, Object>) documentSnapshot.get("daily_tasks");
                if (dailyTasksMap != null) {
                    updateDailyTasksList(dailyTasksMap);
                }
            } else {
                tvPoints.setText("Không có dữ liệu");
            }
        });

        return view;
    }

    private void setupTaskRecyclerView() {
        taskAdapter = new DailyTaskAdapter(dailyTasks);
        rvMissions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMissions.setAdapter(taskAdapter);
    }

    private void checkAndResetDailyTasks(String userId, DocumentReference userRef) {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Date lastUpdated = null;
                Map<String, Object> dailyTasksMap = (Map<String, Object>) documentSnapshot.get("daily_tasks");
                if (dailyTasksMap != null && dailyTasksMap.containsKey("daily_login")) {
                    Map<String, Object> loginTask = (Map<String, Object>) dailyTasksMap.get("daily_login");
                    if (loginTask != null && loginTask.get("last_updated") != null) {
                        lastUpdated = ((com.google.firebase.Timestamp) loginTask.get("last_updated")).toDate();
                    }
                }

                // Lấy thời gian hiện tại và 6h sáng hôm nay
                Calendar now = Calendar.getInstance();
                Calendar today6AM = Calendar.getInstance();
                today6AM.set(Calendar.HOUR_OF_DAY, 6);
                today6AM.set(Calendar.MINUTE, 0);
                today6AM.set(Calendar.SECOND, 0);
                today6AM.set(Calendar.MILLISECOND, 0);

                // Nếu chưa có last_updated hoặc đã qua 6h sáng hôm nay
                if (lastUpdated == null || lastUpdated.before(today6AM.getTime())) {
                    resetDailyTasks(userRef);
                }
            }
        });
    }

    private void resetDailyTasks(DocumentReference userRef) {
        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("completed", false);
        taskData.put("points_earned", 0);
        taskData.put("last_updated", FieldValue.serverTimestamp());

        updates.put("daily_tasks.checkin_cafe", taskData);
        updates.put("daily_tasks.daily_login", taskData);
        updates.put("daily_tasks.write_review", taskData);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Có thể thông báo nếu cần
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi khi reset nhiệm vụ!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDailyTasksList(Map<String, Object> dailyTasksMap) {
        dailyTasks.clear();

        // Check-in task
        Map<String, Object> checkinTask = (Map<String, Object>) dailyTasksMap.get("checkin_cafe");
        if (checkinTask != null) {
            boolean completed = checkinTask.get("completed") != null && (boolean) checkinTask.get("completed");
            Object points = checkinTask.get("points_earned");
            int pointsValue = points instanceof String ? Integer.parseInt((String) points) : ((Number) points).intValue();

            dailyTasks.add(new DailyTask(
                    "checkin_cafe",
                    "Check-in tại quán cafe",
                    pointsValue,
                    completed,
                    "Hàng ngày"
            ));
        }

        // Daily login task
        Map<String, Object> loginTask = (Map<String, Object>) dailyTasksMap.get("daily_login");
        if (loginTask != null) {
            boolean completed = loginTask.get("completed") != null && (boolean) loginTask.get("completed");
            Object points = loginTask.get("points_earned");
            int pointsValue = points instanceof String ? Integer.parseInt((String) points) : ((Number) points).intValue();

            dailyTasks.add(new DailyTask(
                    "daily_login",
                    "Đăng nhập hàng ngày",
                    pointsValue,
                    completed,
                    "Hàng ngày"
            ));
        }

        // Write review task
        Map<String, Object> reviewTask = (Map<String, Object>) dailyTasksMap.get("write_review");
        if (reviewTask != null) {
            boolean completed = reviewTask.get("completed") != null && (boolean) reviewTask.get("completed");
            Object points = reviewTask.get("points_earned");
            int pointsValue = points instanceof String ? Integer.parseInt((String) points) : ((Number) points).intValue();

            dailyTasks.add(new DailyTask(
                    "write_review",
                    "Viết đánh giá",
                    pointsValue,
                    completed,
                    "Hàng ngày"
            ));
        }

        taskAdapter.notifyDataSetChanged();
    }
}