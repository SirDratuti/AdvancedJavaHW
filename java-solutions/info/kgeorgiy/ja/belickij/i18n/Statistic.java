package info.kgeorgiy.ja.belickij.i18n;

import java.text.BreakIterator;

public interface Statistic<T> {

    void resolve(final T value);

    int getCount();

    int compare(final T value1, final T value2);

    int getDifferent();

    T getMax();

    T getMin();

    BreakIterator getBreakIterator();

    boolean check(final T s);

    int getMaximumLength();

    T getMaximumLengthElement();

    int getMinimumLength();

    T getMinimumLengthElement();

    String getAverage();


}
