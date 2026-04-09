package com.example.monika;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// Tambahkan import untuk Google Sign-In
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class AktivitasLogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;

    // Variabel untuk Google Login
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // 1. Konfigurasi Google Sign-In menggunakan Web Client ID yang baru saja dibuat
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("630428215938-77g4an5cl3hchjkeua2jpgt4s0naccap.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // 2. Trigger Login Google
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // C. Menangani Hasil Login - Sesuai Foto
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            
            // Berhasil! Kamu bisa ambil data:
            String email = account.getEmail();
            String displayName = account.getDisplayName();

            // Simpan email ke SharedPreferences agar tetap sinkron
            saveEmailToSharedPref(email);

            Toast.makeText(this, "Login Berhasil: " + displayName, Toast.LENGTH_SHORT).show();

            // Lanjutkan ke DashboardActivity
            startActivity(new Intent(AktivitasLogin.this, DashboardActivity.class));
            finish();

        } catch (ApiException e) {
            // Tangkap error jika gagal
            Log.w("Google Login", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Gagal login Google (Error: " + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field", Toast.LENGTH_SHORT).show();
        } else {
            saveEmailToSharedPref(email);
            Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AktivitasLogin.this, DashboardActivity.class));
            finish();
        }
    }

    private void saveEmailToSharedPref(String email) {
        SharedPreferences sharedPref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email", email);
        editor.apply();
    }
}
