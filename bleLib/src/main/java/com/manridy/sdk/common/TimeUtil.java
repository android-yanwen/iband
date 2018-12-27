package com.manridy.sdk.common;


import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间工具类
 */
public class TimeUtil {

    /**
     * 毫秒转化时分秒毫秒
     */
    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if(day > 0) {
            sb.append(day+"天");
        }
        if(hour > 0) {
            sb.append(hour+"小时");
        }
        if(minute > 0) {
            sb.append(minute+"分");
        }
        if(second > 0) {
            sb.append(second+"秒");
        }
        if(milliSecond > 0) {
            sb.append(milliSecond+"毫秒");
        }
        return sb.toString();
    }

    /**
     * 得到现在时间
     * @param t
     * @return
     */
    public static String longToTime(long t){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date =dateFormat.format(new Date(t));
        return date;
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getNowYMDHMSTime() {


        SimpleDateFormat mDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String date = mDateFormat.format(new Date());
        return date;
    }

    /**
     * MM-dd HH:mm:ss
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getNowMDHMSTime() {

        SimpleDateFormat mDateFormat = new SimpleDateFormat(
                "MM-dd HH:mm:ss");
        String date = mDateFormat.format(new Date());
        return date;
    }

    /**
     * MM-dd
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getNowYMD() {

        SimpleDateFormat mDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        String date = mDateFormat.format(new Date());
        return date;
    }

    /**
     * yyyy-MM-dd
     *
     * @param date
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getYMD(Date date) {

        SimpleDateFormat mDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        String dateS = mDateFormat.format(date);
        return dateS;
    }

    public static int[] getNowYMDHMStoInt(){
        int[] time = new int[7];
        Calendar cal = Calendar.getInstance();//使用默认时区和语言环境获得一个日历。
        time[0] =cal.get(Calendar.YEAR);
        time[1] =cal.get(Calendar.MONTH)+1;
        time[2] =cal.get(Calendar.DAY_OF_MONTH);
        time[3] =cal.get(Calendar.HOUR_OF_DAY);
        time[4] =cal.get(Calendar.MINUTE);
        time[5] =cal.get(Calendar.SECOND);
        time[6] =cal.get(Calendar.DAY_OF_WEEK)-1;
        return time;
    }

    public static int[] getNowYMDHMStoInt2(){
        int[] time = new int[7];
        SimpleDateFormat mDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss");
        String[] timeStrs = mDateFormat.format(new Date()).split("-");
        Calendar cal = Calendar.getInstance();//使用默认时区和语言环境获得一个日历。
        time[6] =cal.get(Calendar.DAY_OF_WEEK)-1;
        for (int i = 0; i < timeStrs.length; i++) {
            time[i] = Integer.parseInt(timeStrs[i]);
        }

        return time;
    }

    public static int[] getYMDtoInt(String s){
        int[] time = new int[3];
        String[] strings = s.split("-");
        time[0] = Integer.parseInt(strings[0]);
        time[1] = Integer.parseInt(strings[1])-1;
        time[2] = Integer.parseInt(strings[2]);
        return time;
    }

    public static int[] getYMDHMtoInt(String s){
        int[] time = new int[3];
        String[] strings = s.split(" ");
        String[] strings1= strings[0].split("-");
        time[0] = Integer.parseInt(strings1[0]);
        time[1] = Integer.parseInt(strings1[1])-1;
        time[2] = Integer.parseInt(strings1[2]);
        return time;
    }

    /**
     * 比较选择日期是否大于当前日期
     * @param date 选择日期
     * @return true 大于，false 不大于
     */
    public static boolean compareNowYMD(Date date){
        Calendar calendar = Calendar.getInstance();
        Date curDate = calendar.getTime();
        boolean is;
        if ( date.getTime() > curDate.getTime()){
            is = true;
        }else{
            is = false;
        }
        return is;
    }

    /**
     * 比较选择周期是否大于当前日期
     * @param date 选择日期
     * @return true 大于，false 不大于
     */
    public static boolean compareNowWeek(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-7);
        Date curDate = calendar.getTime();
        boolean is;
        if ( date.getTime() >= curDate.getTime()){
            is = true;
        }else{
            is = false;
        }
        return is;
    }

    public static List<String> getCurrenWeek(Calendar cal){
        List<String> weeks = new ArrayList<>();
        Calendar calendar =cal;
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");
        for (int i = 1 ;i < 8 ; i++){
            calendar.add(Calendar.DAY_OF_MONTH, +i);
            if (format.format(calendar.getTime()).equals(format.format(calendar1.getTime()))){
                weeks.add("今天");
            }else{
                weeks.add(format.format(calendar.getTime()));
            }
            calendar.add(Calendar.DAY_OF_MONTH, -i);
        }
        return weeks;
    }

    public static String getDayFormat(Date day) {
        SimpleDateFormat Dayformat = new SimpleDateFormat("MM/dd");
        String d = Dayformat.format(day);
        return d;
    }


    public static String minToHM(int min){
        String s;
        int m;
        int h = min/60;
        if (h != 0){
            m = min%h;
        }else{
            m = min;
        }
        s=h+"h"+m+"min";
        return s;
    }
    public static String minToHM2(int min){
        String s;
        int m;
        int h = min/60;
        if (h != 0){
            m = min%h;
        }else{
            m = min;
        }
        s=zero(h)+":"+zero(m);
        return s;
    }

    public static String zero(int i){
        String s;
        if (i <10){
            s = "0" + i;
        }else {
            s = "" + i;
        }
        return s;
    }

    public static String week(int i){
        String day = "";
        switch (i){
            case 0:
                day = "日";
                break;
            case 1:
                day = "一";
                break;
            case 2:
                day = "二";
                break;
            case 3:
                day = "三";
                break;
            case 4:
                day = "四";
                break;
            case 5:
                day = "五";
                break;
            case 6:
                day = "六";
                break;
        }
        if (day.isEmpty()) {
            return null;
        }
        return "星期"+day;
    }

    public static String getCalculateDay(String day,int num){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(day);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH,num);
            return dateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SuppressLint("SimpleDateFormat")
    public static String getYMDHMSTime(Date d) {
        SimpleDateFormat mDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String date = mDateFormat.format(d);
        return date;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getNowYMDHM(){
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String date = mDateFormat.format(new Date());
        return date;
    }

    @SuppressLint("SimpleDateFormat")
    public static Date getDate(String ymdhms) {
//        ymdhms = "2018-12-12 19:22:21";
        ymdhms = formatDate1(ymdhms);
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = mDateFormat.parse(ymdhms);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     *  Created by yw on 18/12/18
     * 时间格式转换为 yyyy-MM-dd HH:mm:ss
     * @param dateStr
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatDate1(String dateStr) {
        String s_one = dateStr.substring(0, 1);
        if (s_one.matches("[a-zA-Z]")) {
            String s_MM = dateStr.substring(0, 3);
            if (s_MM.equals("Jan")) {
                s_MM = "01";
            }
            if (s_MM.equals("Feb")) {
                s_MM = "02";
            }
            if (s_MM.equals("Mar")) {
                s_MM = "03";
            }
            if (s_MM.equals("Apr")) {
                s_MM = "04";
            }
            if (s_MM.equals("May")) {
                s_MM = "05";
            }
            if (s_MM.equals("Jun")) {
                s_MM = "06";
            }
            if (s_MM.equals("Jul")) {
                s_MM = "07";
            }
            if (s_MM.equals("Aug")) {
                s_MM = "08";
            }
            if (s_MM.equals("Sep")) {
                s_MM = "09";
            }
            if (s_MM.equals("Oct")) {
                s_MM = "10";
            }
            if (s_MM.equals("Nov")) {
                s_MM = "11";
            }
            if (s_MM.equals("Dec")) {
                s_MM = "12";
            }
            String s_DD = dateStr.substring(4, 6);
            String s_YY = dateStr.substring(8, 12);
            String s_Time = dateStr.substring(13, 21);
            String s_yyyyMMddHHmmss = s_YY + "-" + s_MM + "-" + s_DD + " " + s_Time;
            return s_yyyyMMddHHmmss;
        }
        return dateStr;
    }

    /**
     * 将时间戳转换为时间
     * format : "yyyy-MM-dd HH:mm:ss"
     *          "HH:mm:ss"
     */
    public static String stampToDate(long s, String format){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
//        long lt = new Long(s);
        long lt = s;
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
}
