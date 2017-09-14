package com.sms.skilltracker;


import android.os.CountDownTimer;
import android.util.Log;

public class Skill {
    private String skillName;
    private long timeSpent; // time spent in seconds
    private long prevTime;  // previous time - stores System.currentTimeInMillis() on each tick
    private boolean isRunning;
    private String dateCreated;

    public Skill(String skillName, String dateCreated) {
        this.skillName = skillName;
        this.timeSpent = 0;
        this.prevTime = 0;
        this.isRunning = false;
        this.dateCreated = dateCreated;
    }

    // unpacks the packed string and constructs the Skill object
    public Skill(String val) {
        String[] values = val.split("\\|");
        this.skillName = values[0];
        this.timeSpent = Long.parseLong(values[1]);
        this.prevTime = Long.parseLong(values[2]);
        this.isRunning = Boolean.parseBoolean(values[3]);
        this.dateCreated = values[4];
    }

    CountDownTimer timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            // increment timer
            setTimeSpent(getTimeSpent() + 1);
            Log.d("Time spent on " + getSkillName(), UtilClass.timeFormat(getTimeSpent()));

            // this saves information of last tick - to load on next start
            setPrevTime(System.currentTimeMillis());
//            MainActivity.sharedPreferences.edit().putLong("prevTime", System.currentTimeMillis());
//            MainActivity.sharedPreferences.edit().apply();

            MainActivity.listViewAdapter.notifyDataSetChanged();

        }

        @Override
        public void onFinish() {
            Log.e("Timer up:", "This should not be reached");
        }
    };

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public long getPrevTime() {
        return prevTime;
    }

    public void setPrevTime(long prevTime) {
        this.prevTime = prevTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public String getDateCreated() {
        return this.dateCreated;
    }

    public void startTimer() {
        timer.start();
        this.setRunning(true);
    }

    public void stopTimer() {
        timer.cancel();
        this.setRunning(false);
    }

    @Override
    public String toString() {

        return
                "skillName : '" + skillName + '\'' +
                        ", timeSpent(in sec) : " + timeSpent +
                        ", prevTime(systime) : " + prevTime +
                        ", isRunning : " + isRunning
                ;
    }

}
