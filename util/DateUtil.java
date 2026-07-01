package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Checks if a date string matches yyyy-MM-dd format and is valid
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Computes the age in years given a DOB string in yyyy-MM-dd format
     */
    public static int calculateAge(String dobStr) {
        if (!isValidDate(dobStr)) return -1;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date birthDate = sdf.parse(dobStr);
            
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            
            Calendar today = Calendar.getInstance();
            
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            return -1;
        }
    }
}
