package com.example.myapplication.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
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
    private final List<Uri> selectedImageUris = new ArrayList<>(); // Lưu danh sách URI hình ảnh
    private Uri selectedVideoUri = null; // Lưu URI video
    private final List<String> uploadedImageUrls = new ArrayList<>(); // Lưu danh sách URL hình ảnh sau khi upload
    private String uploadedVideoUrl = null; // Lưu URL video sau khi upload
    private OkHttpClient client;
    private static final String IMGUR_CLIENT_ID = "44708ec159ebd14"; // Sử dụng Client-ID của bạn
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/upload";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private HorizontalScrollView mediaContainer; // Biến để lưu mediaContainer từ dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        client = new OkHttpClient();

        // Yêu cầu quyền runtime
        requestStoragePermissions();

        // Lấy userId
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            // Không gọi finish() ngay, để người dùng tự quay lại
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

        // Tải thông tin quán cà phê
        loadCafeInfo();

        // Khởi tạo ActivityResultLauncher để chọn hình ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (selectedImageUris.size() < 3) {
                            selectedImageUris.add(uri);
                            Toast.makeText(this, "Đã chọn " + selectedImageUris.size() + "/3 hình ảnh", Toast.LENGTH_SHORT).show();
                            updateMediaContainer(); // Cập nhật mediaContainer
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
                            updateMediaContainer(); // Cập nhật mediaContainer
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
            if (cafeId == null) {
                Toast.makeText(this, "Không tìm thấy ID quán!", Toast.LENGTH_SHORT).show();
                return;
            }
            showReviewDialog();
        });
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                }, PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền truy cập bị từ chối, không thể chọn ảnh/video!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //# HIỂN THỊ THÔNG TIN QUÁN
    private void loadCafeInfo() {
        if (cafeId != null) {
            db.collection("cafes").document(cafeId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String cafeName = documentSnapshot.getString("name");
                            Double ratingStar = documentSnapshot.getDouble("ratingStar");
                            String locationText = documentSnapshot.getString("locationText");
                            String description = documentSnapshot.getString("description");
                            String image1 = documentSnapshot.getString("image1");
                            String activity = documentSnapshot.getString("activity");

                            // Hiển thị dữ liệu
                            tvCafeName.setText(cafeName != null ? cafeName : "Tên quán");
                            tvRating.setText("Đánh giá: " + (ratingStar != null ? String.format("%.1f", ratingStar) : "0.0") + "/5");
                            tvAddress.setText("Địa chỉ: " + (locationText != null ? locationText : "Không có địa chỉ")); // Sửa từ "address" thành "locationText"
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
                            // Không gọi finish() ngay, để người dùng tự quay lại
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tải thông tin quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Không gọi finish() ngay, để người dùng tự quay lại
                    });
        } else {
            Toast.makeText(this, "Không tìm thấy ID quán!", Toast.LENGTH_SHORT).show();
            // Không gọi finish() ngay, để người dùng tự quay lại
        }
    }


    // #LOAD ẢNH VÀ VIDEO LÊN MÀN HÌNH HIỂN THỊ
    private void updateMediaContainer() {
        if (mediaContainer == null) return;

        // Xóa các view cũ trong mediaContainer
        mediaContainer.removeAllViews();

        // Tạo LinearLayout để chứa các ảnh/video
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                450 // Đảm bảo chiều cao khớp với XML
        ));
        linearLayout.setPadding(8, 8, 8, 8);

        // Thêm các hình ảnh đã chọn
        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);
            View mediaView = LayoutInflater.from(this).inflate(R.layout.item_media, linearLayout, false);

            ImageView ivMedia = mediaView.findViewById(R.id.ivMedia);
            ImageButton btnRemove = mediaView.findViewById(R.id.btnRemove);
            TextView tvMediaCount = mediaView.findViewById(R.id.tvMediaCount);

            // Hiển thị ảnh
            ivMedia.setImageURI(uri);

            // Hiển thị số lượng ảnh
            tvMediaCount.setText((i + 1) + "/3");

            // Xử lý nút xóa
            btnRemove.setOnClickListener(v -> {
                selectedImageUris.remove(uri);
                updateMediaContainer();
                Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
            });

            linearLayout.addView(mediaView);
        }

        // Thêm video đã chọn (nếu có)
        if (selectedVideoUri != null) {
            View mediaView = LayoutInflater.from(this).inflate(R.layout.item_media, linearLayout, false);

            ImageView ivMedia = mediaView.findViewById(R.id.ivMedia);
            ImageButton btnRemove = mediaView.findViewById(R.id.btnRemove);
            TextView tvMediaCount = mediaView.findViewById(R.id.tvMediaCount);

            // Hiển thị thumbnail của video bằng Glide
            Glide.with(this)
                    .load(selectedVideoUri)
                    .thumbnail(0.25f)
                    .into(ivMedia);

            // Hiển thị số lượng (video chỉ có 1)
            tvMediaCount.setText("1/1");

            // Xử lý nút xóa
            btnRemove.setOnClickListener(v -> {
                selectedVideoUri = null;
                updateMediaContainer();
                Toast.makeText(this, "Đã xóa video", Toast.LENGTH_SHORT).show();
            });

            linearLayout.addView(mediaView);
        }

        // Thêm LinearLayout vào mediaContainer
        mediaContainer.addView(linearLayout);
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
        View btnAddImages = dialogView.findViewById(R.id.btnAddImages);
        View btnAddVideo = dialogView.findViewById(R.id.btnAddVideo);
        Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);
        mediaContainer = dialogView.findViewById(R.id.mediaContainer);

        // Cập nhật mediaContainer với các hình ảnh và video đã chọn
        updateMediaContainer();

        // Kiểm tra quyền trước khi cho phép chọn ảnh/video
        boolean hasStoragePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) :
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

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
            if (!hasStoragePermission) {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập để chọn ảnh!", Toast.LENGTH_SHORT).show();
                requestStoragePermissions();
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
            if (!hasStoragePermission) {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập để chọn video!", Toast.LENGTH_SHORT).show();
                requestStoragePermissions();
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

                        // Làm mới giao diện
                        loadCafeInfo();

                        // Không gọi setResult và finish, để giữ người dùng ở lại ReviewActivity
                    });
                })
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
    }
    
    private void updateCafeRating(float rating) {
        DocumentReference cafeRef = db.collection("cafes").document(cafeId);
        cafeRef.get().addOnSuccessListener(documentSnapshot -> {
            Long ratingCount = documentSnapshot.getLong("ratingCount");
            Double currentRating = documentSnapshot.getDouble("ratingStar");

            long newRatingCount = (ratingCount != null ? ratingCount : 0) + 1;
            double totalRating = (currentRating != null ? currentRating * (newRatingCount - 1) : 0) + rating;
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