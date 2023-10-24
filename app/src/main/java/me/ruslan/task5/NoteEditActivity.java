package me.ruslan.task5;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import me.ruslan.task5.db.DBHelper;
import me.ruslan.task5.models.Note;

public class NoteEditActivity extends AppCompatActivity {
    private EditText title;
    private EditText text;
    private DBHelper db;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");
        int font = prefs.getInt("font_size", 4) + SettingsActivity.MIN_FONT_SIZE;
        setTheme(theme.equals("dark") ? R.style.Theme_Task5Dark : R.style.Theme_Task5Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        db = new DBHelper(this);

        title = findViewById(R.id.note_title);
        text = findViewById(R.id.note_text);

        note = (Note) getIntent().getSerializableExtra("note");
        title.setText(note.getTitle());
        text.setText(note.getText() == null ? "" : note.getText());

        title.setTextSize(font);
        text.setTextSize(font);
    }

    @Override
    protected void onPause() {
        note.setTitle(title.getText().toString());
        note.setText(text.getText().toString());
        db.updateNote(note);

        super.onPause();
    }
}