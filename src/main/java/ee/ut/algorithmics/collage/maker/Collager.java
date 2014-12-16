package ee.ut.algorithmics.collage.maker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ee.ut.algorithmics.collage.maker.exceptions.ClusteringNotRunException;
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
		if (splitCoefficent <= 0) splitCoefficent = 5;
		if (outputImageMaxWidth <= 0 ) outputImageMaxWidth = 3104;
		try{
			InputImage inputImage = new InputImage(inputImageFile, cSpace, outputImageMaxWidth);
			if (minPixelsInCluster <= 0) minPixelsInCluster = inputImage.getWidth()/100* inputImage.getHeight()/100;
			List<ReplacementImage> replacementImages = new ArrayList<ReplacementImage>();
			try {
				for (File replacementImageFile: replacementImageFiles){
					replacementImages.add(new ReplacementImage(replacementImageFile, cSpace, inputImage.getLargestClusterWidth(splitCoefficent, minPixelsInCluster)));
				}
			}
			catch (ClusteringNotRunException e) {
				e.printStackTrace();
				throw new NoImageCreatedException();
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
