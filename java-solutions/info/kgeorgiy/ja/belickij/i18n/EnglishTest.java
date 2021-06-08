package info.kgeorgiy.ja.belickij.i18n;

import org.junit.Test;

import java.util.Locale;

import static info.kgeorgiy.ja.belickij.i18n.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnglishTest {

    private static final String fileName = "american_goodbye.txt";

    @Test
    public void test01_testSentences() {
        final Statistic<String> sentenceStatistic = prepare(
                new Locale("en", "US"),
                new Locale("en"),
                SENTENCE_STATISTICS,
                fileName);
        assertEquals(8, sentenceStatistic.getCount());
        assertEquals(59, sentenceStatistic.getMaximumLength());
        assertEquals(27, sentenceStatistic.getMinimumLength());
        assertEquals("We are parting now, and we know - It's for 11/23/2009 good.",
                sentenceStatistic.getMaximumLengthElement());
        assertEquals("One day we'll find it best.",
                sentenceStatistic.getMinimumLengthElement());
    }

    @Test
    public void test02_testWords() {
        final Statistic<String> wordStatistic = prepare(
                new Locale("en", "US"),
                new Locale("en"),
                WORD_STATISTICS,
                fileName);
        assertEquals(60, wordStatistic.getCount());
        assertEquals(8, wordStatistic.getMaximumLength());
        assertEquals(2, wordStatistic.getMinimumLength());
        assertEquals("eyebrows", wordStatistic.getMaximumLengthElement());
        assertEquals("as",
                wordStatistic.getMinimumLengthElement());
    }

    @Test
    public void test03_testNumbers() {
        final Statistic<String> numberStatistic = prepare(
                new Locale("en", "US"),
                new Locale("en"),
                NUMBER_STATISTICS,
                fileName);
        assertEquals(7, numberStatistic.getCount());
        assertEquals(6, numberStatistic.getMaximumLength());
        assertEquals(2, numberStatistic.getMinimumLength());
        assertEquals("0.5556", numberStatistic.getMaximumLengthElement());
        assertEquals("12",
                numberStatistic.getMinimumLengthElement());
        assertEquals("12,333", numberStatistic.getMax());
        assertEquals("0.5556", numberStatistic.getMin());
        assertEquals("2,055.731", numberStatistic.getAverage());
    }

    @Test
    public void test04_testDates() {
        final Statistic<String> dateStatistics = prepare(
                new Locale("en", "US"),
                new Locale("en"),
                DATE_STATISTICS,
                fileName);
        assertEquals(1, dateStatistics.getCount());
        assertEquals(10, dateStatistics.getMaximumLength());
        assertEquals(10, dateStatistics.getMinimumLength());
        assertEquals("11/23/2009", dateStatistics.getMaximumLengthElement());
        assertEquals("11/23/2009",
                dateStatistics.getMinimumLengthElement());
        assertEquals("11/23/2009", dateStatistics.getMax());
        assertEquals("11/23/2009", dateStatistics.getMin());
        assertEquals("11/23/09", dateStatistics.getAverage());
    }

    @Test
    public void test05_testCurrency() {
        final Statistic<String> currencyStatistics = prepare(
                new Locale("en", "US"),
                new Locale("en"),
                CURRENCY_STATISTICS,
                fileName);
        assertEquals(2, currencyStatistics.getCount());
        assertEquals(7, currencyStatistics.getMaximumLength());
        assertEquals(3, currencyStatistics.getMinimumLength());
        assertEquals("$100.50", currencyStatistics.getMaximumLengthElement());
        assertEquals("$50",
                currencyStatistics.getMinimumLengthElement());
        assertEquals("$100.50", currencyStatistics.getMax());
        assertEquals("$50", currencyStatistics.getMin());
        assertEquals("$75.25", currencyStatistics.getAverage());
    }
}
