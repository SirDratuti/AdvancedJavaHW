package info.kgeorgiy.ja.belickij.i18n;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class NumberStatistic extends AbstractStatistic {

    private final NumberFormat numberFormat;

    public NumberStatistic(final Locale locale, final Locale outLocale) {
        super(locale, outLocale);
        this.breakIterator = BreakIterator.getWordInstance(locale);
        this.numberFormat = NumberFormat.getNumberInstance(locale);
    }

    @Override
    public void average(final String value) {
        try {
            avg = (avg * count + numberFormat
                    .parse(value).doubleValue()) / (count + 1.0);
        } catch (final ParseException e) {
            System.err.println("Error while evaluating avg for " + value + " " + e.getMessage());
        }
    }

    @Override
    public String getAverage() {
        return NumberFormat.getNumberInstance(outLocale).format(avg);
    }

    @Override
    public int compare(final String first, final String second) {
        try {
            return Double.compare(
                    numberFormat.parse(first).doubleValue(),
                    numberFormat.parse(second).doubleValue()
            );
        } catch (final ParseException e) {
            System.err.println("Can't parse this numbers " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean check(final String s) {
        return Checker.isNumber(s, locale);
    }

}
