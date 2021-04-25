package by.kirill.uskov.medsched.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String[] mon = new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
    public static String[] daysOfWeek = new String[]{"ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ"};

    private static SimpleDateFormat time = new SimpleDateFormat("HH:mm");

    public static ArrayList<String> getTimeList() {
        try {
            ArrayList<String> timeList = new ArrayList<>();

            Date maxDate = new Date();
            maxDate.setTime(19 * 3600000);
            Calendar calendar = Calendar.getInstance();
            Date date = new Date();
            date.setTime(5 * 3600000);
            calendar.setTime(date);

            String startTime = time.format(calendar.getTime());
            calendar.add(Calendar.MINUTE, 30);
            String endTime = time.format(calendar.getTime());
            Date endDate = time.parse(endTime);
            while (endDate.getTime() <= maxDate.getTime()) {

                String t = startTime + " - " + endTime;
                timeList.add(t);

                startTime = endTime;

                calendar.add(Calendar.MINUTE, 30);
                endTime = time.format(calendar.getTime());

                endDate = time.parse(endTime);
            }
            return timeList;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
