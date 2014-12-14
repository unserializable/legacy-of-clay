package ee.ut.algorithmics.image.finder;

import java.util.ArrayList;
import java.util.List;
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
        KeyPhrase p1 = new KeyPhrase("tree", 25);
        KeyPhrase p2 = new KeyPhrase("Edgar Savisaar", 15);
        KeyPhrase p3 = new KeyPhrase("Microsoft", 17);

        phrases.add(p1);
        phrases.add(p2);
        phrases.add(p3);

        imageManager.start(phrases, args[0]);
    }

    public static void start(List<KeyPhrase> listOfPhrases, String path){

        final BlockingQueue<KeyPhrase> queueOfKeyPhrases = new LinkedBlockingQueue<KeyPhrase>(MAX_CAPACITY);

        final BlockingQueue<String> queueOfLinks = new LinkedBlockingQueue<String>(MAX_CAPACITY);


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

        // Wait while all threads to finish the job
        for (ImageDownloader threat : consumers1){

            try {
                threat.join();

            }catch (InterruptedException iex){
                iex.printStackTrace();
            }
        }
    }


}
