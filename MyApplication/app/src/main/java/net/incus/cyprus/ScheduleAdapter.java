package net.incus.cyprus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by spider on 10/8/2016.
 */

public class ScheduleAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<ScheduleObject> objects;
    private Context context;

    private class ViewHolder {
        AutoResizeText textView1;
        AutoResizeText textView2;
    }

    public ScheduleAdapter(Context context, ArrayList<ScheduleObject> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
        this.context = context;
    }

    public int getCount() {
        return objects.size();
    }

    public ScheduleObject getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.schedule_layout_main, null);
            holder.textView1 = (AutoResizeText) convertView.findViewById(R.id.textViewDesc);
            holder.textView2 = (AutoResizeText) convertView.findViewById(R.id.textViewName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String schedule = objects.get(position).getSchedule((String)DataStore.get("SelectedDay"));
        if(schedule == null || schedule == "") {
            schedule = "";
            convertView.setVisibility(View.INVISIBLE);
        }
        holder.textView1.setText(" " + objects.get(position).getName());
        holder.textView2.setText(schedule + " ");
        return convertView;
    }
}