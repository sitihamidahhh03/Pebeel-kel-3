package com.example.monika;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        Button btnAction1 = findViewById(R.id.btnAction1);
        Button btnAction2 = findViewById(R.id.btnAction2);

        if (btnAction1 != null) {
            btnAction1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SuggestionActivity.this, "Memulai penyiraman tanaman...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnAction2 != null) {
            btnAction2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SuggestionActivity.this, "Penyiraman ditunda.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
