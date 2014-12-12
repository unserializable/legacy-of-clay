package ee.ut.algorithmics.collage.maker;

import java.io.File;
import java.io.IOException;
import java.util.List;

class InputImage extends Image {

	protected InputImage(File inputImage, ColorSpace cSpace, int maxImageWidth) throws IOException {
		super(inputImage, cSpace);
		if (getWidth() > maxImageWidth){
			resizeWithAspectRatio(maxImageWidth);
		}
	}
	
	protected void createCollage(double splitCoefficent, int minimalClusterSize, List<ReplacementImage> replacementImages){
		Cluster mainCluster = new Cluster(this);
		mainCluster.split(splitCoefficent, minimalClusterSize);
		mainCluster.replaceClustersWithImages(replacementImages);
	}
	

}
