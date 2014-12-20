package ee.ut.algorithmics.web;

import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ee.ut.algorithmics.collage.maker.Collager;
import ee.ut.algorithmics.collage.maker.exceptions.NoImageCreatedException;
import ee.ut.algorithmics.image.finder.ImageFinder;
import ee.ut.algorithmics.image.finder.ImageSearchManager;
import ee.ut.algorithmics.keyword.finder.WordIncidence;
import fi.iki.elonen.NanoHTTPD;

/**
 * Minimalist web server for collage creation demonstration.
 * @author Taimo Peelo
 */
public class Server extends NanoHTTPD {
	private static final int SERVICE_PORT = 11555;

	public Server() {
		super(SERVICE_PORT);
	}

	public static void main(String[] args) throws Exception {
		final Server server = new Server();
		server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				server.stop();
			}
		}));

		System.out.println("Press Enter to shut down web server on port " + SERVICE_PORT);
		System.in.read();
		System.out.println("Shutting down server...");
	}

	@Override
	public Response serve(IHTTPSession session) {
		if (Method.POST.equals(session.getMethod())) {
			// TODO:
			System.out.println("got POST with parms" + session.getParms());
			Map<String, String> files = new LinkedHashMap<>();
			try {
				session.parseBody(files);
				String uploadedImg = files.get("base_image");
				String keyPhrase = session.getParms().get("key_phrase");
				if (uploadedImg == null || keyPhrase == null || keyPhrase.isEmpty()) {
					return new Response(OK, MIME_HTML, getClass().getResourceAsStream("/index.html"));
				}

				return handleCollageRequest(keyPhrase, uploadedImg);
				//return new Response(OK, "image/png", new FileInputStream(new File(uploadedImg)));
			} catch (IOException | ResponseException ex) {
				return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Something went wrong");
			}
		} else {
			return new Response(OK, MIME_HTML, getClass().getResourceAsStream("/index.html"));
		}
	}

	private Response handleCollageRequest(String keyphrase, String mainImageFileName) {
		System.out.println("handling collage request " + keyphrase + " " + mainImageFileName);

		Path foundImgSavePath = null;
		try {
			foundImgSavePath = Files.createTempDirectory("imgsearchmnger").toAbsolutePath();
		} catch (IOException e) {
			return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Could not start image downloads.");
		}

		ImageSearchManager ism = new ImageSearchManager(Collections.singletonList(new WordIncidence(keyphrase, 100)));
		File saveDir = foundImgSavePath.toFile();
		ism.downloadPictures(saveDir.getAbsolutePath());

		List<File> inputImages = Arrays.asList(saveDir.listFiles());

		File outputImage;
		try {
			outputImage = File.createTempFile(mainImageFileName, ".png");
		} catch (IOException e) {
			return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Could not create image.");
		}
		try {
			System.out.println(Thread.currentThread().getName() + " creating collage to " + outputImage);
			Collager.createCollageWithDefaultValues(new File(mainImageFileName), inputImages, outputImage);
		} catch (NoImageCreatedException ex) {
			return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, ex.toString());
		}

		try {
			return new Response(OK, "image/png", new FileInputStream(outputImage));
		} catch (FileNotFoundException e) {
			return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Could not load collage.");
		}
	}
}
