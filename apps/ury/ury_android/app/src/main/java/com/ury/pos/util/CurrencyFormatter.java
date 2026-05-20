package com.ury.pos.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {

    private static final DecimalFormat df;

    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        sym.setGroupingSeparator(',');
        df = new DecimalFormat("#,##0", sym);
    }

    public static String format(double amount) {
        return "៛" + df.format(Math.round(amount));
    }

    public static String formatNoSymbol(double amount) {
        return df.format(Math.round(amount));
    }
}
