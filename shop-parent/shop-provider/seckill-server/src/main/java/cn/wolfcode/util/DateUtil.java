package cn.wolfcode.util;

import java.util.Calendar;
import java.util.Date;


public class DateUtil {
    /**
     * 根据日期和场次看是否在秒杀有效时间之内
     * @param date
     * @param time
     * @return
     */
    public static boolean isLegalTime(Date date, int time){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY,time);
        Long start = c.getTime().getTime();
        Long now = new Date().getTime();
        c.add(Calendar.HOUR_OF_DAY,2);
        Long end = c.getTime().getTime();
        return now>=start && now<=end;
    }
}
