package info.kgeorgiy.ja.belickij.i18n;

import java.text.BreakIterator;
import java.util.Locale;

public final class SentenceStatistic extends AbstractStatistic {

    public SentenceStatistic(final Locale locale, final Locale outLocale) {
        super(locale, outLocale);
        this.breakIterator = BreakIterator.getSentenceInstance(locale);
    }

    @Override
    public void average(final String value) {
        avg = (avg * count + value.length()) / (count + 1.0);
    }

    @Override
    public boolean check(final String s) {
        return Checker.isSentence(s);
    }

}
