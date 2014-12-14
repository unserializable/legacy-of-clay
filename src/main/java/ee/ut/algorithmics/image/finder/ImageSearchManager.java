package ee.ut.algorithmics.image.finder;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Iurii on 12/13/2014.
 */
public class ImageSearchManager {

    private static final int MAX_CAPACITY = 250;
    private static final int NUMBER_OF_THREADS_FOR_SEARCH = 2;
    private static final int NUMBER_OF_THREADS_FOR_DOWNLOAD = 3;

    public static void main(String[] args){

        ImageSearchManager imageManager = new ImageSearchManager();

        List<KeyPhrase> phrases = new ArrayList<KeyPhrase>();
        KeyPhrase p1 = new KeyPhrase("Vilja Savisaar", 424);
        KeyPhrase p2 = new KeyPhrase("Siiri Oviir", 81);
        KeyPhrase p3 = new KeyPhrase("Vanilla Ninja", 5);

        KeyPhrase p4 = new KeyPhrase("Vilja Savisaar", 424);

        KeyPhrase p5 = new KeyPhrase("Kadri Simson", 761);
        KeyPhrase p6 = new KeyPhrase("Ain Seppik", 391);
        KeyPhrase p7 = new KeyPhrase("Mart Laar", 427);
        KeyPhrase p8 = new KeyPhrase("Lennart Meri", 181);
        KeyPhrase p9 = new KeyPhrase("Tiit Vähi", 107);
        KeyPhrase p10 = new KeyPhrase("Siret Kotka", 391);

        phrases.add(p1);
        phrases.add(p2);
        phrases.add(p3);
        phrases.add(p4);
        phrases.add(p5);
        phrases.add(p6);
        phrases.add(p7);
        phrases.add(p8);
        phrases.add(p9);
        phrases.add(p10);

        imageManager.start(imageManager.limitNumberOfImages(imageManager.calculateRealWeight(phrases), 200), args[0]);

    }


    private List<KeyPhrase> calculateRealWeight(List<KeyPhrase> keyPhrases){

        List<KeyPhrase> newPhrases = new ArrayList<KeyPhrase>();

        int total = 0;

        for (KeyPhrase item : keyPhrases){
            total += item.getWeight();
        }

        for (KeyPhrase item : keyPhrases){
            newPhrases.add(new KeyPhrase(item.getPhrase(), (int) (((float) item.getWeight() / total) * 100)));
        }

        return newPhrases;
    }

    private List<KeyPhrase> limitNumberOfImages(List<KeyPhrase> keyPhrases, int limit){

        int total = 0;

        for (KeyPhrase item : keyPhrases){
            total += item.getWeight();
        }

        if (total > limit){
            float multiplier = total / limit;

            for (KeyPhrase item : keyPhrases){
                item.setWeight((int) (item.getWeight() * multiplier));
            }
        }

        return keyPhrases;
    }

    public static void start(List<KeyPhrase> listOfPhrases, String path){

        final BlockingQueue<KeyPhrase> queueOfKeyPhrases = new LinkedBlockingQueue<KeyPhrase>(MAX_CAPACITY);

        final BlockingQueue<Pair<String, String>> queueOfLinks =
                new LinkedBlockingQueue<Pair<String, String>>(MAX_CAPACITY);


        queueOfKeyPhrases.addAll(listOfPhrases);

        final List<ImageFinder> consumers = new ArrayList<ImageFinder>();
        for (int i = 0; i < NUMBER_OF_THREADS_FOR_SEARCH; i++) {
            final ImageFinder consThread = new ImageFinder(queueOfKeyPhrases, queueOfLinks);
            consThread.start();
            consumers.add(consThread);
        }


        final List<ImageDownloader> consumers1 = new ArrayList<ImageDownloader>();
        for (int i = 0; i < NUMBER_OF_THREADS_FOR_DOWNLOAD + 2; i++) {
            final ImageDownloader consThread = new ImageDownloader(queueOfLinks, path);
            consThread.start();
            consumers1.add(consThread);
        }


        // Wait while all threads to finish the job
        for (ImageFinder threat : consumers){

            try {
                threat.join();

            }catch (InterruptedException iex){
                iex.printStackTrace();
            }
        }

        queueOfLinks.add(new Pair("full_stop", "stop"));

        System.out.println("Search finished");

        // Wait while all threads to finish the job
        for (ImageDownloader threat : consumers1){

            try {
                threat.join();

            }catch (InterruptedException iex){
                iex.printStackTrace();
            }
        }

        System.out.println("Download finished");

    }


}
