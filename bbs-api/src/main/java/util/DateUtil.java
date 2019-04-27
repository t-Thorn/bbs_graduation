package util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String now() {
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }
}
