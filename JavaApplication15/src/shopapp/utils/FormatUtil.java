package shopapp.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    private static final Locale VIETNAM = new Locale("vi", "VN");
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(VIETNAM);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Định dạng tiền VND
    public static String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }

    // Định dạng ngày dd/MM/yyyy
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    // Định dạng số không có đơn vị tiền
    public static String formatNumber(int number) {
        return NumberFormat.getNumberInstance(VIETNAM).format(number);
    }
}
