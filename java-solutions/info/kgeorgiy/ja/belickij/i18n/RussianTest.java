package info.kgeorgiy.ja.belickij.i18n;


import org.junit.Test;

import java.util.Locale;

import static info.kgeorgiy.ja.belickij.i18n.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RussianTest {

    private static final String fileName = "russian_goodbye.txt";

    @Test
    public void test01_testSentences() {
        final Statistic<String> sentenceStatistic = prepare(
                new Locale("ru", "RU"),
                new Locale("ru"),
                SENTENCE_STATISTICS,
                fileName);
        assertEquals(4, sentenceStatistic.getCount());
        assertEquals(156, sentenceStatistic.getMaximumLength());
        assertEquals(32, sentenceStatistic.getMinimumLength());

        assertEquals("До свиданья,4 друг мой, без руки, без слова," +
                " Не 20.05.2021 грусти и не печаль бровей, " +
                "- В этой жизни 16 умирать 450 ₽ не ново," +
                " Но и жить, конечно, не новей.", sentenceStatistic.getMaximumLengthElement());
        assertEquals("Милый мой, ты 13 у меня в груди.",
                sentenceStatistic.getMinimumLengthElement());
    }

    @Test
    public void test02_testWords() {
        final Statistic<String> wordStatistic = prepare(
                new Locale("ru", "RU"),
                new Locale("ru"),
                WORD_STATISTICS,
                fileName);
        assertEquals(44, wordStatistic.getCount());
        assertEquals(15, wordStatistic.getMaximumLength());
        assertEquals(1, wordStatistic.getMinimumLength());
        assertEquals("Предназначенное", wordStatistic.getMaximumLengthElement());
        assertEquals("и",
                wordStatistic.getMinimumLengthElement());
    }

    @Test
    public void test03_testNumbers() {
        final Statistic<String> numberStatistic = prepare(
                new Locale("ru", "RU"),
                new Locale("ru"),
                NUMBER_STATISTICS,
                fileName);
        assertEquals(7, numberStatistic.getCount());
        assertEquals(3, numberStatistic.getMaximumLength());
        assertEquals(1, numberStatistic.getMinimumLength());
        assertEquals("450", numberStatistic.getMaximumLengthElement());
        assertEquals("4",
                numberStatistic.getMinimumLengthElement());
        assertEquals("450", numberStatistic.getMax());
        assertEquals("1", numberStatistic.getMin());
        assertEquals("91,714", numberStatistic.getAverage());
    }

    @Test
    public void test04_testDates() {
        final Statistic<String> dateStatistics = prepare(
                new Locale("ru", "RU"),
                new Locale("ru"),
                DATE_STATISTICS,
                fileName);
        assertEquals(3, dateStatistics.getCount());
        assertEquals(10, dateStatistics.getMaximumLength());
        assertEquals(10, dateStatistics.getMinimumLength());
        assertEquals("20.05.2021", dateStatistics.getMaximumLengthElement());
        assertEquals("20.05.2021",
                dateStatistics.getMinimumLengthElement());
        assertEquals("24.05.2021", dateStatistics.getMax());
        assertEquals("20.05.2021", dateStatistics.getMin());
        assertEquals("22.05.2021", dateStatistics.getAverage());
    }

    @Test
    public void test05_testCurrency() {
        final Statistic<String> dateStatistics = prepare(
                new Locale("ru", "RU"),
                new Locale("ru"),
                CURRENCY_STATISTICS,
                fileName);
        assertEquals(2, dateStatistics.getCount());
        assertEquals(5, dateStatistics.getMaximumLength());
        assertEquals(5, dateStatistics.getMinimumLength());
        assertEquals("450 ₽", dateStatistics.getMaximumLengthElement());
        assertEquals("450 ₽",
                dateStatistics.getMinimumLengthElement());
        assertEquals("450 ₽", dateStatistics.getMax());
        assertEquals("150 ₽", dateStatistics.getMin());
        assertEquals("300,00 ₽", dateStatistics.getAverage());
    }
}
