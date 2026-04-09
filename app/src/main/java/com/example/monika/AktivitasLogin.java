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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// Tambahkan import untuk Google Sign-In
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AktivitasLogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private DatabaseHelper dbHelper;

    // Variabel untuk Google Login
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_login);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // 1. Konfigurasi Google Sign-In
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

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        // Melakukan signOut terlebih dahulu agar jendela "Pilih Akun" selalu muncul
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

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
            String email = account.getEmail();

            // LOGIKA: Cek apakah email Google ini terdaftar di SQLite
            if (dbHelper.isEmailExists(email)) {
                String name = dbHelper.getUserName(email);
                loginSuccess(email, name);
            } else {
                // Logout Google jika tidak terdaftar agar tidak auto-login di percobaan berikutnya
                mGoogleSignInClient.signOut();
                Toast.makeText(this, "Email Google ini tidak terdaftar di sistem!", Toast.LENGTH_LONG).show();
            }

        } catch (ApiException e) {
            Log.w("Google Login", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Gagal login Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        // LOGIKA: Validasi Email dan Password menggunakan SQLite
        if (dbHelper.checkUser(email, password)) {
            String name = dbHelper.getUserName(email);
            loginSuccess(email, name);
        } else {
            Toast.makeText(this, "Email atau Password salah!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginSuccess(String email, String name) {
        saveEmailToSharedPref(email);
        Toast.makeText(this, "Selamat datang, " + name, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AktivitasLogin.this, DashboardActivity.class));
        finish();
    }

    private void saveEmailToSharedPref(String email) {
        SharedPreferences sharedPref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email", email);
        editor.apply();
    }
}
