package ee.ut.algorithmics.collage.maker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.plaf.metal.MetalBorders.Flush3DBorder;

public class ReplacementImage extends Image{
	
	private double rgbBlueAVGValue;
	private double rgbRedAVGValue;
	private double rgbGreenAVGValue;
	
	private Map<int[], BufferedImage> cache;

	public ReplacementImage(File imageFile, ColorSpace cSpace, int largestClusterWidth) throws IOException {
		super(imageFile, cSpace);
		cache = new HashMap<int[], BufferedImage>();
		double originalWidth = getWidth();
		double originalHeight = getHeight();
		double aspectRatio = originalWidth/originalHeight;
		int replacementImageWidth = largestClusterWidth;
		if (getWidth()>replacementImageWidth){
			resize(replacementImageWidth, (int) (replacementImageWidth/aspectRatio));
		}
		writeImageRGBValues();
	}
	
	public BufferedImage getResizedCopy(int newW, int newH){
		int[] sizeParameters = {newW, newH};
		if (cache.containsKey(sizeParameters)){
			return cache.get(sizeParameters);
		}
		java.awt.Image tmp = getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_DEFAULT);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();
	    cache.put(sizeParameters, dimg);
	    return dimg;
	}
	
	private void writeImageRGBValues(){
		double[] rgbValues = new double[3];
		int counter = 0;
		for (int y = 0; y < getImage().getHeight(); y++){
			for (int x = 0; x < getImage().getWidth(); x++){
				rgbValues[2] = rgbValues[2] + getBlueValue(getImage().getRGB(x, y));
				rgbValues[1] = rgbValues[1] + getGreenValue(getImage().getRGB(x, y));
				rgbValues[0] = rgbValues[0] + getRedValue(getImage().getRGB(x, y));
				counter = counter + 1;
			}
		}
		rgbRedAVGValue = rgbValues[0]/counter;
		rgbGreenAVGValue= rgbValues[1]/counter;
		rgbBlueAVGValue = rgbValues[2]/counter;
	}

	public double getRgbBlueAVGValue() {
		return rgbBlueAVGValue;
	}

	public double getRgbRedAVGValue() {
		return rgbRedAVGValue;
	}

	public double getRgbGreenAVGValue() {
		return rgbGreenAVGValue;
	}	
	
}
