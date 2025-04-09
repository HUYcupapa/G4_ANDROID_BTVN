package com.example.myapplication.Activities;

import android.net.Uri;
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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReviewActivity extends AppCompatActivity {

    private TextView tvCafeName, tvRating, tvAddress, tvDescription, tvActivity;
    private ImageView ivCafeImage;
    private Button btnReview;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cafeId, userId;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> videoPickerLauncher;
    private List<Uri> selectedImageUris = new ArrayList<>(); // Lưu danh sách URI hình ảnh
    private Uri selectedVideoUri = null; // Lưu URI video
    private List<String> uploadedImageUrls = new ArrayList<>(); // Lưu danh sách URL hình ảnh sau khi upload
    private String uploadedVideoUrl = null; // Lưu URL video sau khi upload
    private OkHttpClient client;
    private static final String IMGUR_CLIENT_ID = "44708ec159ebd14"; // Sử dụng Client-ID của bạn bạn
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        client = new OkHttpClient();

        // Lấy userId
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
                                ivCafeImage.setImageResource(R.drawable.ic_placeholder);
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin quán!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tải thông tin quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        if (selectedImageUris.size() < 3) {
                            selectedImageUris.add(uri);
                            Toast.makeText(this, "Đã chọn " + selectedImageUris.size() + "/3 hình ảnh", Toast.LENGTH_SHORT).show();
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
                        if (selectedVideoUri == null) {
                            selectedVideoUri = uri;
                            Toast.makeText(this, "Đã chọn video", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Chỉ được chọn 1 video!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Xử lý nút Đánh giá
        btnReview.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }
            showReviewDialog();
        });
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
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm hình ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        // Xử lý nút thêm video
        btnAddVideo.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm video!", Toast.LENGTH_SHORT).show();
                return;
            }
            videoPickerLauncher.launch("video/*");
        });

        // Xử lý nút gửi đánh giá
        AlertDialog dialog = builder.create();
        btnSubmitReview.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để gửi đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }

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

            // Upload hình ảnh và video trước khi lưu đánh giá
            uploadMediaAndSubmitReview(rating, comment, activity, otherActivityDescription, dialog);
        });

        dialog.show();
    }

    private void uploadMediaAndSubmitReview(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        // Reset danh sách URL
        uploadedImageUrls.clear();
        uploadedVideoUrl = null;

        // Upload hình ảnh nếu có
        if (!selectedImageUris.isEmpty()) {
            uploadImages(rating, comment, activity, otherActivityDescription, dialog);
        } else if (selectedVideoUri != null) {
            // Nếu không có hình ảnh nhưng có video, upload video
            uploadVideo(rating, comment, activity, otherActivityDescription, dialog);
        } else {
            // Nếu không có hình ảnh và video, lưu đánh giá ngay
            submitReview(rating, comment, activity, otherActivityDescription, dialog);
        }
    }

    private void uploadImages(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        int totalImages = selectedImageUris.size();
        final int[] uploadedCount = {0};

        for (Uri imageUri : selectedImageUris) {
            try {
                // Đọc dữ liệu từ URI
                byte[] imageBytes = readBytesFromUri(imageUri);
                if (imageBytes == null) {
                    Toast.makeText(this, "Không thể đọc dữ liệu hình ảnh!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                // Tạo request body
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/*"), imageBytes))
                        .addFormDataPart("type", "file")
                        .build();

                // Tạo request
                Request request = new Request.Builder()
                        .url(IMGUR_UPLOAD_URL)
                        .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(ReviewActivity.this, "Lỗi khi upload hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            try {
                                JSONObject json = new JSONObject(responseBody);
                                String imageUrl = json.getJSONObject("data").getString("link");
                                uploadedImageUrls.add(imageUrl);
                                uploadedCount[0]++;

                                // Khi tất cả hình ảnh đã được upload
                                if (uploadedCount[0] == totalImages) {
                                    if (selectedVideoUri != null) {
                                        // Nếu có video, tiếp tục upload video
                                        uploadVideo(rating, comment, activity, otherActivityDescription, dialog);
                                    } else {
                                        // Nếu không có video, lưu đánh giá
                                        submitReview(rating, comment, activity, otherActivityDescription, dialog);
                                    }
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ReviewActivity.this, "Lỗi khi phân tích phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(ReviewActivity.this, "Lỗi khi upload hình ảnh: " + response.message(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                        response.close();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ReviewActivity.this, "Lỗi khi xử lý hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
        }
    }

    private void uploadVideo(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        try {
            // Đọc dữ liệu từ URI
            byte[] videoBytes = readBytesFromUri(selectedVideoUri);
            if (videoBytes == null) {
                Toast.makeText(this, "Không thể đọc dữ liệu video!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            // Tạo request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("video", "video.mp4", RequestBody.create(MediaType.parse("video/*"), videoBytes))
                    .addFormDataPart("type", "file")
                    .addFormDataPart("disable_audio", "0")
                    .build();

            // Tạo request
            Request request = new Request.Builder()
                    .url(IMGUR_UPLOAD_URL)
                    .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ReviewActivity.this, "Lỗi khi upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            uploadedVideoUrl = json.getJSONObject("data").getString("link");
                            // Sau khi upload video xong, lưu đánh giá
                            submitReview(rating, comment, activity, otherActivityDescription, dialog);
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(ReviewActivity.this, "Lỗi khi phân tích phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ReviewActivity.this, "Lỗi khi upload video: " + response.message(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(ReviewActivity.this, "Lỗi khi xử lý video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
    }

    private void submitReview(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        // Lưu đánh giá vào Firestore
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", userId);
        reviewData.put("cafeId", cafeId);
        reviewData.put("rating", rating);
        reviewData.put("comment", comment.isEmpty() ? null : comment);
        reviewData.put("activity", activity);
        reviewData.put("otherActivityDescription", activity != null && activity.equals("others") ? otherActivityDescription : null);
        reviewData.put("images", uploadedImageUrls); // Lưu danh sách URL hình ảnh
        reviewData.put("video", uploadedVideoUrl); // Lưu URL video
        reviewData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("reviews").add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        updateCafeRating(rating); // Cập nhật rating của quán
                        updateUserPoints(); // Thêm 1 điểm cho đánh giá
                        dialog.dismiss();

                        // Reset danh sách sau khi gửi đánh giá thành công
                        selectedImageUris.clear();
                        selectedVideoUri = null;
                        uploadedImageUrls.clear();
                        uploadedVideoUrl = null;
                    });
                })
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
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

    // Phương thức đọc dữ liệu từ URI
    private byte[] readBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

