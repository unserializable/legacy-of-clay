package ee.ut.algorithmics.image.finder;

import ee.ut.algorithmics.keyword.finder.WordIncidence;
import javafx.util.Pair;
import org.core4j.Enumerable;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.core.OQueryRequest;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by taimo on 11.12.14.
 */
public class ImageFinder extends Thread {

    private static final String ACCOUNT_KEY = "UGLCr6t6LjbPnEo1S3OjyTBimaYFn/x+YHmeZnrhVgE";

    private final BlockingQueue<WordIncidence> queueOfKeyPhrases;
    private final BlockingQueue<Pair<String, String>> listOfLinks;
    private boolean keepGoing = true;

    public ImageFinder(final BlockingQueue<WordIncidence> queue, BlockingQueue<Pair<String, String>> listOfLinks) {
        this.queueOfKeyPhrases = queue;
        this.listOfLinks = listOfLinks;
    }

    public void run() {
        System.out.println("Image Finder " + Thread.currentThread().getName() + " running.");

        while (keepGoing) {
            try {
                if (queueOfKeyPhrases.size() > 0) {

                    WordIncidence phrase = queueOfKeyPhrases.take();
                    listOfLinks.addAll(findLinks(phrase));

                } else {
                    keepGoing = false;
                }
            } catch (final InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    /**
     * Query Bing for a relevant images according to query.
     *
     * @param phrase Phrase to search for and it's weight.
     * @return List of link to relevant images.
     */
    private List<Pair<String, String>> findLinks(WordIncidence phrase) {

        List<Pair<String, String>> listOfLinks = new ArrayList<Pair<String, String>>();

        ODataConsumer c = ODataConsumers
                .newBuilder("https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/")
                .setClientBehaviors(OClientBehaviors.basicAuth("accountKey", ACCOUNT_KEY))
                .build();

        OQueryRequest<OEntity> oRequest = c.getEntities("Image")
                .custom("Query", "%27" + phrase.getWord() + "%27");
        Enumerable<OEntity> entities = oRequest.execute();

        Enumerable<OEntity> entities1 = entities.take(phrase.getIncidence());

        for (OEntity record : entities1) {
            List<OProperty> listOfPropertiesForThumbnail = (List<OProperty>) record.getProperty("Thumbnail").getValue();

            listOfLinks.add(new Pair((String) listOfPropertiesForThumbnail.get(0).getValue(),
                    ((String) listOfPropertiesForThumbnail.get(1).getValue()).split("/")[1]));
        }
        return listOfLinks;

    }

}


