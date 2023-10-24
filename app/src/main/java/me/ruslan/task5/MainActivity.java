package me.ruslan.task5;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.ruslan.task5.adapters.NotesListAdapter;
import me.ruslan.task5.db.DBHelper;
import me.ruslan.task5.models.Note;


public class MainActivity extends AppCompatActivity {
    private NotesListAdapter adapter;
    private String searchQuery = "";
    private int filterPriority = 7;
    private String savedTheme;
    private SharedPreferences prefs;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        setTheme(prefs);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);

        ListView notes_list = findViewById(R.id.notes_list);

        adapter = new NotesListAdapter(db.getNotes(), getApplicationContext(), prefs);
        notes_list.setAdapter(adapter);

        notes_list.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
            intent.putExtra("note", adapter.getFiltered(i));
            startActivity(intent);
        });

        notes_list.setOnItemLongClickListener((adapterView, view, i, l) -> {
            showNoteManageDialog(adapter.getFiltered(i));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setNotes(db.getNotes());

        if(!prefs.getString("theme", "light").equals(savedTheme))
            restart();
    }

    private void setTheme(SharedPreferences prefs) {
        String theme = prefs.getString("theme", "light");
        setTheme(theme.equals("dark") ? R.style.Theme_Task5Dark : R.style.Theme_Task5Light);
        savedTheme = theme;
    }

    private void restart() {
        finish();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    public void pickNoteImage(int noteId) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.int_select_file)), 1000+noteId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode >= 1000) {
            Note note = db.getNote(requestCode-1000);
            if(note == null) return;

            File outFile = new File(getFilesDir(), System.currentTimeMillis() + ".png");
            try {
                copyFile(data.getData(), outFile);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error)+e.getMessage(), Toast.LENGTH_LONG).show();
            }
            note.setImage(outFile.getAbsolutePath());
            db.updateNote(note);
            adapter.setNote(note);
            adapter.notifyDataSetChanged();
        }
    }

    public void copyFile(Uri uri, File destinationFile) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        destinationFile.getParentFile().mkdirs();
        OutputStream outputStream = new FileOutputStream(destinationFile);

        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search) {
            showSearchDialog();
            return true;
        } else if (id == R.id.menu_filter) {
            showFilterDialog();
            return true;
        } else if (id == R.id.menu_create) {
            showCreateDialog();
            return true;
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNoteManageDialog(Note note) {
        AlertDialog.Builder noteDialog = new AlertDialog.Builder(MainActivity.this);
        noteDialog.setTitle(String.format(getString(R.string.dialog_manage_note_s), note.getTitle()));

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        Button iconBtn = new Button(getApplicationContext());
        iconBtn.setText(R.string.dialog_set_icon);
        Button priorityBtn = new Button(getApplicationContext());
        priorityBtn.setText(R.string.dialog_set_priority);
        Button deleteBtn = new Button(getApplicationContext());
        deleteBtn.setText(R.string.dialog_note_delete);

        layout.addView(iconBtn);
        layout.addView(priorityBtn);
        layout.addView(deleteBtn);
        noteDialog.setView(layout);

        noteDialog.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());

        AlertDialog dialog = noteDialog.create();
        iconBtn.setOnClickListener(view1 -> {
            pickNoteImage(note.getId());
            dialog.dismiss();
        });
        priorityBtn.setOnClickListener(view1 -> {
            showSetPriorityDialog(note);
            dialog.dismiss();
        });
        deleteBtn.setOnClickListener(view1 -> {
            db.deleteNote(note);
            adapter.setNotes(db.getNotes());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSetPriorityDialog(Note note) {
        AlertDialog.Builder noteDialog = new AlertDialog.Builder(MainActivity.this);
        noteDialog.setTitle(R.string.dialog_priority_set_priority);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        Button lowBtn = new Button(getApplicationContext());
        lowBtn.setText(R.string.dialog_filter_checkbox_low);

        Button medBtn = new Button(getApplicationContext());
        medBtn.setText(R.string.dialog_filter_checkbox_med);

        Button highBtn = new Button(getApplicationContext());
        highBtn.setText(R.string.dialog_filter_checkbox_high);

        layout.addView(lowBtn);
        layout.addView(medBtn);
        layout.addView(highBtn);
        noteDialog.setView(layout);

        noteDialog.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());

        AlertDialog dialog = noteDialog.create();

        View.OnClickListener listener = view -> {
            if(view == lowBtn)
                note.setPriority(1);
            else if(view == medBtn)
                note.setPriority(2);
            else if(view == highBtn)
                note.setPriority(4);

            db.updateNote(note);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        };

        lowBtn.setOnClickListener(listener);
        medBtn.setOnClickListener(listener);
        highBtn.setOnClickListener(listener);

        dialog.show();
    }

    private void showSearchDialog() {
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(MainActivity.this);
        searchDialog.setTitle(R.string.dialog_search_title);

        EditText searchET = new EditText(getApplicationContext());
        searchET.setHint(R.string.dialog_search_text_hint);
        searchET.setText(searchQuery);
        searchDialog.setView(searchET);

        searchDialog.setPositiveButton(R.string.dialog_search_btn_search, (dialog, whichButton) -> {
            searchQuery = searchET.getText().toString();
            adapter.filter(searchQuery, filterPriority);
        });

        searchDialog.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());

        searchDialog.setNeutralButton(R.string.clear, (dialog, i) -> {
            searchQuery = "";
            adapter.filter(searchQuery, filterPriority);
        });

        searchDialog.show();
    }

    private void showFilterDialog() {
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(MainActivity.this);
        searchDialog.setTitle(R.string.dialog_filter_title);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(8, 8, 8, 8);
        CheckBox low = new CheckBox(getApplicationContext());
        CheckBox med = new CheckBox(getApplicationContext());
        CheckBox hig = new CheckBox(getApplicationContext());

        low.setText(R.string.dialog_filter_checkbox_low);
        med.setText(R.string.dialog_filter_checkbox_med);
        hig.setText(R.string.dialog_filter_checkbox_high);
        low.setChecked(filterPriority == 0 || (filterPriority & 1) == 1);
        med.setChecked(filterPriority == 0 || (filterPriority & 2) == 2);
        hig.setChecked(filterPriority == 0 || (filterPriority & 4) == 4);

        layout.addView(low);
        layout.addView(med);
        layout.addView(hig);
        searchDialog.setView(layout);

        searchDialog.setPositiveButton(R.string.dialog_filter_btn_apply, (dialog, whichButton) -> {
            int lowP, medP, higP;
            lowP = low.isChecked() ? 1 : 0;
            medP = med.isChecked() ? 2 : 0;
            higP = hig.isChecked() ? 4 : 0;
            filterPriority = lowP | medP | higP;
            adapter.filter(searchQuery, filterPriority);
        });

        searchDialog.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());

        searchDialog.setNeutralButton(R.string.clear, (dialog, i) -> {
            filterPriority = 7;
            adapter.filter(searchQuery, filterPriority);
        });

        searchDialog.show();
    }

    private void showCreateDialog() {
        AlertDialog.Builder newNote = new AlertDialog.Builder(MainActivity.this);
        newNote.setTitle(R.string.dialog_create_title);

        EditText noteTitle = new EditText(getApplicationContext());
        noteTitle.setHint(R.string.dialog_create_text_hint);
        newNote.setView(noteTitle);

        newNote.setPositiveButton(R.string.dialog_create_btn_create, (dialog, whichButton) -> {
            db.addNote(new Note(noteTitle.getText().toString(), null, new SimpleDateFormat("HH:mm").format(new Date()), 0, null));
            adapter.setNotes(db.getNotes());
            adapter.notifyDataSetChanged();
        });

        newNote.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());

        newNote.show();
    }
}