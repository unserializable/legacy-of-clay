package ee.ut.algorithmics.collage.maker;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ee.ut.algorithmics.collage.maker.exceptions.ClusteringNotRunException;

class InputImage extends Image {
	
	Cluster mainCluster;
	protected int maximumClusterWidth = -1;

	protected InputImage(File inputImage, ColorSpace cSpace, int maxImageWidth) throws IOException {
		super(inputImage, cSpace);
		if (getWidth() > maxImageWidth){
			resizeWithAspectRatio(maxImageWidth);
		}
	}
	
	private void clusterize(double splitCoefficent, int minimalClusterSize){
		System.out.println("Beginning clusterizing");
		long a = System.currentTimeMillis();
		mainCluster = new Cluster(this);
		mainCluster.createClusters(splitCoefficent, minimalClusterSize)
		;
		long b = System.currentTimeMillis();
		System.out.println("clusterizing ended in: " + ((double) (b-a))/1000.0);
	}
	
	protected void createCollage(double splitCoefficent, int minimalClusterSize, List<ReplacementImage> replacementImages){
		if (mainCluster == null){
			clusterize(splitCoefficent, minimalClusterSize);
		}
		System.out.println("Beginning image replacement");
		long a = System.currentTimeMillis();
		mainCluster.replaceClustersWithImages(replacementImages);
		long b = System.currentTimeMillis();
		System.out.println("image replacement ended in: " + ((double) (b-a))/1000.0);
		
	}
		
	protected int getLargestClusterWidth(double splitCoefficent, int minimalClusterSize) throws ClusteringNotRunException{
		if (mainCluster == null){
			clusterize(splitCoefficent, minimalClusterSize);
		}
		if (maximumClusterWidth <= 0){
			maximumClusterWidth = mainCluster.findLargestClusterWidth();
		}
		return maximumClusterWidth;
	}
	

}
