package me.ruslan.task5.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.HashMap;

import me.ruslan.task5.R;
import me.ruslan.task5.SettingsActivity;
import me.ruslan.task5.models.Note;

public class NotesListAdapter extends ArrayAdapter<Note> implements Filterable {
    private HashMap<Integer, Note> dataSet = new HashMap<>();
    private ArrayList<Note> dataSetFiltered;
    private static SearchParams search = new SearchParams("", 7);
    private SharedPreferences prefs;

    private static class ViewHolder {
        TextView txtName;
        TextView txtText;
        TextView txtTime;
        ImageView imgPriority;
        ImageView imgIcon;
    }

    private static class SearchParams {
        private String query;
        private int priority;

        public SearchParams(String query, int priority) {
            this.query = query;
            this.priority = priority;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String value) {
            query = value;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int value) {
            priority = value;
        }

        public boolean isDefault() {
            return query.isEmpty() && (priority == 0 || priority == 7);
        }
    }

    public NotesListAdapter(ArrayList<Note> data, Context context, SharedPreferences prefs) {
        super(context, R.layout.notes_list, data);
        dataSetFiltered = data;
        for(Note note : data) {
            dataSet.put(note.getId(), note);
        }
        this.prefs = prefs;
    }

    public void setNotes(ArrayList<Note> data) {
        dataSet.clear();
        for(Note note : data) {
            dataSet.put(note.getId(), note);
        }
        notifyDataSetChanged(true);
    }

    private Drawable getPriorityDrawable(int priority) {
        switch (priority) {
            case 1:
                return AppCompatResources.getDrawable(getContext(), R.drawable.arrow_down);
            case 4:
                return AppCompatResources.getDrawable(getContext(), R.drawable.arrow_up);
            default:
                return AppCompatResources.getDrawable(getContext(), R.drawable.line);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = getItem(position);
        ViewHolder viewHolder;

        String theme = prefs.getString("theme", "light");
        int fontSize = prefs.getInt("font_size", 8) + SettingsActivity.MIN_FONT_SIZE;
        getContext().setTheme(theme.equals("dark") ? R.style.Theme_Task5Dark : R.style.Theme_Task5Light);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.notes_list, parent, false);
            viewHolder.txtName = convertView.findViewById(R.id.title);
            viewHolder.txtText = convertView.findViewById(R.id.text);
            viewHolder.txtTime = convertView.findViewById(R.id.time);
            viewHolder.imgPriority = convertView.findViewById(R.id.priority);
            viewHolder.imgIcon = convertView.findViewById(R.id.icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtName.setTextSize(fontSize);
        viewHolder.txtText.setTextSize(fontSize-4);
        viewHolder.txtTime.setTextSize(fontSize-4);

        viewHolder.txtName.setText(note.getTitle());
        viewHolder.txtText.setText(note.getText() == null ? "(null)" : note.getText());
        viewHolder.txtTime.setText(note.getTime());
        viewHolder.imgPriority.setImageDrawable(getPriorityDrawable(note.getPriority()));
        if (note.getImage() == null || !note.getImageFile().exists()) {
            viewHolder.imgIcon.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.no_image));
        } else {
            viewHolder.imgIcon.setImageBitmap(BitmapFactory.decodeFile(note.getImage()));
        }
        return convertView;
    }

    public void filter(String query, int priority) {
        search.setQuery(query);
        search.setPriority(priority);
        filter();
    }

    public void filter() {
        String query = search.getQuery();
        int priority = search.getPriority();
        if(search.isDefault()) {
            dataSetFiltered.clear();
            dataSetFiltered.addAll(dataSet.values());
            notifyDataSetChanged(false);
            return;
        }

        ArrayList<Note> result = new ArrayList<>();

        for (Note note : dataSet.values()) {
            if (!query.isEmpty() && (!note.getTitle().contains(query) && !note.getText().contains(query)))
                continue;
            if (priority != 0 && priority != 7 && ((note.getPriority() & priority) != note.getPriority()))
                continue;
            result.add(note);
        }

        dataSetFiltered.clear();
        dataSetFiltered.addAll(result);
        notifyDataSetChanged(false);
    }

    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    public void notifyDataSetChanged(boolean filter) {
        if(filter)
            filter();
        super.notifyDataSetChanged();
    }

    public Note getFiltered(int idx) {
        return dataSetFiltered.get(idx);
    }

    public void setNote(Note note) {
        dataSet.put(note.getId(), note);
    }
}