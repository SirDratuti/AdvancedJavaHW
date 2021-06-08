package info.kgeorgiy.ja.belickij.i18n;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class Checker {

    public static boolean isWord(final String s, final Locale locale) {
        if (isNumber(s, locale) || isDate(s, locale)) {
            return false;
        }
        return (s.codePoints().anyMatch(Character::isLetter));
    }

    public static boolean isSentence(final String s) {
        return !s.trim().equals("");
    }

    public static boolean isNumber(final String s, final Locale locale) {
        if (isDate(s, locale)) {
            return false;
        }
        try {
            NumberFormat
                    .getNumberInstance(locale)
                    .parse(s);
            return true;
        } catch (final ParseException parseException) {
            return false;
        }
    }

    public static boolean isDate(final String s, final Locale locale) {

        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        try {
            dateFormat.parse(s);
            return true;
        } catch (ParseException ignored) {
        }

        return false;
    }

    public static boolean isCurrency(final String s, final Locale locale) {
        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        try {
            currencyFormat.parse(s);
            return true;
        } catch (final ParseException ignored) {
            return false;
        }
    }
}
