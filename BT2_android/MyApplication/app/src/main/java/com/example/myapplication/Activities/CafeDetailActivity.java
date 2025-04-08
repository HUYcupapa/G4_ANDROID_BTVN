package com.example.myapplication.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CafeDetailActivity extends AppCompatActivity {

    private TextView tvCafeName, tvRating, tvAddress, tvDescription, tvActivity;
    private ImageView ivCafeImage;
    private Button btnReview;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cafeId, userId;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> videoPickerLauncher;
    private List<String> selectedImages = new ArrayList<>(); // Lưu danh sách URL hình ảnh (giả lập)
    private String selectedVideo = null; // Lưu URL video (giả lập)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_detail);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Lấy userId
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        // Khởi tạo các view
        tvCafeName = findViewById(R.id.tv_cafe_name);
        tvRating = findViewById(R.id.tv_rating);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvActivity = findViewById(R.id.tv_activity);
        ivCafeImage = findViewById(R.id.iv_cafe_image);
        btnReview = findViewById(R.id.btn_review);

        // Lấy cafeId từ Intent
        cafeId = getIntent().getStringExtra("cafeId");

        // Lấy thông tin quán từ Firestore
        if (cafeId != null) {
            db.collection("cafes").document(cafeId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String cafeName = documentSnapshot.getString("name");
                            Double ratingStar = documentSnapshot.getDouble("ratingStar");
                            String address = documentSnapshot.getString("address");
                            String description = documentSnapshot.getString("description");
                            String image1 = documentSnapshot.getString("image1");
                            String activity = documentSnapshot.getString("activity");

                            // Hiển thị dữ liệu
                            tvCafeName.setText(cafeName != null ? cafeName : "Tên quán");
                            tvRating.setText("Đánh giá: " + (ratingStar != null ? String.format("%.1f", ratingStar) : "0.0") + "/5");
                            tvAddress.setText("Địa chỉ: " + (address != null ? address : "Không có địa chỉ"));
                            tvDescription.setText("Mô tả: " + (description != null ? description : "Không có mô tả"));
                            tvActivity.setText("Hoạt động: " + (activity != null ? activity : "Không có hoạt động"));

                            // Hiển thị hình ảnh bằng Picasso
                            if (image1 != null && !image1.isEmpty()) {
                                Picasso.get().load(image1).into(ivCafeImage);
                            } else {
                                ivCafeImage.setImageResource(R.drawable.ic_placeholder); // Hình ảnh mặc định nếu không có
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin quán!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tải thông tin quán!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Không tìm thấy ID quán!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Khởi tạo ActivityResultLauncher để chọn hình ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (selectedImages.size() < 3) {
                            selectedImages.add(uri.toString()); // Giả lập URL
                            Toast.makeText(this, "Đã chọn " + selectedImages.size() + "/3 hình ảnh", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Đã đạt tối đa 3 hình ảnh!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Khởi tạo ActivityResultLauncher để chọn video
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (selectedVideo == null) {
                            selectedVideo = uri.toString(); // Giả lập URL
                            Toast.makeText(this, "Đã chọn video", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Chỉ được chọn 1 video!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Xử lý nút Đánh giá
        btnReview.setOnClickListener(v -> showReviewDialog());
    }

    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        // Khởi tạo các thành phần trong dialog
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
        Spinner spinnerActivity = dialogView.findViewById(R.id.spinnerActivity);
        EditText editTextOtherActivity = dialogView.findViewById(R.id.editTextOtherActivity);
        Button btnAddImages = dialogView.findViewById(R.id.btnAddImages);
        Button btnAddVideo = dialogView.findViewById(R.id.btnAddVideo);
        Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.activity_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        // Xử lý khi chọn "others" trong Spinner
        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals("others")) {
                    editTextOtherActivity.setVisibility(View.VISIBLE);
                } else {
                    editTextOtherActivity.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextOtherActivity.setVisibility(View.GONE);
            }
        });

        // Xử lý nút thêm hình ảnh
        btnAddImages.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        // Xử lý nút thêm video
        btnAddVideo.setOnClickListener(v -> {
            videoPickerLauncher.launch("video/*");
        });

        // Xử lý nút gửi đánh giá
        AlertDialog dialog = builder.create();
        btnSubmitReview.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = editTextComment.getText().toString().trim();
            String activity = spinnerActivity.getSelectedItem().toString();
            String otherActivityDescription = editTextOtherActivity.getText().toString().trim();

            // Kiểm tra dữ liệu
            if (activity.equals("Chọn hoạt động")) {
                activity = null; // Để trống nếu không chọn
            }
            if (activity != null && activity.equals("others") && otherActivityDescription.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả hoạt động!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu đánh giá vào Firestore
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("userId", userId);
            reviewData.put("cafeId", cafeId);
            reviewData.put("rating", rating);
            reviewData.put("comment", comment.isEmpty() ? null : comment);
            reviewData.put("activity", activity);
            reviewData.put("otherActivityDescription", activity != null && activity.equals("others") ? otherActivityDescription : null);
            reviewData.put("images", selectedImages);
            reviewData.put("video", selectedVideo);
            reviewData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection("reviews").add(reviewData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        updateCafeRating(rating); // Cập nhật rating của quán
                        updateUserPoints(); // Thêm 1 điểm cho đánh giá
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi đánh giá!", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void updateCafeRating(float newRating) {
        DocumentReference cafeRef = db.collection("cafes").document(cafeId);
        cafeRef.get().addOnSuccessListener(documentSnapshot -> {
            Long ratingCount = documentSnapshot.getLong("ratingCount");
            Double currentRating = documentSnapshot.getDouble("ratingStar");

            long newRatingCount = (ratingCount != null ? ratingCount : 0) + 1;
            double totalRating = (currentRating != null ? currentRating * (newRatingCount - 1) : 0) + newRating;
            double newAverageRating = totalRating / newRatingCount;

            Map<String, Object> updates = new HashMap<>();
            updates.put("ratingCount", newRatingCount);
            updates.put("ratingStar", newAverageRating);

            cafeRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        tvRating.setText("Đánh giá: " + String.format("%.1f", newAverageRating) + "/5");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật đánh giá quán!", Toast.LENGTH_SHORT).show());
        });
    }

    private void updateUserPoints() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Long currentPoints = documentSnapshot.getLong("points");
            int newPoints = (currentPoints != null ? currentPoints.intValue() : 0) + 1;

            userRef.update("points", newPoints)
                    .addOnSuccessListener(aVoid -> {
                        // Có thể thêm thông báo nếu muốn
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật điểm!", Toast.LENGTH_SHORT).show());
        });
    }
}