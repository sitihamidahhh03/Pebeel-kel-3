package com.example.monika;

import android.content.Context;import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
    private Uri tempImageUri;
    private Uri cropResultUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    tempImageUri = result.getData().getData();
                    if (tempImageUri != null) {
                        showCropOptionDialog(tempImageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    boolean savedFromCrop = false;
                    if (cropResultUri != null) {
                        File cropFile = new File(getCacheDir(), "crop_output.jpg");
                        if (cropFile.exists() && cropFile.length() > 0) {
                            saveImageToInternalStorage(cropResultUri);
                            savedFromCrop = true;
                        }
                    }
                    if (!savedFromCrop && tempImageUri != null) {
                        saveImageToInternalStorage(tempImageUri);
                    }
                } else {
                    // Jika crop dibatalkan, tetap simpan foto asli
                    if (tempImageUri != null) {
                        saveImageToInternalStorage(tempImageUri);
                        Toast.makeText(this, "Foto asli disimpan (tanpa crop)", Toast.LENGTH_SHORT).show();
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
            try {
                // Downsampling untuk menghindari OutOfMemory pada gambar besar
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                options.inSampleSize = calculateInSampleSize(options, 512, 512);
                options.inJustDecodeBounds = false;

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                if (myBitmap != null) {
                    ivFotoProfil.setImageBitmap(myBitmap);
                    ivFotoProfil.setImageTintList(null);
                } else {
                    ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
                }
            } catch (Exception e) {
                ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
            }
        } else {
            ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            } else if (which == 1) {
                deleteProfilePhoto();
            }
        });
        builder.show();
    }

    private void showCropOptionDialog(Uri uri) {
        String[] options = {"Potong Foto (Crop)", "Gunakan Langsung"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opsi Foto");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                startCrop(uri);
            } else {
                saveImageToInternalStorage(uri);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void startCrop(Uri uri) {
        try {
            File cropFile = new File(getCacheDir(), "crop_output.jpg");
            if (cropFile.exists()) cropFile.delete();
            cropResultUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cropFile);

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 512);
            cropIntent.putExtra("outputY", 512);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", false);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropResultUri);
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cropImageLauncher.launch(cropIntent);
        } catch (Exception e) {
            saveImageToInternalStorage(uri);
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
                tvNamaUser.setText(newName);
                Toast.makeText(this, "Nama diperbarui", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveImageToInternalStorage(Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return;

            File file = new File(getFilesDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            String photoPath = file.getAbsolutePath();
            userRef.child("photo_path").setValue(photoPath);
            dbHelper.updateUserPhoto(userEmail, photoPath);
            setProfileImage(photoPath);
            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (Exception ignored) {}
        }
    }
}
