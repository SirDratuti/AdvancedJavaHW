package info.kgeorgiy.ja.belickij.i18n;

import java.nio.file.Path;
import java.util.Locale;

public class Utils {

    public final static int SENTENCE_STATISTICS = 0;
    public final static int WORD_STATISTICS = 1;
    public final static int NUMBER_STATISTICS = 2;
    public final static int DATE_STATISTICS = 3;
    public final static int CURRENCY_STATISTICS = 4;

    private static final Path directory = Path.of("testFiles");

    public static Statistic<String> prepare(final Locale localeIn, final Locale localeOut, final int statistic, final String textFile) {
        final Path file = directory.resolve(textFile);
        final TextStatistics textStatistics = new TextStatistics(
                localeIn,
                localeOut,
                file.toString(),
                "out");
        textStatistics.run(false);
        return textStatistics.getStatistics().get(statistic);
    }
}
