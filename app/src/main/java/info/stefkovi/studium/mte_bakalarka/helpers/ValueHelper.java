package info.stefkovi.studium.mte_bakalarka.helpers;

public class ValueHelper {

    public static final int UNAVAILABLE = 2147483647;
    public static String intToStringWithNA(int num) {
        return num == UNAVAILABLE ? "N/A" : String.valueOf(num);
    }

    public static String longToStringWithNA(long num) {
        return num == UNAVAILABLE ? "N/A" : String.valueOf(num);
    }
}
