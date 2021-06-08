package info.kgeorgiy.ja.belickij.i18n;

import java.text.BreakIterator;
import java.util.Locale;

public final class WordStatistics extends AbstractStatistic {

    public WordStatistics(final Locale locale, final Locale outLocale) {
        super(locale, outLocale);
        this.breakIterator = BreakIterator.getWordInstance(locale);
    }

    @Override
    public void average(final String value) {
        avg = (avg * count + value.length()) / (count + 1.0);
    }

    @Override
    public boolean check(final String s) {
        return Checker.isWord(s, locale);
    }

}
