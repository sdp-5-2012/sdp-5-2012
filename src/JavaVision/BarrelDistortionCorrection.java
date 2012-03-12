package JavaVision;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


public class BarrelDistortionCorrection {

	
	private static int width = 0;
	private static int height = 0;
	private final static double ax = -0.016;
	private final static double ay = -0.06;
//	private final static double ax = -0.03;
//	private final static double ay = -0.08;
	

	public BufferedImage correct(Raster data) {
		// create a new raster to hold the generated image
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false,
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		WritableRaster wraster = data.createCompatibleWritableRaster();

		int numPixelsWritten = 0;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int[] out = convertPixel(i, j);
				int[] src = new int[3];

				if (out[0] >= 0 && out[0] < width && out[1] >= 0
						&& out[1] < height) {
					data.getPixel(i, j, src);
					wraster.setPixel(out[0], out[1], src);
					numPixelsWritten++;
				}
			}
		}

		// System.out.println(numPixelsWritten);

		
			BufferedImage img = new BufferedImage(cm, wraster, false, null);
			return img;
	}

	public static int[] convertPixel(int x, int y) {
		// System.out.println("Pixel: (" + x + ", " + y + ")");
		// first normalise pixel
		double px = (2 * x - width) / (double) width;
		double py = (2 * y - height) / (double) height;

		// System.out.println("Norm Pixel: (" + px + ", " + py + ")");
		// then compute the radius of the pixel you are working with
		double rad = px * px + py * py;

		// then compute new pixel'
		double px1 = px * (1 - ax * rad);
		double py1 = py * (1 - ay * rad);

		// then convert back
		int pixi = (int) ((px1 + 1) * width / 2);
		int pixj = (int) ((py1 + 1) * height / 2);
		// System.out.println("New Pixel: (" + pixi + ", " + pixj + ")");

		return new int[] { pixi, pixj };
	}

	public static BufferedImage correctPic(BufferedImage image) {

		
			width = image.getWidth();
			height = image.getHeight();

		Raster data = image.getData();
		BarrelDistortionCorrection bcd = new BarrelDistortionCorrection();
		BufferedImage correct = bcd.correct(data);
		return correct;
		
	}
}
