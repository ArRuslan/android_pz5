package me.ruslan.task5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final int MIN_FONT_SIZE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        setTheme(prefs);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        int currentFont = prefs.getInt("font_size", 4) + MIN_FONT_SIZE;
        String theme = prefs.getString("theme", "light");

        TextView fontText = findViewById(R.id.font_text);
        TextView themeText = findViewById(R.id.theme_text);
        fontText.setTextSize(currentFont);
        themeText.setTextSize(currentFont);

        Spinner themeSpinner = findViewById(R.id.themeSpinner);
        themeSpinner.setSelection(theme.equals("dark") ? 1 : 0);
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeTheme(prefs, i == 1 ? "dark" : "light");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextView fontSizeText = findViewById(R.id.fontSizeText);
        fontSizeText.setText("" + currentFont);

        SeekBar fontSize = findViewById(R.id.fontSizeSeekBar);
        fontSize.setProgress(currentFont - MIN_FONT_SIZE);
        fontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int font = MIN_FONT_SIZE + i;
                fontText.setTextSize(font);
                themeText.setTextSize(font);
                fontSizeText.setTextSize(font);
                fontSizeText.setText("" + font);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("font_size", seekBar.getProgress());
                editor.apply();
            }
        });
    }

    private void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void setTheme(SharedPreferences prefs) {
        String theme = prefs.getString("theme", "light");
        getTheme().applyStyle(theme.equals("dark") ? R.style.Theme_Task5Dark : R.style.Theme_Task5Light, true);
    }

    private void changeTheme(SharedPreferences prefs, String theme) {
        if (prefs.getString("theme", "light").equals(theme))
            return;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("theme", theme);
        editor.apply();

        restart();
    }
}