package com.example.monika;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monika.ui.HeaderManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AktivitasProfil extends AppCompatActivity {

    private TextView tvNamaUser, tvEmailUser;
    private ImageView ivFotoProfil, btnEditNama, btnUbahFoto;
    private DatabaseHelper dbHelper;
    private String userEmail;

    // Launcher untuk memilih gambar dari galeri
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    // Setelah memilih, langsung panggil fungsi crop
                    startCrop(imageUri);
                }
            }
    );

    // Launcher untuk menangani hasil crop
    private final ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap bitmap = extras.getParcelable("data");
                        if (bitmap != null) {
                            saveBitmapToInternalStorage(bitmap);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_profil);

        dbHelper = new DatabaseHelper(this);
        
        SharedPreferences pref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
        userEmail = pref.getString("email", "");

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

        // Menampilkan pilihan saat tombol kamera diklik
        btnUbahFoto.setOnClickListener(v -> showPhotoOptionsDialog());

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(AktivitasProfil.this, AktivitasLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        if (!userEmail.isEmpty()) {
            tvEmailUser.setText(userEmail);
            tvNamaUser.setText(dbHelper.getUserName(userEmail));
            
            String photoPath = dbHelper.getUserPhoto(userEmail);
            if (photoPath != null && !photoPath.isEmpty()) {
                ivFotoProfil.setImageURI(Uri.parse(photoPath));
                ivFotoProfil.setImageTintList(null); // Menghapus tint agar foto asli terlihat
            } else {
                ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
            }
        }
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"Pilih dari Galeri", "Hapus Foto", "Batal"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto Profil");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            } else if (which == 1) {
                deleteProfilePhoto();
            }
        });
        builder.show();
    }

    private void startCrop(Uri uri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 512);
            cropIntent.putExtra("outputY", 512);
            cropIntent.putExtra("return-data", true);
            cropImageLauncher.launch(cropIntent);
        } catch (Exception e) {
            // Jika perangkat tidak mendukung crop intent bawaan, simpan langsung
            saveImageToInternalStorage(uri);
        }
    }

    private void deleteProfilePhoto() {
        String photoPath = dbHelper.getUserPhoto(userEmail);
        if (photoPath != null && !photoPath.isEmpty()) {
            File file = new File(photoPath);
            if (file.exists()) {
                file.delete();
            }
        }
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
                dbHelper.updateUserName(userEmail, newName);
                tvNamaUser.setText(newName);
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
            dbHelper.updateUserPhoto(userEmail, photoPath);
            ivFotoProfil.setImageURI(Uri.fromFile(file));
            ivFotoProfil.setImageTintList(null);

            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show();
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
            dbHelper.updateUserPhoto(userEmail, photoPath);
            ivFotoProfil.setImageURI(Uri.fromFile(file));
            ivFotoProfil.setImageTintList(null);
            
            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show();
        }
    }
}
