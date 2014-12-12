package ee.ut.algorithmics.collage.maker;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import ee.ut.algorithmics.collage.maker.exceptions.NoReplacementImageFoundException;



class Cluster {

	private Image parentImage;
	private int startX;
	private int endX;
	private int startY;
	private int endY;
	private static Random rand = new Random();
	
	private double rgbBlueAVGValue;
	private double rgbRedAVGValue;
	private double rgbGreenAVGValue;
	
	private Cluster[] subClusters = null;
	
	protected Cluster(InputImage image){
		parentImage = image;
		startX = 0;
		startY = 0;
		endX = parentImage.getWidth();
		endY = parentImage.getHeight();
	}
	
	private Cluster(Image parentImage, int startX, int endX, int startY, int endY) {
		super();
		this.parentImage = parentImage;
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
		writeImageRGBValues();
	}
	
	private boolean shouldSplit(double coefficent, Cluster[] clusters, final int MINIMAL_CLUSTER_SIZE){
		if (getTotalPixels()<MINIMAL_CLUSTER_SIZE){
			return false;
		}
		for (Cluster cluster: clusters){
			if (this.distanceTo(cluster) > coefficent){
				return true;
			}
		}
		return false;
	}
	
	protected void split(final double coefficent, final int minimalClusterSize){
		subClusters = new Cluster[4];
		subClusters[0] = new Cluster(parentImage, startX, startX + (endX - startX)/2, startY, startY + (endY-startY)/2);
		subClusters[1] = new Cluster(parentImage, startX + (endX - startX)/2, endX, startY, startY + (endY-startY)/2);
		subClusters[2] = new Cluster(parentImage, startX, startX + (endX - startX)/2, startY + (endY-startY)/2, endY);
		subClusters[3] = new Cluster(parentImage, startX + (endX - startX)/2, endX, startY + (endY-startY)/2, endY);
		if (shouldSplit(coefficent, subClusters, minimalClusterSize)){
			for (Cluster cluster: subClusters){
				cluster.split(coefficent, minimalClusterSize);
			}
		}
		else{
			subClusters = null;
		}
	}
	
	protected void replaceClustersWithImages(List<ReplacementImage> replacementImages) {
		if (subClusters == null){
			try{
				ReplacementImage closestImage = findReplacementImage(replacementImages);
				BufferedImage resizedCopyOfReplacement = closestImage.getResizedCopy(getWidth(), getHeight());
				replaceSectionWithImage(resizedCopyOfReplacement);
			}
			catch (NoReplacementImageFoundException e){
				e.printStackTrace();
			}
		}
		else{
			for (Cluster cluster: subClusters){
				cluster.replaceClustersWithImages(replacementImages);
			}
		}
	}
	
	private void replaceSectionWithImage(BufferedImage replacementImage){
		BufferedImage image = parentImage.getImage(); 
		for (int i = 0; i < replacementImage.getHeight(); i++){
			int y = i + startY;
			for (int j = 0; j < replacementImage.getWidth(); j++){
				int x = j + startX;
				image.setRGB(x, y, replacementImage.getRGB(j, i));
			}
		}
	}
	
	private class ReplacementImageWithDistance implements Comparable<ReplacementImageWithDistance>{
		
		ReplacementImage img;
		double distance;
		
		protected ReplacementImageWithDistance(ReplacementImage img, double distance){
			this.img = img;
			this.distance = distance;
		}

		@Override
		public int compareTo(ReplacementImageWithDistance arg0) {
			if (arg0 instanceof ReplacementImageWithDistance){
				return distance < ((ReplacementImageWithDistance) arg0).distance ? 1 : -1;
			}
			return -1;
		}

		protected ReplacementImage getImg() {
			return img;
		}
		
		protected double getDistance(){
			return distance;
		}
		
		protected void invertDistance(){
			distance = 1/distance;
		}
		
		protected void setDistance(double value){
			distance = value;
		}
	}
	
	private ReplacementImage findReplacementImage(List<ReplacementImage> replacementImages) throws NoReplacementImageFoundException {
		PriorityQueue<ReplacementImageWithDistance> queue = new PriorityQueue<>();
		int maxPicturesToUse = replacementImages.size()/5;
		if (maxPicturesToUse < 1){
			maxPicturesToUse = 1;
		}
		for (ReplacementImage replacementImage: replacementImages){
			queue.add(new ReplacementImageWithDistance(replacementImage, this.distanceTo(replacementImage)));
			if (queue.size() > maxPicturesToUse){
				queue.poll();
			}
		}
		ArrayList<ReplacementImageWithDistance> images = new ArrayList<>();
		double totalDistance = 0.0;
		for (ReplacementImageWithDistance img : queue){
			img.invertDistance();
			totalDistance = totalDistance + img.getDistance();
			img.setDistance(totalDistance);
			images.add(img);
		}
		double randomValue = rand.nextDouble()*totalDistance;
		ReplacementImage chosen = null;
		for (ReplacementImageWithDistance replacementImageWithDistance: images){
			if (replacementImageWithDistance.getDistance()>=randomValue){
				chosen = replacementImageWithDistance.getImg();
				break;
			}
		}
		if (chosen == null){
			throw new NoReplacementImageFoundException("Did not find any replacement images for rectangle: " + ("(" + startX + ";" + startY + ";" + endX + ";" + endY + ")"));
		}
		return chosen;
	}
	
	private void writeImageRGBValues(){
		double[] rgbValues = new double[3];
		BufferedImage img = parentImage.getImage();
		int counter = 0;
		for (int y = startY; y < endY; y++){
			for (int x = startX; x < endX; x++){
				rgbValues[2] = rgbValues[2] + Image.getBlueValue(img.getRGB(x, y));
				rgbValues[1] = rgbValues[1] + Image.getGreenValue(img.getRGB(x, y));
				rgbValues[0] = rgbValues[0] + Image.getRedValue(img.getRGB(x, y));
				counter = counter + 1;
			}
		}
		rgbRedAVGValue = rgbValues[0]/counter;
		rgbGreenAVGValue= rgbValues[1]/counter;
		rgbBlueAVGValue = rgbValues[2]/counter;
	}
	
	private double distanceTo(Cluster anotherCluster){
		double redDif = square(getRgbRedAVGValue() - anotherCluster.getRgbRedAVGValue());
		double blueDif = square(getRgbBlueAVGValue() - anotherCluster.getRgbBlueAVGValue());
		double greenDif = square(getRgbGreenAVGValue() - anotherCluster.getRgbGreenAVGValue());
		return Math.sqrt(redDif + blueDif + greenDif);
	}
	
	private double distanceTo(ReplacementImage image){
		double redDif = square(getRgbRedAVGValue() - image.getRgbRedAVGValue());
		double blueDif = square(getRgbBlueAVGValue() - image.getRgbBlueAVGValue());
		double greenDif = square(getRgbGreenAVGValue() - image.getRgbGreenAVGValue());
		return Math.sqrt(redDif + blueDif + greenDif);
	}	

	protected int getStartX() {
		return startX;
	}

	protected int getEndX() {
		return endX;
	}

	protected int getStartY() {
		return startY;
	}

	protected int getEndY() {
		return endY;
	}

	protected double getRgbBlueAVGValue() {
		return rgbBlueAVGValue;
	}

	protected double getRgbRedAVGValue() {
		return rgbRedAVGValue;
	}

	protected double getRgbGreenAVGValue() {
		return rgbGreenAVGValue;
	}
	
	protected int getHeight(){
		return endY - startY;
	}
	
	protected int getWidth(){
		return endX - startX;
	}
	
	protected int getTotalPixels(){
		return getHeight() * getWidth();
	}
	
	protected double getAspectRatio(){
		return ((double) getWidth())/((double) getHeight());
	}
	
	private static double square(double num){
		return num*num;
	}
	
}
