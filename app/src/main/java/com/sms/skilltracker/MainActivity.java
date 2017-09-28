package com.sms.skilltracker;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static SharedPreferences sharedPreferences;
    ArrayList<Skill> skillList = new ArrayList<>();
    static ListViewAdapter listViewAdapter; // this is static so that we can update listView on
    // every tick (from the skill object)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ListView listView = (ListView) findViewById(R.id.skill_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setAlpha(0.35f);

        skillList = new ArrayList<>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                itemClickHandler(position);
                listViewAdapter.notifyDataSetChanged();
            }
        });

        listViewAdapter = new ListViewAdapter(this, skillList);
        listView.setAdapter(listViewAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInputDialog().show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // load list saved in sharedPrefs; start timer if it was running onPause
    @Override
    protected void onResume() {
        super.onResume();

        getData();
        listViewAdapter.notifyDataSetChanged();

        Skill item = updateTimer();
        if (item != null)
            item.startTimer();
    }

    // timer stopped and values saved
    @Override
    protected void onPause() {
        super.onPause();
        // stop timer if any item was running
        int lastItem = sharedPreferences.getInt("lastRunningItem", -1);
        if (lastItem != -1) {
            skillList.get(lastItem).stopTimer();
        }
        // save list
        saveData();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // called onResume() - get saved data and populate the ArrayList
    void getData() {
        int n = sharedPreferences.getInt("size", 0);
        skillList.clear();
        for (int i = 0; i < n; i++) {
            String packedSkill = sharedPreferences.getString("" + i, null);
            skillList.add(new Skill(packedSkill));  // unpacking is done in the constructor
        }
    }

    // called onResume - update the timer if it was running onPause; returns the last running item if there is one
    Skill updateTimer() {
        int lastItemPosition = sharedPreferences.getInt("lastRunningItem", -1);  // save last used skill - position
        if (lastItemPosition != -1) {   //TODO - remove check on isrunning here if psbl
            Skill item = skillList.get(lastItemPosition);
            long prevTime = sharedPreferences.getLong("prevTime", Long.MIN_VALUE);
            if(prevTime == Long.MIN_VALUE){
                Log.e("SharedPreferences", "prevTime not found in shared preferences");
                return null;
            }

            Snackbar.make(findViewById(android.R.id.content), "You spent " + String.valueOf((System.currentTimeMillis()
                - prevTime) / 60000) + " minutes on " + item.getSkillName(), Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(findViewById(android.R.id.content), "User delete",
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setActionTextColor(Color.GRAY)
                .show();
            item.setTimeSpent(item.getTimeSpent() + (System.currentTimeMillis() - prevTime) / 1000); // + item.getSessionTime()
            return item;
        }
        return null;
    }

    // save size and each item of ArrayList; also save time
    void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("size", skillList.size());

        for (int i = 0; i < skillList.size(); i++) {
            String val = UtilClass.stringifyUtil(skillList.get(i));
            editor.putString("" + i, val);
        }
        // last running item is saved from itemClickHandler
//        editor.putLong("prevTime", System.currentTimeMillis());
        editor.apply();
    }

    // handle starting of timer for clicked item
    void itemClickHandler(int position) {
        Skill item = skillList.get(position);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (item.isRunning()) {
            // stop timer
            item.stopTimer();
            editor.remove("lastRunningItem");
        } else {
            int lastRunningItem = sharedPreferences.getInt("lastRunningItem", -1);
            if (lastRunningItem != -1) {
                // stop previous timer
                skillList.get(lastRunningItem).stopTimer();
            }
            item.startTimer();
            editor.putInt("lastRunningItem", position); // saves position of last running item in case of force close
        }
        editor.apply();
    }

    // returns a dialog for taking user input - adds new item to list and saves to sharedPrefs
    Dialog getInputDialog() {
        final Dialog dialog = new Dialog(this);

        dialog.setTitle("Enter Skill");
        dialog.setContentView(R.layout.input_dialog);

        // input text
        final EditText inp = (EditText) dialog.findViewById(R.id.inp);
        inp.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // starting time
        final EditText defaultHours = (EditText) dialog.findViewById(R.id.defaultHours);

        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.cbox);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_inp_cancel);
        final Button btnOk = (Button) dialog.findViewById(R.id.btn_inp_ok);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnOk.setEnabled(true);
                } else {
                    btnOk.setEnabled(false);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        final Context context = this;
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add new Skill item
                String inString = inp.getText().toString().trim();

                // name shouldn't contain "|" and shouldn't be empty string
                if (inString.equals("") || inString.contains("|")) {
                    Toast.makeText(context, "Invalid Name", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                String defaultHoursString = defaultHours.getText().toString();
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                skillList.add(new Skill(inString, currentDateTimeString));
                if (!defaultHoursString.equals(""))
                    skillList.get(skillList.size() - 1)
                            .setTimeSpent(Long.parseLong(defaultHoursString) * 3600);

                listViewAdapter.notifyDataSetChanged();

                saveData(); // TODO - save only new size and new item
//                Log.e(ADD_TAG,skillList.toString());
                dialog.dismiss();

            }
        });
//        dialog.show();
        return dialog;
    }

}
