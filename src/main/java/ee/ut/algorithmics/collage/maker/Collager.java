package ee.ut.algorithmics.collage.maker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ee.ut.algorithmics.collage.maker.exceptions.ClusteringNotRunException;
import ee.ut.algorithmics.collage.maker.exceptions.NoImageCreatedException;




public class Collager {
	
	private static final int DEFAULT_MINIMAL_CLUSTER_WIDTH = 50;
	private static final int DEFAULT_OUTPUT_IMAGE_MAX_WIDTH = 0;
	private static final double DEFAULT_SPLIT_COEFFICENT = 8;
	private static final ColorSpace DEFAULT_COLOR_SPACE = ColorSpace.sRGB;
	
	public static void createCollage(
									File inputImageFile, 
									List<File> replacementImageFiles, 
									File outputImage, ColorSpace cSpace,
									int minClusterWidth, 
									int outputImageMaxWidth,  
									double splitCoefficent,
									boolean fastFlag
									) throws NoImageCreatedException{
		
		if (cSpace == null)	cSpace = DEFAULT_COLOR_SPACE;
		if (splitCoefficent <= 0) splitCoefficent = DEFAULT_SPLIT_COEFFICENT;
		if (outputImageMaxWidth <= 0 ) outputImageMaxWidth = DEFAULT_OUTPUT_IMAGE_MAX_WIDTH;
		if (minClusterWidth <= 0) minClusterWidth = DEFAULT_MINIMAL_CLUSTER_WIDTH;
		try{
			InputImage inputImage = new InputImage(inputImageFile, cSpace, outputImageMaxWidth);
			List<ReplacementImage> replacementImages = new ArrayList<ReplacementImage>();
			try {
				for (File replacementImageFile: replacementImageFiles){
					replacementImages.add(new ReplacementImage(replacementImageFile, cSpace, inputImage.getLargestClusterWidth(splitCoefficent, minClusterWidth)));
				}
			}
			catch (ClusteringNotRunException e) {
				e.printStackTrace();
				throw new NoImageCreatedException();
			}
			inputImage.createCollage(splitCoefficent, DEFAULT_MINIMAL_CLUSTER_WIDTH, replacementImages, fastFlag);
			inputImage.export(outputImage);	
		}
		catch(IOException e){
			e.printStackTrace();
			throw new NoImageCreatedException("No image was created due to errors in progress. Please check stacktrace for error information");
		}
				
	}
	
	public static void createCollageWithDefaultValues(File inputImageFile, List<File> replacementImageFiles, File outputImage) throws NoImageCreatedException{
		createCollage(inputImageFile, replacementImageFiles, outputImage, null, 0, 0, 0.0, false);
	}
	
	public static void createClasterizing(
											File inputImageFile, 
											File outputImage, 
											ColorSpace cSpace,
											int minClusterWidth, 
											int outputImageMaxWidth,  
											double splitCoefficent
										) throws NoImageCreatedException{
		if (cSpace == null)	cSpace = DEFAULT_COLOR_SPACE;
		if (splitCoefficent <= 0) splitCoefficent = DEFAULT_SPLIT_COEFFICENT;
		if (outputImageMaxWidth <= 0 ) outputImageMaxWidth = DEFAULT_OUTPUT_IMAGE_MAX_WIDTH;
		if (minClusterWidth <= 0) minClusterWidth = DEFAULT_MINIMAL_CLUSTER_WIDTH;
		try{
			InputImage inputImage = new InputImage(inputImageFile, cSpace, outputImageMaxWidth);
			inputImage.createClasteringWithWhiteEdges(splitCoefficent, minClusterWidth);
			inputImage.export(outputImage);	
		}
		catch(IOException e){
			e.printStackTrace();
			throw new NoImageCreatedException("No image was created due to errors in progress. Please check stacktrace for error information");
		}
		
	}

}
