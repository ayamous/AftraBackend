package ma.itroad.aace.eth.core.common.utils;

public class PatternUtils {

    private static final String PHONE_NUMBER =
            "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
            + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
            + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";

    private static String EMAIL = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static String LEADING_ZEROS_AND_PLUS = "^(0{2,}|\\+)+(?!$)*";

    public static boolean isEmail(String value) {
        if (value == null) {
            return false;
        }
        return value.matches(EMAIL);
    }

    public static boolean isPhoneNumber(String value) {
        if (value == null) {
            return false;
        }
        return value.matches(PHONE_NUMBER);
    }

    public static String recoverPhoneNumber(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(LEADING_ZEROS_AND_PLUS, "");
    }
}
