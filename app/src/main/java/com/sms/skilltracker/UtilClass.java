package com.sms.skilltracker;


public final class UtilClass {
//    convert time format : seconds(long) ---> hh:mm:ss (String)
//    if required we can split this string to get the individual parts
    public static String timeFormat(long seconds) {

        long hr = seconds / 3600;
        int rem = (int) (seconds % 3600);
        int min = rem / 60;
        int sec = rem % 60;

        String hourString = (hr < 10 ? "0" : "") + hr;
        String minString = (min < 10 ? "0" : "") + min;
        String secString = (sec < 10 ? "0" : "") + sec;

        return hourString + ":" + minString + ":" + secString;
    }

    //    pack the Skill object, to store it in SharedPreferences as a String
    public static String stringifyUtil(Skill skill) {
        return skill.getSkillName() + "|" + skill.getTimeSpent() + "|" + skill.getPrevTime() + "|" + skill.isRunning()
                + "|" + skill.getDateCreated();
    }
}