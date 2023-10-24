package me.ruslan.task5;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import me.ruslan.task5.models.Note;

public class NoteEditActivity extends AppCompatActivity {
    private int real_index;
    private EditText title;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");
        int font = prefs.getInt("font_size", 4) + SettingsActivity.MIN_FONT_SIZE;
        setTheme(theme.equals("dark") ? R.style.Theme_Task5Dark : R.style.Theme_Task5Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        title = findViewById(R.id.note_title);
        text = findViewById(R.id.note_text);

        real_index = getIntent().getIntExtra("real_index", -1);
        Note note = (Note)getIntent().getSerializableExtra("note");
        title.setText(note.getTitle());
        text.setText(note.getText() == null ? "" : note.getText());

        title.setTextSize(font);
        text.setTextSize(font);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(real_index == -1)
            return;
        MainActivity.updateNote(real_index, title.getText().toString(), text.getText().toString());
    }
}