package ee.ut.algorithmics.image.finder;

import ee.ut.algorithmics.keyword.finder.WordIncidence;
import javafx.util.Pair;

import java.io.File;
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

    private List<WordIncidence> keyphrases;

    public static void main(String[] args){
        ImageSearchManager imageManager = new ImageSearchManager();
        List<WordIncidence> phrases = new ArrayList<>();
        WordIncidence p1 = new WordIncidence("edgar", 97);
        WordIncidence p2 = new WordIncidence("eesti", 57);
        WordIncidence p3 = new WordIncidence("keskerakonna", 53);

        WordIncidence p4 = new WordIncidence("tallinna", 53);

        WordIncidence p5 = new WordIncidence("linnapea", 43);
        WordIncidence p6 = new WordIncidence("esimees", 39);
        WordIncidence p7 = new WordIncidence("riigikogu", 25);

        phrases.add(p1);
        phrases.add(p2);
        phrases.add(p3);
        phrases.add(p4);
        phrases.add(p5);
        phrases.add(p6);
        phrases.add(p7);

        imageManager.startDownload(imageManager.limitNumberOfImages(imageManager.calculateRealWeight(phrases), 200), args[0]);

    }

    public ImageSearchManager() {}

    public ImageSearchManager(List<WordIncidence> keyphrases) {
        this.keyphrases = keyphrases;
    }

    public void downloadPictures(String targetFolder) {
        File dir = new File(targetFolder);
        dir.mkdir();
        startDownload(limitNumberOfImages(calculateRealWeight(this.keyphrases), 200), dir.getAbsolutePath());
    }

    private List<WordIncidence> calculateRealWeight(List<WordIncidence> keyPhrases){

        List<WordIncidence> newPhrases = new ArrayList<WordIncidence>();

        int total = 0;

        for (WordIncidence item : keyPhrases){
            total += item.getIncidence();
        }

        for (WordIncidence item : keyPhrases){
            newPhrases.add(new WordIncidence(item.getWord(), (int) (((float) item.getIncidence() / total) * 100)));
        }

        return newPhrases;
    }

    private List<WordIncidence> limitNumberOfImages(List<WordIncidence> keyPhrases, int limit){

        int total = 0;

        for (WordIncidence item : keyPhrases){
            total += item.getIncidence();
        }

        List<WordIncidence> results;

        if (total > limit){
            results = new ArrayList<>();
            float multiplier = total / limit;

            for (WordIncidence item : keyPhrases){
                results.add(new WordIncidence(item.getWord(), (int) (item.getIncidence() * multiplier)));
            }
        } else {
            return keyPhrases;
        }

        return results;
    }

    public static void startDownload(List<WordIncidence> listOfPhrases, String path){
        final BlockingQueue<WordIncidence> queueOfKeyPhrases = new LinkedBlockingQueue<WordIncidence>(MAX_CAPACITY);

        final BlockingQueue<Pair<String, String>> queueOfLinks =
                new LinkedBlockingQueue<Pair<String, String>>(MAX_CAPACITY);


        queueOfKeyPhrases.addAll(listOfPhrases);

        final List<ImageFinder> imageFinders = new ArrayList<ImageFinder>();
        for (int i = 0; i < NUMBER_OF_THREADS_FOR_SEARCH; i++) {
            final ImageFinder consThread = new ImageFinder(queueOfKeyPhrases, queueOfLinks);
            consThread.start();
            imageFinders.add(consThread);
        }


        final List<ImageDownloader> imageDownloaders = new ArrayList<ImageDownloader>();
        for (int i = 0; i < NUMBER_OF_THREADS_FOR_DOWNLOAD + 2; i++) {
            final ImageDownloader consThread = new ImageDownloader(queueOfLinks, path);
            consThread.start();
            imageDownloaders.add(consThread);
        }

        // Wait while all threads to finish the job
        for (ImageFinder ifThread : imageFinders) {
            try {
                ifThread.join();
            }catch (InterruptedException iex){
                throw new RuntimeException(iex);
            }
        }

        queueOfLinks.add(new Pair("full_stop", "stop"));

        System.out.println("Search finished");

        // Wait while all threads to finish the job
        for (ImageDownloader dwnThread : imageDownloaders){
            try {
                dwnThread.join();
            }catch (InterruptedException iex){
                throw new RuntimeException(iex);
            }
        }

        System.out.println("Download finished");
    }
}
