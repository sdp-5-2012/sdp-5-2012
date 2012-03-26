package JavaVision;
//
//
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
example of using the AffineTransform to rotate an image
	http://beginwithjava.blogspot.co.uk/2009/02/rotating-image-with-java.html
 */

public class RotateImage extends JPanel {

	public static BufferedImage correctRotation(BufferedImage image) {

		Graphics2D tg = (Graphics2D) image.getGraphics();

		AffineTransform at = new AffineTransform();
		at.rotate(Math.toRadians(0.5), image.getHeight() *1.28,
				image.getWidth() *1.28);

		tg.drawImage(image, at, null);
		return image;

	}

}