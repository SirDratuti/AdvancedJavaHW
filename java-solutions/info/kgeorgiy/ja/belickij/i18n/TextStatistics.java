package info.kgeorgiy.ja.belickij.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.util.*;
import java.util.stream.Collectors;

public final class TextStatistics {


    private final String output;
    private final Locale outLocale;
    private final String input;

    private final List<Statistic<String>> statistics;

    public TextStatistics(final Locale localeIn,
                          final Locale localeOut,
                          final String input,
                          final String out) {
        this.output = out;
        this.input = input;
        this.outLocale = localeOut;
        this.statistics = fillStatistics(localeIn, localeOut);
    }

    private static boolean checkInput(final String[] args) {
        if (args == null || args.length != 4) {
            return false;
        }
        return Arrays.stream(args).noneMatch(Objects::isNull);
    }

    public static void main(final String[] args) {
        if (checkInput(args)) {
            final Locale inputLocale = generateLocale(args[0]);
            final Locale outputLocale = new Locale(args[1]);
            final String inputFile = args[2];
            final String outputFile = args[3];

            final TextStatistics textStatistics = new TextStatistics(inputLocale, outputLocale, inputFile, outputFile);
            textStatistics.run(true);
        }
    }

    private static Locale generateLocale(final String arg) {
        final List<String> list = Arrays.stream(arg.split("-")).collect(Collectors.toList());
        Locale locale;
        switch (list.size()) {
            case 0:
                locale = Locale.getDefault();
                break;
            case 1:
                locale = new Locale(list.get(0));
                break;
            case 2:
                locale = new Locale(list.get(0), list.get(1));
                break;
            default:
                locale = new Locale(list.get(0), list.get(1), list.get(2));
                break;
        }
        return locale;
    }

    private List<Statistic<String>> fillStatistics(final Locale locale, final Locale outLocale) {
        final List<Statistic<String>> list = new ArrayList<>();
        list.add(new SentenceStatistic(locale, outLocale));
        list.add(new WordStatistics(locale, outLocale));
        list.add(new NumberStatistic(locale, outLocale));
        list.add(new DateStatistics(locale, outLocale));
        list.add(new CurrencyStatistics(locale, outLocale));
        return list;
    }

    public void run(final boolean write) {

        statistics.forEach(it ->
                iterate(it,
                        readFile(input))
        );

        if (write) {
            writeToFile(input, output, statistics);
        }
    }

    private void iterate(final Statistic<String> statistic,
                         final String text) {
        final List<String> parts = split(text, statistic.getBreakIterator());
        for (String str : parts) {
            statistic.resolve(str);
        }
    }

    private static List<String> split(final String text, final BreakIterator boundary) {
        boundary.setText(text);
        final List<String> parts = new ArrayList<>();
        for (
                int begin = boundary.first(), end = boundary.next();
                end != BreakIterator.DONE;
                begin = end, end = boundary.next()
        ) {
            parts.add(text.substring(begin, end).trim());
        }
        return parts;
    }

    private static String readFile(final String path) {
        final StringBuilder stringBuilder = new StringBuilder();
        try (final BufferedReader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            String str = reader.readLine();
            while (str != null) {
                stringBuilder.append(str).append(" ");
                str = reader.readLine();
            }
        } catch (final IOException exception) {
            System.err.println("Unable to read input file");
        }


        return stringBuilder.toString();
    }

    private void writeToFile(final String input,
                             final String output,
                             final List<Statistic<String>> statistic) {
        final ResourceBundle bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.belickij.i18n.UsageResourceBundle", outLocale);
        String outText = String.format(
                "%s : %s\n" +
                        //Sentences
                        "%s %s\n" +
                        "\t%s %s: %d (%d %s).\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s %s: %d (\"%s\").\n" +
                        "\t%s %s %s: %d (\"%s\").\n" +
                        //Words
                        "%s %s\n" +
                        "\t%s %s: %d (%d %s).\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s %s: %d (\"%s\").\n" +
                        "\t%s %s %s: %d (\"%s\").\n" +
                        "\t%s %s %s: %s.\n" +
                        //Numbers
                        "%s %s\n" +
                        "\t%s %s: %d (%d %s).\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: %s.\n" +
                        //Dates
                        "%s %s\n" +
                        "\t%s %s: %d (%d %s).\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: %s.\n" +
                        //Currencies
                        "%s %s\n" +
                        "\t%s %s: %d (%d %s).\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: \"%s\".\n" +
                        "\t%s %s: %s.\n"
                ,
                bundle.getString("nameFile"),
                input,
                ///Sentences
                bundle.getString("statistics"),
                bundle.getString("sentences"),
                ///
                bundle.getString("amount"),
                bundle.getString("sentAmount"),
                statistic.get(0).getCount(),
                statistic.get(0).getDifferent(),
                bundle.getString("diff"),
                ///
                bundle.getString("min"),
                bundle.getString("sentverb"),
                statistic.get(0).getMin(),
                ///
                bundle.getString("max"),
                bundle.getString("sentverb"),
                statistic.get(0).getMax(),
                ///
                bundle.getString("minaya"),
                bundle.getString("length"),
                bundle.getString("sentya"),
                statistic.get(0).getMinimumLength(),
                statistic.get(0).getMinimumLengthElement(),
                ///
                bundle.getString("maxaya"),
                bundle.getString("length"),
                bundle.getString("sentya"),
                statistic.get(0).getMaximumLength(),
                statistic.get(0).getMaximumLengthElement(),
                ///Words
                bundle.getString("statistics"),
                bundle.getString("words"),
                ///
                bundle.getString("amount"),
                bundle.getString("wordAmount"),
                statistic.get(1).getCount(),
                statistic.get(1).getDifferent(),
                bundle.getString("diff"),
                ///
                bundle.getString("min"),
                bundle.getString("wordverb"),
                statistic.get(1).getMin(),
                ///
                bundle.getString("max"),
                bundle.getString("wordverb"),
                statistic.get(1).getMax(),
                ///
                bundle.getString("minaya"),
                bundle.getString("length"),
                bundle.getString("wordya"),
                statistic.get(1).getMinimumLength(),
                statistic.get(1).getMinimumLengthElement(),
                ///
                bundle.getString("maxaya"),
                bundle.getString("length"),
                bundle.getString("wordya"),
                statistic.get(1).getMaximumLength(),
                statistic.get(1).getMaximumLengthElement(),
                ///
                bundle.getString("average"),
                bundle.getString("length"),
                bundle.getString("wordya"),
                statistic.get(1).getAverage(),
                ///Numbers
                bundle.getString("statistics"),
                bundle.getString("numbers"),
                ///
                bundle.getString("amount"),
                bundle.getString("numsAmount"),
                statistic.get(2).getCount(),
                statistic.get(2).getDifferent(),
                bundle.getString("diff"),
                ///
                bundle.getString("min"),
                bundle.getString("numbverb"),
                statistic.get(2).getMin(),
                ///
                bundle.getString("max"),
                bundle.getString("numbverb"),
                statistic.get(2).getMax(),
                ///
                bundle.getString("average"),
                bundle.getString("numbverb"),
                statistic.get(2).getAverage(),
                ///Dates
                bundle.getString("statistics"),
                bundle.getString("dates"),
                ///
                bundle.getString("amount"),
                bundle.getString("datesAmount"),
                statistic.get(3).getCount(),
                statistic.get(3).getDifferent(),
                bundle.getString("diff"),
                ///
                bundle.getString("min"),
                bundle.getString("dateverb"),
                statistic.get(3).getMin(),
                ///
                bundle.getString("max"),
                bundle.getString("dateverb"),
                statistic.get(3).getMax(),
                ///
                bundle.getString("averageaya"),
                bundle.getString("dateverb"),
                statistic.get(3).getAverage(),
                ///Currency
                bundle.getString("statistics"),
                bundle.getString("currency"),
                ///
                bundle.getString("amount"),
                bundle.getString("currencies"),
                statistic.get(4).getCount(),
                statistic.get(4).getDifferent(),
                bundle.getString("diff"),
                ///
                bundle.getString("min"),
                bundle.getString("currencyverb"),
                statistic.get(4).getMin(),
                ///
                bundle.getString("max"),
                bundle.getString("currencyverb"),
                statistic.get(4).getMax(),
                ///
                bundle.getString("averageaya"),
                bundle.getString("currencyverb"),
                statistic.get(4).getAverage()

        );
        try (final BufferedWriter writer = Files.newBufferedWriter(Path.of(output))) {
            writer.write(outText);
        } catch (final IOException exception) {
            System.err.println("Error while creating output file" + exception.getMessage());
        }
    }

    public List<Statistic<String>> getStatistics() {
        return statistics;
    }
}

