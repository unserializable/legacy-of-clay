package ee.ut.algorithmics.collage.maker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class Image {

	private BufferedImage image;
	private ColorSpace cSpace;

	protected Image(File inputImageFile, ColorSpace cSpace) throws IOException{
		this.image = ImageIO.read(inputImageFile);
		if (cSpace.equals(ColorSpace.GRAYSCALE)){
			convertToGreyScale();
		}
		this.cSpace = cSpace;
	}
	
	protected void export(File outputFile) throws IOException {
		ImageIO.write(image, "png", outputFile);
	}

	protected void resize(int newWidth, int newHeight) {
		java.awt.Image tmp = getImage().getScaledInstance(newWidth, newHeight,
				java.awt.Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		setImage(dimg);
	}

	protected void resizeWithAspectRatio(int newWidth) {
		double aspectRatio = getWidth() / ((double) getHeight());
		int newHeight = (int) aspectRatio * newWidth;
		java.awt.Image tmp = getImage().getScaledInstance(newWidth, newHeight,
				java.awt.Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		setImage(dimg);
	}
	
	protected void convertToGreyScale() {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				int r = getRedValue(rgb);
				int g = getGreenValue(rgb);
				int b = getBlueValue(rgb);
				int l = (int) (.299 * r + .587 * g + .114 * b);
				image.setRGB(x, y, getIntFromRGB(l, l, l));
			}
		}
	}

	protected static int getBlueValue(int rgb) {
		return rgb & 0xFF;
	}

	protected static int getRedValue(int rgb) {
		return (rgb >> 16) & 0xFF;
	}

	protected static int getGreenValue(int rgb) {
		return (rgb >> 8) & 0xFF;
	}

	protected static int getIntFromRGB(int Red, int Green, int Blue) {
		Red = (Red << 16) & 0x00FF0000;
		Green = (Green << 8) & 0x0000FF00;
		Blue = Blue & 0x000000FF;
		return 0xFF000000 | Red | Green | Blue;
	}
	
	protected ColorSpace getColorSpace(){
		return cSpace;
	}
	
	protected int getTotalPixels() {
		return getHeight() * getWidth();
	};

	protected int getHeight() {
		return image.getHeight();
	}

	protected int getWidth() {
		return image.getWidth();
	}

	protected BufferedImage getImage() {
		return image;
	}

	protected void setImage(BufferedImage image) {
		this.image = image;
	}

}
