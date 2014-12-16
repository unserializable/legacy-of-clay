package ee.ut.algorithmics.keyword.finder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Converts the full HTML of an "article" to minimal one suitable for indexing.
 *
 * @author Taimo Peelo
 */
public class DelfiArticleMinimizer {
    /* get rid of unneeded content, ~10x size decrease */
    private static final String[] SELECTORS_TO_IGNORE = new String[] {
            "#article_keyword_ads",
            "#ss_buttons",
            "div.art_utils",
            "div#center",
            "div#right",
            "#diginav-lb-login",
            "div.obfuscated_body",
            ".ado_banner",
            ".ad_atop",
            ".dwidget",
            "#city24_mfooter",
            "#footer",
            "#city24_precom",
            "#rb_filmilaenutus",
            "#artBottomRibbon",
            ".artExtra",
            ".articleCustomEnd",
            ".exCon",
            "#header",
            "div.art_sep",
            "div.comment-thread-switcher-list",
            "div.comment-add-form",
            "#locationB",
            "#pre_footer",
            "font.articleSource",
            "div#conwrapper",
            ".commentCount"
    };

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: DelfiArticleIndexer articleInputFolder articleOutputFolder");
            System.exit(1);
        }

        String articleInputFolder = args[0];
        String articleOutputFolder = args[1];

        File articleFolder = new File(articleInputFolder);
        int i = 0;

        Map<String, Element> delfiTopicLinks = new LinkedHashMap<>();
        for (File articleFile: articleFolder.listFiles()) {
            i++;
            System.err.println("processing file #" + i);
            Document parsed = null;
            try {
                parsed = Jsoup.parse(articleFile, "UTF-8");
            } catch (IOException ex) {
                System.err.println("Skipping document " + articleFile.getAbsolutePath() + " (IO/parsing failure)");
                continue;
            }
            Document d = cleanup(parsed);
            writeMinimal(d, new File(articleOutputFolder + "/" + articleFile.getName()));

            Elements links = d.body().select("#article a");
            for (Element link: links) {
                if (link.attr("href").contains("delfi.ee/teemalehed/"))
                    delfiTopicLinks.put(link.attr("href"), link);
            }
        }
        System.out.println(delfiTopicLinks);
    }

    private static void writeMinimal(Document d, File f) {
        FileOutputStream fos = null;
        PrintStream out = null;
        try {
            f.createNewFile();
            fos = new FileOutputStream(f);
            out = new PrintStream(fos);
            out.println("<!DOCTYPE html>\n");
            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\">");
            out.println("<head>");
            out.println("<meta charset=\"utf-8\"/>");
            out.println("</head>");
            out.println("<body>");
            out.println(d.select("body #article .articleTitle").html());
            out.println(d.select("body #article .content .artImage").html());
            out.println(d.select("body #article .content .articleBody").html());
            out.println("</body>");
            out.println("</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                out.close();
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static Document cleanup(Document d) {
        d.select("script").remove();
        d.select("meta").remove();
        d.select("link").remove();
        d.select("style").remove();
        for (String ignore: SELECTORS_TO_IGNORE) {
            d.select(ignore).remove();
        }
        return d;
    }

    public static Set<String> loadStopWords() {
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
}
