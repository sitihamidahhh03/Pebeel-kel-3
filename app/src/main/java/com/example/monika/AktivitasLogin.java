package com.example.monika;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Random;

public class AktivitasLogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private DatabaseHelper dbHelper;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;
    
    private BottomSheetDialog captchaDialog;
    private boolean isPendingGoogleLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_login);

        dbHelper = new DatabaseHelper(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("630428215938-77g4an5cl3hchjkeua2jpgt4s0naccap.apps.googleusercontent.com")
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field", Toast.LENGTH_SHORT).show();
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

                // Hitung skala CenterCrop
                float scale;
                float dx = 0, dy = 0;
                if (fullBitmap.getWidth() * viewH > viewW * fullBitmap.getHeight()) {
                    scale = (float) viewH / (float) fullBitmap.getHeight();
                    dx = (viewW - fullBitmap.getWidth() * scale) * 0.5f;
                } else {
                    scale = (float) viewW / (float) fullBitmap.getWidth();
                    dy = (viewH - fullBitmap.getHeight() * scale) * 0.5f;
                }

                // Ukuran kepingan dalam pixel layar
                int pieceSizePx = (int) (55 * density);
                
                // Koordinat crop pada gambar asli
                int startXOnOriginal = (int) ((targetMarginXPx - dx) / scale);
                int startYOnOriginal = (int) (((viewH / 2) - (pieceSizePx / 2) + targetMarginYPx - dy) / scale);
                int sizeOnOriginal = (int) (pieceSizePx / scale);

                // Pastikan tidak keluar batas gambar asli
                startXOnOriginal = Math.max(0, Math.min(startXOnOriginal, fullBitmap.getWidth() - sizeOnOriginal));
                startYOnOriginal = Math.max(0, Math.min(startYOnOriginal, fullBitmap.getHeight() - sizeOnOriginal));

                // Potong dari gambar asli (Kualitas Tinggi)
                Bitmap pieceBitmap = Bitmap.createBitmap(fullBitmap, startXOnOriginal, startYOnOriginal, sizeOnOriginal, sizeOnOriginal);
                
                // Scale kepingan hasil potong ke ukuran layar dengan filter berkualitas tinggi
                Bitmap finalPiece = Bitmap.createScaledBitmap(pieceBitmap, pieceSizePx, pieceSizePx, true);
                Bitmap maskedBitmap = getMaskedBitmap(finalPiece, R.drawable.ic_puzzle_piece);
                
                ivPuzzlePiece.setImageBitmap(maskedBitmap);
                ivPuzzlePiece.clearColorFilter();
                ivPuzzlePiece.setImageTintList(null);
                
            } catch (Exception e) { e.printStackTrace(); }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float translationX = (progress / 100f) * (225 * density);
                ivPuzzlePiece.setTranslationX(translationX);
                if (tvHint != null) tvHint.setAlpha(1 - (progress / 50f));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float currentPosPx = ivPuzzlePiece.getTranslationX();
                if (Math.abs(currentPosPx - targetMarginXPx) <= (10 * density)) {
                    ivPuzzlePiece.setTranslationX(targetMarginXPx);
                    ivPuzzlePiece.setColorFilter(Color.parseColor("#4CAF50"), PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(AktivitasLogin.this, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show();
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (captchaDialog != null) captchaDialog.dismiss();
                        if (isPendingGoogleLogin) signInWithGoogle();
                        else handleLogin();
                    }, 600);
                } else {
                    seekBar.setProgress(0);
                    ivPuzzlePiece.setTranslationX(0);
                    if (tvHint != null) tvHint.setAlpha(1f);
                }
            }
        });
        captchaDialog.show();
    }

    private Bitmap getMaskedBitmap(Bitmap source, int maskResId) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Drawable maskDrawable = ContextCompat.getDrawable(this, maskResId);
        maskDrawable.setBounds(0, 0, width, height);
        maskDrawable.draw(canvas);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (dbHelper.checkUser(email, password)) {
            loginSuccess(email, dbHelper.getUserName(email));
        } else {
            Toast.makeText(this, "Email atau Password salah!", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
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
            if (dbHelper.isEmailExists(account.getEmail())) {
                loginSuccess(account.getEmail(), dbHelper.getUserName(account.getEmail()));
            } else {
                mGoogleSignInClient.signOut();
                Toast.makeText(this, "Email Google tidak terdaftar!", Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) { Toast.makeText(this, "Gagal login Google", Toast.LENGTH_SHORT).show(); }
    }

    private void loginSuccess(String email, String name) {
        SharedPreferences.Editor editor = getSharedPreferences("SyamPref", Context.MODE_PRIVATE).edit();
        editor.putString("email", email);
        editor.apply();
        Toast.makeText(this, "Selamat datang, " + name, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AktivitasLogin.this, DashboardActivity.class));
        finish();
    }
}
