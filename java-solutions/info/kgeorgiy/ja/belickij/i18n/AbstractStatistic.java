package info.kgeorgiy.ja.belickij.i18n;

import java.text.BreakIterator;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class AbstractStatistic implements Statistic<String> {
    protected int count;

    protected final Locale locale;
    protected final Locale outLocale;
    protected String minimum = null;
    protected String maximum = null;

    protected final Collator collator;
    protected final Set<String> different = new HashSet<>();
    protected BreakIterator breakIterator;

    protected int maximumLength = -2;
    protected String maximumLengthElement = null;
    protected int minimumLength = Integer.MAX_VALUE;
    protected String minimumLengthElement = null;
    protected double avg;


    public AbstractStatistic(final Locale locale, final Locale outLocale) {
        this.locale = locale;
        this.outLocale = outLocale;
        count = 0;
        avg = 0.0;
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.IDENTICAL);
    }

    @Override
    public void resolve(final String value) {
        if (!check(value)) {
            return;
        }
        final String copyValue = normalize(value);
        average(copyValue);
        count++;
        different.add(copyValue);

        if (copyValue.length() >= maximumLength) {
            maximumLength = copyValue.length();
            maximumLengthElement = copyValue;
        }

        if (copyValue.length() <= minimumLength) {
            minimumLength = copyValue.length();
            minimumLengthElement = copyValue;
        }

        if (minimum == null || maximum == null) {
            minimum = copyValue;
            maximum = copyValue;

        } else {
            final int compMin = compare(minimum, copyValue);
            final int compMax = compare(maximum, copyValue);
            if (compMin > 0) {
                minimum = copyValue;
            }
            if (compMax < 0) {
                maximum = copyValue;
            }
        }
    }

    public abstract void average(final String value);

    public String normalize(String s) {
        if (s.endsWith(" ")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    @Override
    public int compare(final String first, final String second) {
        return collator.compare(first, second);
    }

    @Override
    public abstract boolean check(final String s);

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getDifferent() {
        return different.size();
    }

    @Override
    public String getMax() {
        return maximum;
    }

    @Override
    public String getMin() {
        return minimum;
    }

    @Override
    public BreakIterator getBreakIterator() {
        return breakIterator;
    }

    @Override
    public String getAverage() {
        return NumberFormat.getNumberInstance(outLocale).format(avg);
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public String getMaximumLengthElement() {
        return maximumLengthElement;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public String getMinimumLengthElement() {
        return minimumLengthElement;
    }
}
