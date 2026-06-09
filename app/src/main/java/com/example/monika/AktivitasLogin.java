package com.example.monika;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class AktivitasLogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    private BottomSheetDialog captchaDialog;
    private boolean isPendingGoogleLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_login);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupGoogleSignIn();
        startFinalAnimations();

        SharedPreferences pref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
        
        // Fitur Keamanan: Cek Inaktivitas 30 Menit
        long lastActive = pref.getLong("last_active_time", 0);
        long currentTime = System.currentTimeMillis();
        long thirtyMinutes = 30 * 60 * 1000;

        if (lastActive != 0 && (currentTime - lastActive) > thirtyMinutes) {
            // Sesi berakhir, paksa logout
            mAuth.signOut();
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("email");
            editor.remove("last_active_time");
            editor.apply();
            Toast.makeText(this, "Sesi berakhir. Silakan login kembali.", Toast.LENGTH_LONG).show();
        }

        if (mAuth.getCurrentUser() != null && !pref.getString("email", "").isEmpty()) {
            saveLastActiveTime();
            goToDashboard();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            if (validateInput()) {
                isPendingGoogleLogin = false;
                showPuzzleCaptcha();
            }
        });

        btnGoogleLogin.setOnClickListener(v -> {
            isPendingGoogleLogin = true;
            showPuzzleCaptcha();
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showPuzzleCaptcha() {
        captchaDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_captcha_bottom_sheet, null);
        captchaDialog.setContentView(view);

        SeekBar seekBar = view.findViewById(R.id.seekBarPuzzle);
        ImageView ivPuzzlePiece = view.findViewById(R.id.ivPuzzlePiece);
        ImageView ivPuzzleTarget = view.findViewById(R.id.ivPuzzleTarget);
        ImageView ivPuzzleBg = view.findViewById(R.id.ivPuzzleBg);
        TextView tvHint = view.findViewById(R.id.tvCaptchaHintSheet);

        Random random = new Random();
        int[] images = {R.drawable.pemandangan_1, R.drawable.pemandangan_2, R.drawable.pemandangan_3, R.drawable.pemandangan_4};
        ivPuzzleBg.setImageResource(images[random.nextInt(images.length)]);

        float density = getResources().getDisplayMetrics().density;
        int randomMarginX = random.nextInt(70) + 150;
        int randomMarginY = random.nextInt(50) - 25;

        ViewGroup.MarginLayoutParams targetParams = (ViewGroup.MarginLayoutParams) ivPuzzleTarget.getLayoutParams();
        final int targetMarginXPx = (int) (randomMarginX * density);
        final int targetMarginYPx = (int) (randomMarginY * density);
        targetParams.leftMargin = targetMarginXPx;
        targetParams.topMargin = targetMarginYPx;
        ivPuzzleTarget.setLayoutParams(targetParams);

        ivPuzzleBg.post(() -> {
            try {
                Bitmap fullBitmap = ((BitmapDrawable) ivPuzzleBg.getDrawable()).getBitmap();
                int viewW = ivPuzzleBg.getWidth();
                int viewH = ivPuzzleBg.getHeight();
                float scale = (fullBitmap.getWidth() * viewH > viewW * fullBitmap.getHeight()) ?
                        (float) viewH / (float) fullBitmap.getHeight() : (float) viewW / (float) fullBitmap.getWidth();
                float dx = (viewW - fullBitmap.getWidth() * scale) * 0.5f;
                float dy = (viewH - fullBitmap.getHeight() * scale) * 0.5f;

                int pieceSizePx = (int) (55 * density);
                int startXOnOriginal = (int) ((targetMarginXPx - dx) / scale);
                int startYOnOriginal = (int) (((viewH / 2) - (pieceSizePx / 2) + targetMarginYPx - dy) / scale);
                int sizeOnOriginal = (int) (pieceSizePx / scale);

                startXOnOriginal = Math.max(0, Math.min(startXOnOriginal, fullBitmap.getWidth() - sizeOnOriginal));
                startYOnOriginal = Math.max(0, Math.min(startYOnOriginal, fullBitmap.getHeight() - sizeOnOriginal));

                Bitmap pieceBitmap = Bitmap.createBitmap(fullBitmap, startXOnOriginal, startYOnOriginal, sizeOnOriginal, sizeOnOriginal);
                Bitmap finalPiece = Bitmap.createScaledBitmap(pieceBitmap, pieceSizePx, pieceSizePx, true);
                ivPuzzlePiece.setImageBitmap(getMaskedBitmap(finalPiece, R.drawable.ic_puzzle_piece));
            } catch (Exception e) { e.printStackTrace(); }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ivPuzzlePiece.setTranslationX((progress / 100f) * (225 * density));
                if (tvHint != null) tvHint.setAlpha(1 - (progress / 50f));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (Math.abs(ivPuzzlePiece.getTranslationX() - targetMarginXPx) <= (15 * density)) {
                    ivPuzzlePiece.setTranslationX(targetMarginXPx);
                    ivPuzzlePiece.setColorFilter(Color.parseColor("#4CAF50"), PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(AktivitasLogin.this, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> {
                        if (captchaDialog != null) captchaDialog.dismiss();
                        if (isPendingGoogleLogin) signInWithGoogle();
                        else handleLogin();
                    }, 600);
                } else {
                    seekBar.setProgress(0);
                    ivPuzzlePiece.setTranslationX(0);
                }
            }
        });
        captchaDialog.show();
    }

    private Bitmap getMaskedBitmap(Bitmap source, int maskResId) {
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Drawable mask = ContextCompat.getDrawable(this, maskResId);
        mask.setBounds(0, 0, source.getWidth(), source.getHeight());
        mask.draw(canvas);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserInDatabase(email);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Email atau Password salah!";
                        Toast.makeText(AktivitasLogin.this, "Login Gagal: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserInDatabase(String email) {
        String encodedEmail = email.replace(".", ",");
        FirebaseDatabase.getInstance("https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users")
                .child(encodedEmail)
                .get()
                .addOnCompleteListener(task -> {
                    saveLastActiveTime();
                    saveEmailToPref(email);
                    updateLastLogin(email);
                    Toast.makeText(AktivitasLogin.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                });
    }

    private void updateLastLogin(String email) {
        FirebaseDatabase.getInstance("https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("logs/last_login").setValue("User (" + email + ")");
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserInDatabase(user.getEmail());
                        }
                    } else {
                        Toast.makeText(AktivitasLogin.this, "Firebase Authentication Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveEmailToPref(String email) {
        getSharedPreferences("SyamPref", Context.MODE_PRIVATE).edit().putString("email", email).apply();
    }

    private void saveLastActiveTime() {
        getSharedPreferences("SyamPref", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_active_time", System.currentTimeMillis())
                .apply();
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void startFinalAnimations() {
        View header = findViewById(R.id.headerBackground);
        View logo = findViewById(R.id.ivLogo);
        View title = findViewById(R.id.tvLoginTitle);
        View orLine = findViewById(R.id.tvOR);
        View sepL = findViewById(R.id.separatorLeft);
        View sepR = findViewById(R.id.separatorRight);
        if (header == null) return;
        final View[] forms = {title, etEmail, etPassword, btnLogin, orLine, sepL, sepR, btnGoogleLogin};
        header.setTranslationY(-1000f);
        logo.setAlpha(0f);
        logo.setTranslationY(-500f);
        for (View v : forms) { if (v != null) { v.setAlpha(0f); v.setTranslationY(-300f); } }
        header.animate().translationY(0).setDuration(900).setInterpolator(new DecelerateInterpolator()).start();
        new Handler().postDelayed(() -> logo.animate().alpha(1f).translationY(0).scaleX(1f).scaleY(1f).setDuration(1000).setInterpolator(new OvershootInterpolator(1.2f)).start(), 400);
        for (int i = 0; i < forms.length; i++) { if (forms[i] != null) forms[i].animate().alpha(1f).translationY(0).setDuration(700).setStartDelay(800 + (i * 80)).start(); }
    }
}
