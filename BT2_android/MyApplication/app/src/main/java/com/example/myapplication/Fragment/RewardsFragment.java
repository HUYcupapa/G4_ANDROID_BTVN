package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RewardsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvPoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        tvPoints = view.findViewById(R.id.tv_points);

        // Lấy điểm thưởng từ Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long points = documentSnapshot.getLong("points");
                        tvPoints.setText("Tổng điểm thưởng: " + (points != null ? points : 0));
                    }
                });

        // TODO: Thêm logic hiển thị huy hiệu và nhiệm vụ
        return view;
    }
}