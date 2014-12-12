package ee.ut.algorithmics.collage.maker;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import ee.ut.algorithmics.collage.maker.exceptions.NoImageCreatedException;


public class MainClass {

	public static void main(String[] args) {
		try {
			List<File> replacementImages = getReplacementImages(new File("src\\main\\resources\\replacementImages\\"));
			Collager.createCollageWithDefaultValues(new File("src\\main\\resources\\savisaar.jpg"), replacementImages, new File("src\\main\\resources\\output.png"));
		} catch (NoImageCreatedException e) {
			e.printStackTrace();
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
