package com.example.monika;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
                    if (imageUri != null) startCrop(imageUri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        Bitmap bitmap = null;
                        // Prioritas: Ambil dari file URI (lebih aman untuk memory)
                        if (cropResultUri != null) {
                            bitmap = rotateBitmapIfRequired(this, cropResultUri);
                        }

                        // Fallback: Ambil dari Intent Extras
                        if (bitmap == null && result.getData() != null && result.getData().getExtras() != null) {
                            bitmap = result.getData().getExtras().getParcelable("data");
                        }

                        if (bitmap != null) {
                            uploadPhoto(bitmap);
                        } else {
                            Toast.makeText(this, "Gagal mengambil foto hasil potong", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("PROFIL", "Error processing crop", e);
                        Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        } else {
            SharedPreferences pref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
            userEmail = pref.getString("email", "");
        }

        if (userEmail == null || userEmail.isEmpty()) {
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

        tvEmailUser.setText(userEmail);
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
            getSharedPreferences("SyamPref", Context.MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(AktivitasProfil.this, AktivitasLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setProfileImage(String data) {
        if (data == null || data.isEmpty()) {
            ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
            ivFotoProfil.setImageTintList(null);
            return;
        }
        try {
            byte[] decodedString = Base64.decode(data, Base64.NO_WRAP);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (decodedByte != null) {
                ivFotoProfil.setImageBitmap(decodedByte);
                ivFotoProfil.setImageTintList(null);
            }
        } catch (Exception e) {
            ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
        }
    }

    private void loadUserData() {
        setProfileImage(dbHelper.getUserPhoto(userEmail));
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isFinishing()) {
                    String name = snapshot.child("nama").getValue(String.class);
                    if (name == null) name = snapshot.child("name").getValue(String.class);
                    String photoData = snapshot.child("photo_path").getValue(String.class);

                    tvNamaUser.setText(name != null ? name : "User");
                    if (photoData != null && !photoData.isEmpty()) {
                        setProfileImage(photoData);
                        dbHelper.updateUserPhoto(userEmail, photoData);
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

    private void startCrop(Uri uri) {
        try {
            // Gunakan Cache internal (lebih aman izinnya)
            File tempFile = new File(getCacheDir(), "profile_crop.jpg");
            if (tempFile.exists()) tempFile.delete();
            cropResultUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 400);
            intent.putExtra("outputY", 400);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropResultUri);
            intent.putExtra("return-data", false); // Gunakan URI file agar tidak memory error
            cropImageLauncher.launch(intent);
        } catch (Exception e) {
            saveImageToInternalStorage(uri);
        }
    }

    private void deleteProfilePhoto() {
        userRef.child("photo_path").setValue("").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dbHelper.updateUserPhoto(userEmail, null);
                ivFotoProfil.setImageResource(R.drawable.ic_account_circle);
                Toast.makeText(this, "Foto dihapus", Toast.LENGTH_SHORT).show();
            }
        });
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
                Map<String, Object> updates = new HashMap<>();
                updates.put("nama", newName);
                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbHelper.updateUserName(userEmail, newName);
                        Toast.makeText(this, "Nama diperbarui", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadPhoto(Bitmap bitmap) {
        try {
            // Kompresi gambar agar tidak terlalu besar di database
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);

            userRef.child("photo_path").setValue(encodedImage).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    dbHelper.updateUserPhoto(userEmail, encodedImage);
                    setProfileImage(encodedImage);
                    Toast.makeText(AktivitasProfil.this, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AktivitasProfil.this, "Gagal simpan ke Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            Bitmap bitmap = rotateBitmapIfRequired(this, uri);
            if (bitmap != null) uploadPhoto(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private static Bitmap rotateBitmapIfRequired(Context context, Uri selectedImage) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();

        InputStream input2 = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei = new ExifInterface(input2);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        input2.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180: return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270: return rotateImage(bitmap, 270);
            default: return bitmap;
        }
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
