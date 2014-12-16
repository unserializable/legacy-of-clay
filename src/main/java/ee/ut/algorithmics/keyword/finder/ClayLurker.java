package ee.ut.algorithmics.keyword.finder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Taimo Peelo
 */
public class ClayLurker {
    public static void main(String[] args) {
        if (args.length != 0) {
            System.err.println("Usage: ClayLurker articleInputFolder");
            System.exit(1);
        }

        String articleInputFolder = "/home/taimo/Downloads/savi-articles/0" ; //args[0];

        File articleFolder = new File(articleInputFolder);
        int i = 0;

        Set<String> stopWords = loadStopWords();

        List<Set<String>> articleWordList = new ArrayList<>();

        for (File articleFile: articleFolder.listFiles()) {
            if (i > 100) {
                break;
            }
            i++;
            System.err.println("processing file #" + i);
            Document parsed = null;
            try {
                parsed = Jsoup.parse(articleFile, "UTF-8");
            } catch (IOException ex) {
                System.err.println("Skipping document " + articleFile.getAbsolutePath() + " (IO/parsing failure)");
                continue;
            }

            String articleText = parsed.body().text();
            String[] articleSplit = articleText.split("[\\s\\,\\.\\!\\”\\“\\\"\\'\\—\\:\\„\\…\\?]");

            Set<String> articleWords = new LinkedHashSet<>();

            for (int j = 0; j < articleSplit.length; j++) {
                String trimmed = articleSplit[j].trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (trimmed.matches("\\d+")) {
                    continue;
                }

                if (stopWords.contains(trimmed.toLowerCase())) {
                    continue;
                }

                articleSplit[j] = trimmed;

                if (Character.isUpperCase(articleSplit[j].charAt(0)) && (j == 0 || (!articleSplit[j-1].isEmpty() && !Character.isUpperCase(articleSplit[j-1].charAt(0))))) {
                    StringBuilder phraseBuild = new StringBuilder(articleSplit[j]);
                    for (int k = j+1; k < articleSplit.length; k++) {
                        String trimmedNext = articleSplit[k].trim();
                        if (trimmedNext.isEmpty())
                            break;
                        if (stopWords.contains(trimmedNext.toLowerCase()))
                            break;
                        if (Character.isUpperCase(trimmedNext.charAt(0))) {
                            phraseBuild.append(" ");
                            phraseBuild.append(trimmedNext);
                            continue;
                        } else
                            break;
                    }

                    if (phraseBuild.length() > trimmed.length()) {
                        articleWords.add(phraseBuild.toString().toLowerCase());
                    }
                }

                articleWords.add(articleSplit[j].toLowerCase());
            }

            articleWordList.add(articleWords);
        }

        Set<String> allWords = new LinkedHashSet<>();
        for (Set<String> articlewords: articleWordList) {
            allWords.addAll(articlewords);
        }

        List<WordIncidence> incidences = new ArrayList<>(allWords.size());

        for (String word: allWords) {
            incidences.add(new WordIncidence(word, incidenceCount(word, articleWordList)));
        }

        incidences.sort(Collections.reverseOrder(WordIncidenceComparator.INSTANCE));

        System.out.println(keyPhrases("savisaar", incidences));
    }


    private static List<WordIncidence> keyPhrases(String searchPhrase, List<WordIncidence> incidences) {
        List<WordIncidence> result = new ArrayList<>();
        double totalIncidences = 0l;
        for (WordIncidence incidence: incidences) {
            totalIncidences += incidence.getIncidence();
        }

        double avgIncidence = totalIncidences/incidences.size();
        double avgDiffSum = 0;
        for (WordIncidence incidence: incidences) {
            double diff = (incidence.getIncidence() - avgIncidence);
            avgDiffSum += (diff * diff);
        }
        double stddev = Math.sqrt(avgDiffSum/incidences.size());

        System.out.println("total=" + totalIncidences + " avg=" + avgIncidence + " stddev=" + stddev);

        for (WordIncidence incidence: incidences) {
            if (!incidence.getWord().contains(searchPhrase)) {
                if ((incidence.getIncidence() > (avgIncidence + 3*stddev)) ||
                    ((incidence.getWord().split(" ").length > 1) && (incidence.getIncidence() > avgIncidence + 1.5 *stddev)))
                result.add(incidence);
            }
        }

        return result;
    }

    private static int incidenceCount(String word, List<Set<String>> articleWords) {
        int i = 0;
        for (Set<String> words: articleWords) {
            if (words.contains(word))
                i++;
        }
        return i;
    }

    private static Set<String> loadStopWords() {
        Path path = null;
        List<String> lines = null;
        try {
            path = Paths.get(DelfiArticleMinimizer.class.getResource("/stopwords_et.txt").toURI());
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (lines.isEmpty())
            return Collections.emptySet();

        return new LinkedHashSet<>(lines);
    }

    private static class WordIncidenceComparator implements Comparator<WordIncidence> {
        public static final WordIncidenceComparator INSTANCE = new WordIncidenceComparator();

        @Override
        public int compare(WordIncidence o1, WordIncidence o2) {
            return o1.getIncidence() - o2.getIncidence();
        }
    }

}
