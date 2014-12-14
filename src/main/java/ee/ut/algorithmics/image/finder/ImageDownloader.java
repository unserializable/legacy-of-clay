package ee.ut.algorithmics.image.finder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Iurii on 12/13/2014.
 */
public class ImageDownloader extends Thread {

    private final BlockingQueue<String> queueOfLinks;
    private String path;
    private boolean keepGoing = true;

    public ImageDownloader(final BlockingQueue<String> queue, String path) {
        this.queueOfLinks = queue;
        this.path = path;
    }

    public void run() {

        while (keepGoing) {
            try {

                if (queueOfLinks.size() > 0) {

                    String link = queueOfLinks.take();

                    if (link.equals("full_stop")) {
                        keepGoing = false;
                        queueOfLinks.add("full_stop");
                    } else {
                        save(link, this.path);
                    }
                } else {
                    Thread.sleep(700);
                }
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void save(String imageUrl, String fileDestination) {

        try {

            URL url = new URL(imageUrl);

            HttpURLConnection con;

            con = (HttpURLConnection) url.openConnection();

            String fileName = imageUrl.split("HN.")[1].split("&")[0] + ".png";

            InputStream is = con.getInputStream();
            OutputStream os = new FileOutputStream(fileDestination + fileName);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
