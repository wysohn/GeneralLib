package org.generallib.time.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static Pattern pattern = Pattern.compile("(\\d+)(h|m|s)");

    /**
     * convert time format into milliseconds
     * 
     * @param str
     *            the format string
     * @return time in milliseconds
     */
    public static long parseTime(String str) {
        long sum = 0;

        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            switch (matcher.group(2)) {
            case "h":
                sum += Long.parseLong(matcher.group(1)) * 60 * 60 * 1000;
                break;
            case "m":
                sum += Long.parseLong(matcher.group(1)) * 60 * 1000;
                break;
            case "s":
                sum += Long.parseLong(matcher.group(1)) * 1000;
                break;
            }
        }

        return sum;
    }

    /**
     * Convert interval into formatted
     * 
     * @param interval
     * @return
     */
    public static String milliSecondsToString(long interval) {
        long r = 0;

        long day = interval / (24 * 60 * 60 * 1000);
        r = interval % (24 * 60 * 60 * 1000);

        long hour = interval / (60 * 60 * 1000);
        r = r % (60 * 60 * 1000);

        long minute = r / (60 * 1000);
        r = r % (60 * 1000);

        long second = r / (1000);

        StringBuilder builder = new StringBuilder();
        if(day > 0){
            builder.append(day + "d ");
        }
        if(hour > 0){
            builder.append(hour + "h ");
        }
        if(minute > 0){
            builder.append(minute + "m ");
        }
        if(second > 0){
            builder.append(second + "s");
        }

        return hour + "h " + minute + "m " + second + "s";
    }

    public static void main(String[] ar) {
        System.out.println(parseTime("12h33m50s"));
    }
}
