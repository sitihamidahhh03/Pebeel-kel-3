package com.example.monika;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.monika.ui.HeaderManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AktivitasProfil extends AppCompatActivity {

    private TextView tvNamaUser, tvEmailUser;
    private ImageView ivFotoProfil, btnEditNama, btnUbahFoto;
    private String userEmail;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    private Uri cropResultUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // 1. Salin foto dari galeri ke folder internal aplikasi dulu agar aman
                        Uri localUri = copyToInternal(imageUri);
                        if (localUri != null) {
                            // 2. Baru panggil fungsi potong (Crop) menggunakan file lokal
                            startCrop(localUri);
                        }
                    }
                }
            }
    );

    private Uri copyToInternal(Uri uri) {
        try {
            File tempFile = new File(getCacheDir(), "temp_input.jpg");
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.close();
            return FileProvider.getUriForFile(this, "com.syram.monika.fileprovider", tempFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 1. Coba ambil dari URI hasil crop (paling stabil)
                    if (cropResultUri != null) {
                        saveImageToInternalStorage(cropResultUri);
                        return;
                    }

                    // 2. Fallback: Coba ambil dari extras jika URI null
                    if (result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.containsKey("data")) {
                            Bitmap bitmap = extras.getParcelable("data");
                            if (bitmap != null) {
                                saveBitmapToInternalStorage(bitmap);
                                return;
                            }
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_profil);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
        userEmail = pref.getString("email", "");
        
        if (userEmail.isEmpty()) {
            finish();
            return;
        }

        String encodedEmail = userEmail.replace(".", ",");
        // SESUAIKAN: Menggunakan 'users' (u kecil) sesuai dengan screenshot database Firebase Anda
        userRef = FirebaseDatabase.getInstance("https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(encodedEmail);

        tvNamaUser = findViewById(R.id.tvNamaUser);
        tvEmailUser = findViewById(R.id.tvEmailUser);
        ivFotoProfil = findViewById(R.id.ivFotoProfil);
        btnEditNama = findViewById(R.id.btnEditNama);
        btnUbahFoto = findViewById(R.id.btnUbahFoto);
        Button btnLogout = findViewById(R.id.btnLogout);

        loadUserData();

        HeaderManager header = new HeaderManager(this);
        header.setHeaderTitle("Profil");
        header.showBackButton(true);
        header.showProfileIcon(false);

        btnEditNama.setOnClickListener(v -> showEditNameDialog());
        btnUbahFoto.setOnClickListener(v -> showPhotoOptionsDialog());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.signOut();

            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(AktivitasProfil.this, AktivitasLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setProfileImage(String path) {
        if (path == null || path.isEmpty()) {
            ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
            return;
        }
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            if (myBitmap != null) {
                ivFotoProfil.setImageBitmap(myBitmap);
                ivFotoProfil.setImageTintList(null);
            } else {
                ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
            }
        } else {
            ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
        }
    }

    private void loadUserData() {
        tvEmailUser.setText(userEmail);
        setProfileImage(dbHelper.getUserPhoto(userEmail));

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isFinishing()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String photoPath = snapshot.child("photo_path").getValue(String.class);
                    tvNamaUser.setText(name != null ? name : "User");
                    if (photoPath != null && !photoPath.isEmpty()) {
                        setProfileImage(photoPath);
                        dbHelper.updateUserPhoto(userEmail, photoPath);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"Pilih dari Galeri", "Hapus Foto", "Batal"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto Profil");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkPermissionAndOpenGallery();
            } else if (which == 1) {
                deleteProfilePhoto();
            }
        });
        builder.show();
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
            } else {
                openGallery();
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(Intent.createChooser(intent, "Pilih Foto Profil"));
        } catch (Exception e) {
            Toast.makeText(this, "Akses galeri gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Izin galeri ditolak. Silakan aktifkan di pengaturan HP.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCrop(Uri sourceUri) {
        try {
            // Buat file untuk menampung hasil potongan
            File cropFile = new File(getFilesDir(), "profile_crop.jpg");
            cropResultUri = FileProvider.getUriForFile(this, "com.syram.monika.fileprovider", cropFile);

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(sourceUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 512);
            cropIntent.putExtra("outputY", 512);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", false);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropResultUri);
            
            // Berikan izin akses penuh agar aplikasi pemotong bisa membaca & menulis
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            
            // Berikan izin ke semua aplikasi yang mampu menangani intent ini
            java.util.List<android.content.pm.ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cropIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (android.content.pm.ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, cropResultUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                grantUriPermission(packageName, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            cropImageLauncher.launch(cropIntent);
        } catch (Exception e) {
            // Jika fitur Crop benar-benar tidak ada di HP tersebut, langsung simpan foto aslinya
            saveImageToInternalStorage(sourceUri);
        }
    }

    private void deleteProfilePhoto() {
        userRef.child("photo_path").removeValue();
        dbHelper.updateUserPhoto(userEmail, null);
        ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
        Toast.makeText(this, "Foto profil dihapus", Toast.LENGTH_SHORT).show();
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubah Nama");
        final EditText input = new EditText(this);
        input.setText(tvNamaUser.getText().toString());
        builder.setView(input);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                userRef.child("name").setValue(newName);
                dbHelper.updateUserName(userEmail, newName);
                Toast.makeText(this, "Nama diperbarui", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();
            String photoPath = file.getAbsolutePath();
            
            // Simpan ke Firebase
            userRef.child("photo_path").setValue(photoPath).addOnFailureListener(e -> {
                Toast.makeText(AktivitasProfil.this, "Gagal sinkron ke Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            
            dbHelper.updateUserPhoto(userEmail, photoPath);
            setProfileImage(photoPath);
            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.close();
            String photoPath = file.getAbsolutePath();
            
            // Simpan ke Firebase
            userRef.child("photo_path").setValue(photoPath).addOnFailureListener(e -> {
                Toast.makeText(AktivitasProfil.this, "Gagal sinkron ke Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            
            dbHelper.updateUserPhoto(userEmail, photoPath);
            setProfileImage(photoPath);
            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memuat foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
