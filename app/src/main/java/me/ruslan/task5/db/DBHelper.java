package me.ruslan.task5.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import me.ruslan.task5.models.Note;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "notes";
    public static final String TABLE_NOTES = "notes";

    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TEXT = "text";
    public static final String KEY_TIME = "time";
    public static final String KEY_PRIO = "priority";
    public static final String KEY_IMAGE = "image";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s text, `%s` text, %s text, %s text, %s text)",
                TABLE_NOTES, KEY_ID, KEY_TITLE, KEY_TEXT, KEY_TIME, KEY_PRIO, KEY_IMAGE
        ));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public ArrayList<Note> getNotes() {
        ArrayList<Note> result = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(TABLE_NOTES, new String[]{KEY_ID, KEY_TITLE, KEY_TEXT, KEY_TIME, KEY_PRIO, KEY_IMAGE}, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    result.add(new Note(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4),
                            cursor.getString(5)
                    ));
                    cursor.moveToNext();
                }
            }
        }

        return result;
    }

    public void addNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, note.getTitle());
        if (note.getText() == null)
            values.putNull(KEY_TEXT);
        else
            values.put(KEY_TEXT, note.getText());
        values.put(KEY_TIME, note.getTime());
        values.put(KEY_PRIO, note.getPriority());
        if (note.getImage() == null)
            values.putNull(KEY_IMAGE);
        else
            values.put(KEY_IMAGE, note.getImage());

        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, note.getTitle());
        if (note.getText() == null)
            values.putNull(KEY_TEXT);
        else
            values.put(KEY_TEXT, note.getText());
        values.put(KEY_TIME, note.getTime());
        values.put(KEY_PRIO, note.getPriority());
        if (note.getImage() == null)
            values.putNull(KEY_IMAGE);
        else
            values.put(KEY_IMAGE, note.getImage());

        db.update(TABLE_NOTES, values, "id = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void deleteNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, "id = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public Note getNote(int noteId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_NOTES, new String[]{KEY_ID, KEY_TITLE, KEY_TEXT, KEY_TIME, KEY_PRIO, KEY_IMAGE}, "id = ?", new String[]{String.valueOf(noteId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Note(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getString(5)
                );
            }
        }

        return null;
    }
}
