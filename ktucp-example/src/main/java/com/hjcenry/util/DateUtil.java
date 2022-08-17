/*
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG
*/
package com.hjcenry.util;


import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by 喜 on 2015/2/9.
 */
public class DateUtil {

//    /** redis服务器的时间 */
//    public static long redisTime = System.currentTimeMillis();
    /** 当前服务器时间和redis服务器时间的时间差 */
    private static volatile long timeDifference;
    /** 连续超过时间差的次数 */
    private static int timeDiffNum;

    public static final TimeZone TIMEZONE = TimeZone.getDefault();


    public static  final Long DAYMINI = 24*60*60*1000L;

    /**
     * 判断两个日期是否是同一天
     *
     * 此函数减去了统一刷新小时数
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDayRefHour(long time1, long time2) {
        time1 = time1-ReduceHour;
        time2 = time2-ReduceHour;
        return isSameDay(time1,time2);
    }

    /**
     * 判断两个日期是否是同一天
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDay(long time1, long time2) {
        DateTime date1 = new DateTime(time1);
        DateTime date2 = new DateTime(time2);

        if (date1.getYear() != date2.getYear()) {
            return false;
        }
        if (date1.getDayOfYear() != date2.getDayOfYear()) {
            return false;
        }
        return true;
    }



    /**
     * 计算两个日期的天数差
     *此函数减去了统一刷新小时数
     * @param time1
     * @param time2
     * @return
     */
    public static int dateDiffRefHour(long time1, long time2) {
        time1 = time1-ReduceHour;
        time2 = time2-ReduceHour;
        return dateDiff(time1,time2);
    }

    /**
     * 获取当前时间对象
     *此函数减去了统一刷新小时数
     * @return
     */
    public static DateTime getDateTimeRefHour() {
        DateTime refHour = new DateTime(DateUtil.getLongTimeReduceHour());
        return refHour;
    }

    /**
     * 计算两个日期的天数差
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int dateDiff(long time1, long time2) {

        DateTime date1=new DateTime(time1);
        DateTime date2=new DateTime(time2);

        return dateDiff(date1, date2);
    }
    /**
     * 计算两个日期的天数差
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int dateDiff(DateTime date1, DateTime date2) {
        int totalYearDays=0;
        if (date1.getYear()>date2.getYear()){
            for (int i = date2.getYear(); i < date1.getYear(); i++) {
                int yearDays=date2.year().isLeap()?366:365;
                totalYearDays+=yearDays;
            }
        }
        else if (date1.getYear()<date2.getYear()){
            for (int i = date1.getYear(); i < date2.getYear(); i++) {
                int yearDays=date1.year().isLeap()?366:365;
                totalYearDays-=yearDays;
            }
        }
        return (totalYearDays+(date1.getDayOfYear()-date2.getDayOfYear()));
    }
    /**
     * 计算两个日期的小时数差
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int hourDiff(long time1, long time2) {


        DateTime date1=new DateTime(time1);
        DateTime date2=new DateTime(time2);

        return hourDiff(date1, date2);
    }

    /**
     * 计算两个日期的小时数差
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int hourDiff( DateTime date1, DateTime date2) {
        int hourDiff=0;
        int dateDiff=dateDiff(date1,date2);
        if (dateDiff==0){
            hourDiff=date1.getHourOfDay()-date2.getHourOfDay();
        }
        else {
            dateDiff-=1;
            hourDiff=date1.getHourOfDay()+(24-date2.getHourOfDay());
        }
        return dateDiff*24+(hourDiff);
    }

    /**
     * 计算两个日期的月数差
     *此函数减去了统一刷新小时数
     * @param time1
     * @param time2
     * @return
     */
    public static int monthDiffRefHour(long time1, long time2) {
        time1 = time1-ReduceHour;
        time2 = time2-ReduceHour;
        return monthDiff(time1,time2);
    }


    /**
     * 计算两个日期的月数差
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int monthDiff(long time1, long time2) {
        DateTime date1 = new DateTime(time1);
        DateTime date2 = new DateTime(time2);
        int yearDiff = date1.yearOfEra().get() - date2.yearOfEra().get();

        int monthDiff = date1.monthOfYear().get() - date2.monthOfYear().get();

        return yearDiff * 12 + monthDiff;
    }

    /**
     * 计算两个日期的星期差
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int weekDiff(long time1, long time2) {

        DateTime date1=new DateTime(time1);
        DateTime data1_monday=date1.withDayOfWeek(DateTimeConstants.MONDAY);
        DateTime date2= new DateTime(time2);
        DateTime date2_monday=date2.withDayOfWeek(DateTimeConstants.MONDAY);
        int dateDiff=dateDiff(data1_monday.getMillis(),date2_monday.getMillis());
        int diff=dateDiff%7==0?dateDiff/7:dateDiff/7+1;
        return  diff;

    }

    /**
     * 获取双周
     * @param time1
     * @return
     */
    public static int getDoubleWeek(long time1){
        DateTime date1=new DateTime(time1);
        int d1_week=date1.getWeekOfWeekyear();
        int d1_doubleWeek=d1_week%2!=0? d1_week/2+1:d1_week/2;
        return d1_doubleWeek;
    }


    /**
     * 计算两个日期的分钟差
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int minuteDiff(long time1, long time2) {
        long millisDiff = Math.abs(time1 - time2);
        return (int) (millisDiff / (60 * 1000));
    }


    public static DateTimeFormatter format_datetime= DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter format_datetime_mill= DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss SSS");
    public static DateTimeFormatter format_date= DateTimeFormat.forPattern("yyyy-MM-dd");
    public static DateTimeFormatter format_time= DateTimeFormat.forPattern("HH:mm:ss");


    /**
     * 从毫秒数中获取到需要的日期字符串
     * @param curMs
     * @return
     */
    public static String formatDateTime(long curMs) {
        String format = "yyyyMMdd";
        return formatDateTime(curMs, format);
    }

    /**
     * 从毫秒数中获取到需要的日期字符串
     * @param curMs 时间戳
     * @param format 要求的格式
     * @return
     */
    public static String formatDateTime(long curMs, String format) {
        DateTime dateTime = new DateTime(curMs);
        return dateTime.toString(format);
    }

    /**
     * 将日期转换为datetime
     * @param timeStr
     * @return
     */
    public static DateTime parseDateTime(String timeStr){
        if (timeStr.length()==10){
            timeStr+=" 23:59:59";
        }
        return DateTime.parse(timeStr,timeStr.length()==10? DateUtil.format_date:DateUtil.format_datetime);
    }

    /**
     * 将日期转换为datetime
     * @param timeStr
     * @return
     */
    public static DateTime parseTime(String timeStr){
        if (timeStr.length()==10){
            timeStr+=" 23:59:59";
        }
        return DateTime.parse(timeStr,format_time);
    }

    /**
     * 获取当前int时间
     * @return
     */
    public static int getIntTime(){
        Long nowLongSec = DateUtil.getNow() / 1000;
        return nowLongSec.intValue();
    }

    /**
     * 获取当前long时间
     * @return
     */
    public static long getLongTime(){
        Long nowLongSec = DateUtil.getNow();
        return nowLongSec;
    }

    /**
     * 获取字符串yyyyMMddHHmm格式的long时间
     * @param str
     * @return
     */
    public static long getLongTime(String str){
        return getLongTime(str, "yyyyMMddHHmm");
    }

    /**
     * 获取字符串format格式的long时间
     * @param str
     * @param format
     * @return
     */
    public static long getLongTime(String str, String format){
        SimpleDateFormat _sdf = new SimpleDateFormat(format);
        Long millionSeconds = null;
        try {
            millionSeconds = _sdf.parse(str).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millionSeconds;
    }





    /**
     * 获取当前date的yyyy-MM-dd HH:mm:ss字符串格式
     * @param date
     * @return
     */
    public static String getTime(Date date){
        return getTime(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前date的format字符串格式
     * @param date
     * @param format
     * @return
     */
    public static String getTime(Date date, String format){
        SimpleDateFormat _sdf = new SimpleDateFormat(format);
        return _sdf.format(date);
    }

    /**
     * 获取long时间的yyyy-MM-dd HH:mm:ss字符串格式
     * @param time
     * @return
     */
    public static String getTime(long time){
        Date d = new Date(time);
        return getTime(d);
    }

    /**
     * 返回int时间date
     * @param time
     * @return
     */
    public static Date getDate(int time){
        return new Date(time * 1000l);
    }

    /**
     * 返回long时间date
     * @param time
     * @return
     */
    public static Date getDate(long time){
        return new Date(time);
    }

    /**
     * 获取字符串的date值
     * @param str
     * @return
     */
    public static Date getDate(String str){
        if(str.length() == 8){
            str = str + "0000";
        }
        return new Date(getLongTime(str));
    }

    /**
     * 获取字符串format格式下的date值
     * @param str
     * @param format
     * @return
     */
    public static Date getDate(String str, String format){
        return new Date(getLongTime(str, format));
    }

    /**
     * 获取date时间是周几
     * @param dt
     * @return
     */
    public static int getWeekOfDate(Date dt) {
        int[] weekDays = {6, 0, 1, 2, 3, 4, 5};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * 获取date时间的小时
     * @param date
     * @return
     */
    public static int getHour(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取date时间的当天n点date值
     * @param date
     * @return
     */
    public static Date stripToDay(Date date, int n) {
        Calendar c = Calendar.getInstance(TIMEZONE);
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, n);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 判断是否在指定时间范围内
     * begin = 开始时间
     * end = 结束时间
     * parameter = 比较时间
     * */
    public static  boolean checkRangeTime(long begin,long end,long parameter)
    {
         boolean back = false;

         if(parameter>=begin&&parameter<end){
             back = true;
         }
         return back;
    }

    /**
     * 判断是否在指定时间范围内
     * begin = 开始时间
     * end = 结束时间
     * parameter = 比较时间
     * */
    public static  boolean checkRangeTime(String begin,String end,long parameter)
    {
        try {

            DateTime beginTime=parseDateTime(begin);
            DateTime endTime=parseDateTime(end);

            return  checkRangeTime(beginTime.getMillis(),endTime.getMillis(),parameter);

        } catch (Exception e) {
            e.printStackTrace();
//           Log.error(e);
        }

        return false;
    }

    /**
     * 判断当前时间戳是否在指定时间范围内
     * begin = 开始时间
     * end = 结束时间
     * */
    public static boolean checkRangeTime(long begin,long end) {
        return checkRangeTime(begin,end,getLongTime());
    }

    /**
     * 今年的第几周
     * 周一为本周开始的第一天
     * */
    public static int getWeekOfYear(){
        Calendar c=Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 毫秒转换成秒
     * */
    public static int millisecondToSecond(long millisecond){

        return (int) (millisecond/1000);
    }

    private static final int crossDayHour = 4;
    /**
     * 游戏系统时间减去指定时间
     * 例如：凌晨4点跨天
     * 21.3.12已和迪康暂定为凌晨4点执行更新或清空
     * 目前更新后的数据更新还没有做/可能需要推送或客户端再次请求所有
     * */
    private static final long ReduceHour = TimeUnit.HOURS.toMillis(crossDayHour);

    public static int getCrossDayHour() {
        return crossDayHour;
    }
    /**
     * 获取当前long时间减去指定小时数
     * @return
     */
    public static long getLongTimeReduceHour(){
        Long nowLongSec = getLongTime();
        return nowLongSec - ReduceHour;
    }

    /**
     * 获取当前int时间 减去指定小时数
     * @return
     */
    public static int getIntTimeReduceHour(){
        Long nowLongSec = getLongTimeReduceHour() / 1000;
        return nowLongSec.intValue();
    }

    /**
     * 获取当前long时间减去指定小时数
     * @return
     */
    public static long getLongTimeReduceHour(long time){
        return time - ReduceHour;
    }

    /**
     * 获取明天零点的时间戳
     * */
    public static long getTomorrowTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);//这里改为1
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取下周一零点的时间戳
     * */
    public static long getNextWeekTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_YEAR,1);//增加一周
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取下个月零点的时间戳
     * */
    public static long getNextMonthTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }


    /**
     * LocalDateTime 获取当前时间
     * @throws Exception
     */
    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }


    /**
     * 获得今天0点的毫秒数
     * @return
     */
    public static long getTodayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //当天0点
        long zero = calendar.getTimeInMillis();
        return zero;
    }

    public static long getWeek(int time,int day,int hour,int min,int second) {
        Calendar weekdayStart = Calendar.getInstance();
        day=day+1;
        if(day==8) {
            day=1;
        }
        weekdayStart.add(Calendar.WEEK_OF_YEAR,time);//增加一周
        weekdayStart.set(Calendar.DAY_OF_WEEK,day);
        weekdayStart.set(Calendar.HOUR_OF_DAY, hour);
        weekdayStart.set(Calendar.MINUTE, min);
        weekdayStart.set(Calendar.SECOND, second);
        weekdayStart.set(Calendar.MILLISECOND, 0);
        System.out.println(weekdayStart.getTimeInMillis());
        return weekdayStart.getTimeInMillis();
    }

    /**
     * 获取参考redis服务器后的当前时间
     * 以redis服务器的时间作为所有服务器的统一的时间产生点
     * 如果直接使用redis服务器的时间，万一没取到，就会出问题，所以使用timeDifference
     * @return 服务器时间
     */
    public static long getNow() {
        String data=System.getProperty("openTime");
        if("true".equals(data)) {
            return System.currentTimeMillis();
        }
        return System.currentTimeMillis() + timeDifference;
    }

    /**
     * 获取本服的当前时间
     * @return
     */
    public static long getOsTime() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
       System.out.println( isSameDay(System.currentTimeMillis(),System.currentTimeMillis()+1000));
    }


}
