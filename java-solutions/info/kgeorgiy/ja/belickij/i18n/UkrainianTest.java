package info.kgeorgiy.ja.belickij.i18n;

import org.junit.Test;

import java.util.Locale;

import static info.kgeorgiy.ja.belickij.i18n.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UkrainianTest {

    private static final String fileName = "ukrainian_goodbye.txt";

    @Test
    public void test01_testSentences() {
        final Statistic<String> sentenceStatistic = prepare(
                new Locale("uk", "UA"),
                new Locale("ru"),
                SENTENCE_STATISTICS,
                fileName);
        assertEquals(5, sentenceStatistic.getCount());
        assertEquals(67, sentenceStatistic.getMaximumLength());
        assertEquals(32, sentenceStatistic.getMinimumLength());
        assertEquals("До побачення, друже, без слова, " +
                "Не сумуй, 125,67 ₴ що злітаю у вир.", sentenceStatistic.getMaximumLengthElement());
        assertEquals("Милий 12 мій, ти у гру'дях моїх.",
                sentenceStatistic.getMinimumLengthElement());
    }

    @Test
    public void test02_testWords() {
        final Statistic<String> wordStatistic = prepare(
                new Locale("uk", "UA"),
                new Locale("ru"),
                WORD_STATISTICS,
                fileName);
        assertEquals(40, wordStatistic.getCount());
        assertEquals(11, wordStatistic.getMaximumLength());
        assertEquals(1, wordStatistic.getMinimumLength());
        assertEquals("розставання", wordStatistic.getMaximumLengthElement());
        assertEquals("і",
                wordStatistic.getMinimumLengthElement());
    }

    @Test
    public void test03_testNumbers() {
        final Statistic<String> numberStatistic = prepare(
                new Locale("uk", "UA"),
                new Locale("ru"),
                NUMBER_STATISTICS,
                fileName);
        assertEquals(5, numberStatistic.getCount());
        assertEquals(6, numberStatistic.getMaximumLength());
        assertEquals(1, numberStatistic.getMinimumLength());
        assertEquals("125,67", numberStatistic.getMaximumLengthElement());
        assertEquals("1",
                numberStatistic.getMinimumLengthElement());
        assertEquals("1337", numberStatistic.getMax());
        assertEquals("1", numberStatistic.getMin());
        assertEquals("303,134", numberStatistic.getAverage());
    }

    @Test
    public void test04_testDates() {
        final Statistic<String> dateStatistics = prepare(
                new Locale("uk", "UA"),
                new Locale("ru"),
                DATE_STATISTICS,
                fileName);
        assertEquals(2, dateStatistics.getCount());
        assertEquals(10, dateStatistics.getMaximumLength());
        assertEquals(10, dateStatistics.getMinimumLength());
        assertEquals("09.05.1945", dateStatistics.getMaximumLengthElement());
        assertEquals("09.05.1945",
                dateStatistics.getMinimumLengthElement());
        assertEquals("09.05.1945", dateStatistics.getMax());
        assertEquals("17.10.1588", dateStatistics.getMin());
        assertEquals("27.01.67", dateStatistics.getAverage());
    }

    @Test
    public void test05_testCurrency() {
        final Statistic<String> dateStatistics = prepare(
                new Locale("uk", "UA"),
                new Locale("ru"),
                CURRENCY_STATISTICS,
                fileName);
        assertEquals(3, dateStatistics.getCount());
        assertEquals(8, dateStatistics.getMaximumLength());
        assertEquals(3, dateStatistics.getMinimumLength());
        assertEquals("125,67 ₴", dateStatistics.getMaximumLengthElement());
        assertEquals("1 ₴",
                dateStatistics.getMinimumLengthElement());
        assertEquals("125,67 ₴", dateStatistics.getMax());
        assertEquals("1 ₴", dateStatistics.getMin());
        assertEquals("55,56 ₴", dateStatistics.getAverage());
    }

}
