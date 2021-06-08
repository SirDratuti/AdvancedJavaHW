package info.kgeorgiy.ja.belickij.i18n;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public final class DateStatistics extends AbstractStatistic {

    private Date date = null;

    public DateStatistics(final Locale locale, final Locale outLocale) {
        super(locale, outLocale);
        this.breakIterator = BreakIterator.getLineInstance(locale);
    }

    @Override
    public void average(final String value) {
        try {
            final Date current = DateFormat.getDateInstance(DateFormat.SHORT, locale).parse(value);
            if (date == null) {
                date = current;
            } else {
                long average = ((long) ((date.getTime() * getCount() + current.getTime()) / (getCount() + 1.0)));
                date = new Date(average);
            }
        } catch (final ParseException exception) {
            System.err.println(exception.getMessage());
        }
    }

    @Override
    public int compare(final String first, final String other) {
        try {
            final Date current = DateFormat.getDateInstance(DateFormat.SHORT, locale).parse(first);
            final Date second = DateFormat.getDateInstance(DateFormat.SHORT, locale).parse(other);
            if (current.before(second)) {
                return -1;
            } else if (current.after(second)) {
                return 1;
            } else {
                return 0;
            }
        } catch (final ParseException exception) {
            System.err.println("Error while comparing " + exception.getMessage());
            return 0;
        }
    }

    @Override
    public boolean check(final String s) {
        return Checker.isDate(s, locale);
    }

    @Override
    public String getAverage() {
        return DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date);
    }

}
