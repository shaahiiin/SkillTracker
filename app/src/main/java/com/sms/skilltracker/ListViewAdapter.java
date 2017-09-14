package com.sms.skilltracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;


public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Skill> list;

    public ListViewAdapter(Context context, ArrayList<Skill> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;
        // if there is no view to convert into the new view
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = layoutInflater.inflate(R.layout.skill_list, null);
        } else {
            rowView = convertView;
        }

        // set values
        TextView skill = (TextView) rowView.findViewById(R.id.skill_name);
        TextView time = (TextView) rowView.findViewById(R.id.time_view);
        TextView dateCreated = (TextView) rowView.findViewById(R.id.date_created);
        ImageButton btnDelete = (ImageButton) rowView.findViewById(R.id.delete_item);

        Skill item = list.get(position);

        // set the values of the list item
        skill.setText(item.getSkillName());
        time.setText(UtilClass.timeFormat(item.getTimeSpent()));
        dateCreated.setText(item.getDateCreated());

        // onclick change background color
        if (item.isRunning()) {
            rowView.setBackgroundColor(Color.parseColor("#e7e7e7"));
        } else {
            rowView.setBackgroundColor(Color.TRANSPARENT);
        }


        btnDelete.setFocusable(true);   //TODO - try putting this in main activity
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlertDialogDelete(position).show();
            }
        });

        return rowView;
    }

    public void refreshAdapterView() {
        this.notifyDataSetChanged();
    }

    // Delete item Dialog builder
    public AlertDialog.Builder getAlertDialogDelete(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
                .setMessage("Are you sure you want to delete this skill?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // remove item at that position (stopped timer just in case...)
                        list.get(position).stopTimer();
                        list.remove(position);

                        refreshAdapterView();

                        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
                        editor.remove("size");
                        editor.putInt("size", list.size());
                        editor.remove("" + list.size());
                        if (MainActivity.sharedPreferences.getInt("lastRunningItem", -1) == position) {
                            editor.remove("lastRunningItem");
                        }
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder;
    }
}
