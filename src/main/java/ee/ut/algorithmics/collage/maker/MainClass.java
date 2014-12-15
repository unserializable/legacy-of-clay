package ee.ut.algorithmics.collage.maker;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import ee.ut.algorithmics.collage.maker.exceptions.NoImageCreatedException;


public class MainClass {
	public static void main(String[] args) {
		if (!(args.length == 3 || args.length == 2)) {
			System.err.println("Usage: MainClass baseImageFile collageImageFolder outputImageFile");
			System.exit(1);
		}
		if (args.length == 3){
			String baseImageFile = args[0];
			String collageImageFolder = args[1];
			String outputImageFile = args[2];
			try {
				List<File> replacementImages = getReplacementImages(new File(collageImageFolder));
				Collager.createCollageWithDefaultValues(new File(baseImageFile), replacementImages, new File(outputImageFile));
			} catch (NoImageCreatedException e) {
				e.printStackTrace();
			}
		}
		if (args.length == 2){
			String baseImageFile = args[0];
			String outputImageFile = args[1];
			try {
				Collager.createClasterizing(new File(baseImageFile), new File(outputImageFile), null, 0, 0, 0);
			} catch (NoImageCreatedException e) {
				e.printStackTrace();
			}
		}

	}

	private static List<File> getReplacementImages(final File folder) {
		List<File> replacementImages = new ArrayList<File>();
	    for (final File fileEntry : folder.listFiles()) {
	    	if(!fileEntry.isDirectory() && !fileEntry.isHidden()){
	    		replacementImages.add(fileEntry);
	    	}
	    }
		return replacementImages;
	}

}
