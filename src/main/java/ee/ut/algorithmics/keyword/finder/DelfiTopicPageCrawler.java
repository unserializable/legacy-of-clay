package ee.ut.algorithmics.keyword.finder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Taimo Peelo
 */
public class DelfiTopicPageCrawler {
    public static final String DELFI_TOPIC_PAGE_BASE_URL="http://www.delfi.ee/teemalehed/";
    public static final String DELFI_TOPIC_PAGE_PAGE_PARAMETER="page";

    public static void main(String[] args) throws Exception {
		new DelfiTopicCrawler("edgar-savisaar").call();
    }

	private static class DelfiTopicCrawler implements Callable<List<String>> {
		private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		private List<String> topicArticleUrls = new ArrayList<>();
		private String delfiTopic;

		public DelfiTopicCrawler(String delfiTopic) {
			this.delfiTopic = delfiTopic;
		}

		@Override
		public List<String> call() {
			int page = 1;
			ScheduledFuture<Document> ffetch;
			try {
				do {
                    ffetch = scheduler.schedule(new DelfiTopicPageFetcher(delfiTopic, page), 5L, TimeUnit.SECONDS);
                    page++;
					Document doc = ffetch.get();
					findHeadLineLinks(doc, topicArticleUrls);
                } while (hasNextTopicPage(ffetch.get()));
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}

			return topicArticleUrls;
		}

		private static void findHeadLineLinks(Document d, List<String> outLinks) {
			Elements articleLinks = d.body().select(".headlines .headline a.article_title");
			for (Element l: articleLinks) {
				String link = l.attr("href");
				outLinks.add(link);
				System.out.println(link);
			}
		}

		private boolean hasNextTopicPage(Document topicPage) {
			return (topicPage != null) && !(topicPage.body().select(".headline").isEmpty());
		}
	}

	private static class DelfiTopicPageFetcher extends PageFetcher implements Callable<Document> {
		private String delfiTopic;
		private int page;

		public DelfiTopicPageFetcher(String delfiTopic, int page) {
			this.delfiTopic = delfiTopic;
			this.page = page;
		}

		private String delfiURL() {
			int len = DELFI_TOPIC_PAGE_BASE_URL.length() + DELFI_TOPIC_PAGE_PAGE_PARAMETER.length();
			len += String.valueOf(page).length() + "?=".length();
			return new StringBuilder(len)
					.append(DELFI_TOPIC_PAGE_BASE_URL)
					.append(delfiTopic).append("?")
					.append(DELFI_TOPIC_PAGE_PAGE_PARAMETER)
					.append("=")
					.append(page)
					.toString();
		}

		@Override
		public Document call() {
			return fetchDocument(delfiURL());
		}
	}

	private static class PageFetcher {
		private static final String UA_REAL = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:34.0) Gecko/20100101 Firefox/34.0";
		private static final String UA_FAKE = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

		protected Document fetchDocument(String url) {
			try {
				return Jsoup.connect(url).userAgent(UA_FAKE).get();
			} catch (IOException e) {
				// well then, nothing it is...
			}
			return null;
		}
	}
}
