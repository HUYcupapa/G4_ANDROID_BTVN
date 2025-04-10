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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapter.AdminCafeAdapter;
import com.example.myapplication.Model.CafeAdmin;
import com.example.myapplication.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.json.JSONObject;

public class AdminActivity extends AppCompatActivity implements AdminCafeAdapter.OnCafeActionListener {

    private RecyclerView rvCafes;
    private AdminCafeAdapter cafeAdapter;
    private List<CafeAdmin> cafeList;
    private FirebaseFirestore db;
    private Button btnAddCafe;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> videoPickerLauncher;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private Uri selectedVideoUri = null;
    private List<String> uploadedImageUrls = new ArrayList<>();
    private String uploadedVideoUrl = null;
    private OkHttpClient client;
    private static final String IMGUR_CLIENT_ID = "44708ec159ebd14";
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/upload";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private HorizontalScrollView mediaContainer;
    private LatLng selectedLocation;
    private PlacesClient placesClient;
    private String adminEmail = "admin@example.com"; // Giả định email của Admin, có thể lấy từ đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        client = new OkHttpClient();

        // Yêu cầu quyền truy cập bộ nhớ
        requestStoragePermissions();

        // Khởi tạo Places API
        Places.initialize(getApplicationContext(), "AIzaSyD88lQgGXHV5qERgU5cj0Q_F5j01i1dy6s");
        placesClient = Places.createClient(this);

        // Khởi tạo RecyclerView
        rvCafes = findViewById(R.id.rv_cafes);
        cafeList = new ArrayList<>();
        cafeAdapter = new AdminCafeAdapter(this, cafeList, this);
        rvCafes.setLayoutManager(new LinearLayoutManager(this));
        rvCafes.setAdapter(cafeAdapter);

        // Khởi tạo nút Thêm Quán
        btnAddCafe = findViewById(R.id.btn_add_cafe);
        btnAddCafe.setOnClickListener(v -> showAddEditCafeDialog(null));

        // Khởi tạo ActivityResultLauncher để chọn hình ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (selectedImageUris.size() < 3) {
                            selectedImageUris.add(uri);
                            Toast.makeText(this, "Đã chọn " + selectedImageUris.size() + "/3 hình ảnh", Toast.LENGTH_SHORT).show();
                            updateMediaContainer();
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
                            updateMediaContainer();
                        } else {
                            Toast.makeText(this, "Chỉ được chọn 1 video!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Tải danh sách quán cà phê từ collection cafe_admin
        loadCafes();
    }

    private void loadCafes() {
        db.collection("cafe_admin")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi khi tải danh sách quán!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cafeList.clear();
                    if (value != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : value) {
                            CafeAdmin cafe = document.toObject(CafeAdmin.class);
                            if (cafe != null) {
                                cafe.setId(document.getId());
                                cafeList.add(cafe);
                            }
                        }
                    }
                    cafeAdapter.notifyDataSetChanged();
                });
    }

    private void showAddEditCafeDialog(CafeAdmin cafe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_cafe, null);
        builder.setView(dialogView);

        // Khởi tạo các thành phần trong dialog
        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etCafeName = dialogView.findViewById(R.id.et_cafe_name);
        EditText etCafeAddress = dialogView.findViewById(R.id.et_cafe_address);
        Button btnSelectLocation = dialogView.findViewById(R.id.btn_select_location);
        Spinner spinnerActivity = dialogView.findViewById(R.id.spinner_activity);
        EditText etOtherActivity = dialogView.findViewById(R.id.et_other_activity);
        LinearLayout layoutOtherActivity = dialogView.findViewById(R.id.layout_other_activity);
        EditText etCafeDescription = dialogView.findViewById(R.id.et_cafe_description);
        Button btnAddImages = dialogView.findViewById(R.id.btn_add_images);
        Button btnAddVideo = dialogView.findViewById(R.id.btn_add_video);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        mediaContainer = dialogView.findViewById(R.id.media_container);

        // Reset dữ liệu media
        selectedImageUris.clear();
        selectedVideoUri = null;
        uploadedImageUrls.clear();
        uploadedVideoUrl = null;
        selectedLocation = null;

        // Thiết lập tiêu đề và dữ liệu nếu là chỉnh sửa
        if (cafe != null) {
            tvDialogTitle.setText("Sửa Quán Cà Phê");
            etCafeName.setText(cafe.getName());
            etCafeAddress.setText(cafe.getLocationText());
            etCafeDescription.setText(cafe.getDescription());
            if (cafe.getImage1() != null && !cafe.getImage1().isEmpty()) {
                uploadedImageUrls.add(cafe.getImage1());
            }
            if (cafe.getImage2() != null && !cafe.getImage2().isEmpty()) {
                uploadedImageUrls.add(cafe.getImage2());
            }
            if (cafe.getImage3() != null && !cafe.getImage3().isEmpty()) {
                uploadedImageUrls.add(cafe.getImage3());
            }
            if (cafe.getVideo() != null && !cafe.getVideo().isEmpty()) {
                uploadedVideoUrl = cafe.getVideo();
            }
            if (cafe.getLat() != 0 && cafe.getLng() != 0) {
                selectedLocation = new LatLng(cafe.getLat(), cafe.getLng());
            }
        } else {
            tvDialogTitle.setText("Thêm Quán Cà Phê");
        }

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.activity_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        // Đặt giá trị mặc định cho Spinner nếu là chỉnh sửa
        if (cafe != null && cafe.getActivity() != null) {
            String activity = cafe.getActivity();
            if (!activity.equals("Boardgame") && !activity.equals("Book") && !activity.equals("Workshop")) {
                spinnerActivity.setSelection(adapter.getPosition("others"));
                etOtherActivity.setText(activity);
                layoutOtherActivity.setVisibility(View.VISIBLE);
            } else {
                spinnerActivity.setSelection(adapter.getPosition(activity));
            }
        }

        // Xử lý khi chọn "others" trong Spinner
        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals("others")) {
                    layoutOtherActivity.setVisibility(View.VISIBLE);
                } else {
                    layoutOtherActivity.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutOtherActivity.setVisibility(View.GONE);
            }
        });

        // Xử lý nút Chọn Vị Trí
        btnSelectLocation.setOnClickListener(v -> showPlacePicker());

        // Xử lý nút Thêm Hình Ảnh
        btnAddImages.setOnClickListener(v -> {
            if (!hasStoragePermission()) {
                requestStoragePermissions();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        // Xử lý nút Thêm Video
        btnAddVideo.setOnClickListener(v -> {
            if (!hasStoragePermission()) {
                requestStoragePermissions();
                return;
            }
            videoPickerLauncher.launch("video/*");
        });

        // Cập nhật media container
        updateMediaContainer();

        // Xử lý nút Xác Nhận
        AlertDialog dialog = builder.create();
        btnConfirm.setOnClickListener(v -> {
            String name = etCafeName.getText().toString().trim();
            String address = etCafeAddress.getText().toString().trim();
            String description = etCafeDescription.getText().toString().trim();
            String activity = spinnerActivity.getSelectedItem().toString();
            String otherActivity = etOtherActivity.getText().toString().trim();

            // Kiểm tra dữ liệu
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên quán!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedLocation == null) {
                Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (activity.equals("Chọn hoạt động")) {
                activity = null;
            }
            if (activity != null && activity.equals("others")) {
                if (otherActivity.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mô tả hoạt động!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (otherActivity.length() > 50) {
                    Toast.makeText(this, "Mô tả hoạt động không được vượt quá 50 ký tự!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (selectedImageUris.isEmpty() && uploadedImageUrls.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 hình ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload media và lưu quán
            uploadMediaAndSaveCafe(cafe, name, address, description, activity, otherActivity, dialog);
        });

        dialog.show();
    }

    private void uploadMediaAndSaveCafe(CafeAdmin cafe, String name, String address, String description,
                                        String activity, String otherActivity, AlertDialog dialog) {
        // Nếu không có hình ảnh mới để upload, sử dụng URL hiện tại (trong trường hợp chỉnh sửa)
        if (selectedImageUris.isEmpty() && !uploadedImageUrls.isEmpty()) {
            if (selectedVideoUri != null) {
                uploadVideo(cafe, name, address, description, activity, otherActivity, dialog);
            } else {
                saveCafe(cafe, name, address, description, activity, otherActivity, dialog);
            }
            return;
        }

        // Upload hình ảnh
        int totalImages = selectedImageUris.size();
        final int[] uploadedCount = {0};
        uploadedImageUrls.clear();

        for (Uri imageUri : selectedImageUris) {
            try {
                byte[] imageBytes = readBytesFromUri(imageUri);
                if (imageBytes == null) {
                    Toast.makeText(this, "Không thể đọc dữ liệu hình ảnh!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/*"), imageBytes))
                        .addFormDataPart("type", "file")
                        .build();

                Request request = new Request.Builder()
                        .url(IMGUR_UPLOAD_URL)
                        .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminActivity.this, "Lỗi khi upload hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                                if (uploadedCount[0] == totalImages) {
                                    if (selectedVideoUri != null) {
                                        uploadVideo(cafe, name, address, description, activity, otherActivity, dialog);
                                    } else {
                                        saveCafe(cafe, name, address, description, activity, otherActivity, dialog);
                                    }
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(AdminActivity.this, "Lỗi khi phân tích phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(AdminActivity.this, "Lỗi khi upload hình ảnh: " + response.message(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                        response.close();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminActivity.this, "Lỗi khi xử lý hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
        }
    }

    private void uploadVideo(CafeAdmin cafe, String name, String address, String description,
                             String activity, String otherActivity, AlertDialog dialog) {
        try {
            byte[] videoBytes = readBytesFromUri(selectedVideoUri);
            if (videoBytes == null) {
                Toast.makeText(this, "Không thể đọc dữ liệu video!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("video", "video.mp4", RequestBody.create(MediaType.parse("video/*"), videoBytes))
                    .addFormDataPart("type", "file")
                    .addFormDataPart("disable_audio", "0")
                    .build();

            Request request = new Request.Builder()
                    .url(IMGUR_UPLOAD_URL)
                    .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminActivity.this, "Lỗi khi upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            saveCafe(cafe, name, address, description, activity, otherActivity, dialog);
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(AdminActivity.this, "Lỗi khi phân tích phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminActivity.this, "Lỗi khi upload video: " + response.message(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(AdminActivity.this, "Lỗi khi xử lý video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
    }

    private void saveCafe(CafeAdmin cafe, String name, String address, String description,
                          String activity, String otherActivity, AlertDialog dialog) {
        Map<String, Object> cafeData = new HashMap<>();
        cafeData.put("name", name);
        cafeData.put("locationText", address);
        cafeData.put("description", description);
        cafeData.put("activity", activity != null && activity.equals("others") ? otherActivity : activity);
        cafeData.put("image1", uploadedImageUrls.size() > 0 ? uploadedImageUrls.get(0) : "");
        cafeData.put("image2", uploadedImageUrls.size() > 1 ? uploadedImageUrls.get(1) : "");
        cafeData.put("image3", uploadedImageUrls.size() > 2 ? uploadedImageUrls.get(2) : "");
        cafeData.put("video", uploadedVideoUrl != null ? uploadedVideoUrl : "");
        cafeData.put("lat", selectedLocation.latitude);
        cafeData.put("lng", selectedLocation.longitude);
        cafeData.put("location", new GeoPoint(selectedLocation.latitude, selectedLocation.longitude));
        cafeData.put("createdBy", adminEmail);

        if (cafe == null) {
            // Thêm quán mới vào collection cafe_admin
            db.collection("cafe_admin")
                    .add(cafeData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Thêm quán thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi thêm quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        } else {
            // Sửa quán trong collection cafe_admin
            db.collection("cafe_admin")
                    .document(cafe.getId())
                    .set(cafeData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Sửa quán thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi sửa quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        }
    }

    private void showPlacePicker() {
        AutocompleteSupportFragment autocompleteFragment = new AutocompleteSupportFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, autocompleteFragment)
                .addToBackStack(null)
                .commit();

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                selectedLocation = place.getLatLng();
                getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(AdminActivity.this, "Lỗi khi chọn vị trí: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void updateMediaContainer() {
        if (mediaContainer == null) return;

        mediaContainer.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                450
        ));
        linearLayout.setPadding(8, 8, 8, 8);

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);
            View mediaView = LayoutInflater.from(this).inflate(R.layout.item_media, linearLayout, false);

            ImageView ivMedia = mediaView.findViewById(R.id.ivMedia);
            ImageButton btnRemove = mediaView.findViewById(R.id.btnRemove);
            TextView tvMediaCount = mediaView.findViewById(R.id.tvMediaCount);

            ivMedia.setImageURI(uri);
            tvMediaCount.setText((i + 1) + "/3");

            btnRemove.setOnClickListener(v -> {
                selectedImageUris.remove(uri);
                updateMediaContainer();
                Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
            });

            linearLayout.addView(mediaView);
        }

        if (selectedVideoUri != null) {
            View mediaView = LayoutInflater.from(this).inflate(R.layout.item_media, linearLayout, false);

            ImageView ivMedia = mediaView.findViewById(R.id.ivMedia);
            ImageButton btnRemove = mediaView.findViewById(R.id.btnRemove);
            TextView tvMediaCount = mediaView.findViewById(R.id.tvMediaCount);

            Picasso.get().load(selectedVideoUri).into(ivMedia);
            tvMediaCount.setText("1/1");

            btnRemove.setOnClickListener(v -> {
                selectedVideoUri = null;
                updateMediaContainer();
                Toast.makeText(this, "Đã xóa video", Toast.LENGTH_SHORT).show();
            });

            linearLayout.addView(mediaView);
        }

        mediaContainer.addView(linearLayout);
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

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền truy cập bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] readBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
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

    @Override
    public void onEditClick(CafeAdmin cafe) {
        showAddEditCafeDialog(cafe);
    }

    @Override
    public void onDeleteClick(CafeAdmin cafe) {
        new AlertDialog.Builder(this)
                .setTitle("Xác Nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa quán " + cafe.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("cafe_admin")
                            .document(cafe.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Xóa quán thành công!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi xóa quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}