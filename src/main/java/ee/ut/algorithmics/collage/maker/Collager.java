package ee.ut.algorithmics.collage.maker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ee.ut.algorithmics.collage.maker.exceptions.NoImageCreatedException;


public class Collager {
	
	public static void createCollage(
									File inputImageFile, 
									List<File> replacementImageFiles, 
									File outputImage, ColorSpace cSpace,
									int minPixelsInCluster, 
									int outputImageMaxWidth,  
									double splitCoefficent
									) throws NoImageCreatedException{
		
		if (cSpace == null)	cSpace = ColorSpace.sRGB;
		if (minPixelsInCluster <= 0) minPixelsInCluster = 16;
		if (splitCoefficent <= 0) splitCoefficent = 4;
		if (outputImageMaxWidth <= 0 ) outputImageMaxWidth = 1280;
		try{
			InputImage inputImage = new InputImage(inputImageFile, cSpace, outputImageMaxWidth);
			List<ReplacementImage> replacementImages = new ArrayList<ReplacementImage>();
			
			for (File replacementImageFile: replacementImageFiles){
				replacementImages.add(new ReplacementImage(replacementImageFile, cSpace, inputImage.getWidth()));
			}
			inputImage.createCollage(splitCoefficent, minPixelsInCluster, replacementImages);
			inputImage.export(outputImage);	
		}
		catch(IOException e){
			e.printStackTrace();
			throw new NoImageCreatedException("No image was created due to errors in progress. Please check stacktrace for error information");
		}
				
	}
	
	public static void createCollageWithDefaultValues(File inputImageFile, List<File> replacementImageFiles, File outputImage) throws NoImageCreatedException{
		createCollage(inputImageFile, replacementImageFiles, outputImage, null, 0, 0, 0.0);
	}

}
