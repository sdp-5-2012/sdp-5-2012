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
 * Simple program that loads, rotates and displays an image. Uses the file
 * Duke_Blocks.gif, which should be in the same directory.
 * 
 * @author MAG
 * @version 20Feb2009
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