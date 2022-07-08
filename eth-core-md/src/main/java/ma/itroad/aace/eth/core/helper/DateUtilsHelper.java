package ma.itroad.aace.eth.core.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class DateUtilsHelper {

    private DateUtilsHelper() {}

    public static Date getMidnight() {
        Date today = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static int getDayOfMonth() {
        Date today = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(today);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int getMonth() {
        Date today = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(today);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static boolean isDateNonExpired(Date date) {
        Date today = new Date();
        return date.after(today);
    }

    public static Date getDateBefore(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -days);
        return calendar.getTime();
    }

    public static Date getDateAfter(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

}
