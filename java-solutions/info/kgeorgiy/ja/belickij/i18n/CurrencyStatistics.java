package info.kgeorgiy.ja.belickij.i18n;

import java.text.*;
import java.util.Locale;

public final class CurrencyStatistics extends AbstractStatistic {

    private final NumberFormat numberFormat;

    public CurrencyStatistics(final Locale locale, final Locale outLocale) {
        super(locale, outLocale);
        this.breakIterator = BreakIterator.getLineInstance(locale);
        this.numberFormat = NumberFormat.getCurrencyInstance(locale);
    }

    @Override
    public void average(final String value) {
        try {
            avg = (avg * count + numberFormat
                    .parse(value).doubleValue()) / (count + 1.0);
        } catch (final ParseException ignored) {
        }
    }

    @Override
    public int compare(final String value, final String other) {
        try {
            return Double.compare(
                    numberFormat.parse(value).doubleValue(),
                    numberFormat.parse(other).doubleValue()
            );
        } catch (final ParseException ignored) {
            return 0;
        }
    }

    @Override
    public boolean check(final String s) {
        return Checker.isCurrency(s, locale);
    }

    @Override
    public String getAverage() {

        return numberFormat.format(avg);
    }

}
