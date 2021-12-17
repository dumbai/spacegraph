package spacegraph.util;


import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.collections.impl.tuple.Tuples.pair;

/*
 *  Convenience class to create and optionally save to a file a
 *  BufferedImage of an area on the screen. Generally there are
 *  four different scenarios. Create an image of:
 *
 *  a) an entire component
 *  b) a region of the component
 *  c) the entire desktop
 *  d) a region of the desktop
 *
 *  The first two use the Swing paint() method to draw the
 *  component image to the BufferedImage. The latter two use the
 *  AWT Robot to create the BufferedImage.
 *
 *	The created image can then be saved to a file by usig the
 *  writeImage(...) method. The type of file must be supported by the
 *  ImageIO write method.
 *
 *  Although this class was originally designed to create an image of a
 *  component on the screen it can be used to create an image of components
 *  not displayed on a GUI. Behind the scenes the component will be given a
 *  size and the component will be layed out. The default size will be the
 *  preferred size of the component although you can invoke the setSize()
 *  method on the component before invoking a createImage(...) method. The
 *  default functionality should work in most cases. However the only
 *  foolproof way to get a image to is make sure the component has been
 *  added to a realized window with code something like the following:
 *
 *  JFrame frame = new JFrame();
 *  frame.setContentPane( someComponent );
 *  frame.pack();
 *  ScreenImage.createImage( someComponent );
 *
 */
public enum AWTCamera {
	;

	private static final Map<Component, Pair<AtomicBoolean, Graphics2D>> graphicsDrawers = new WeakHashMap<>();

	public static BufferedImage get(Component component, @Nullable BufferedImage image) {
		return get(component, image, null);
	}

	/*
	 *  Create a BufferedImage for Swing components.
	 *  The entire component will be captured to an image.
	 *
	 *  @param  component Swing component to create image from
	 *  @return	image the image for the given region
	 */
	public static BufferedImage get(Component component, @Nullable BufferedImage out, @Nullable Rectangle in) {
		Pair<AtomicBoolean, Graphics2D> pair = graphicsDrawers.get(component);
		if (!((pair == null || pair.getOne().compareAndSet(false, true))))
			return out;
		Dimension d = component.getSize();

		if (d.width == 0 || d.height == 0)
			component.setSize(d = component.getPreferredSize());


		if ((in == null) || (in.width != d.width) || (in.height != d.height)) {
			if (in != null && (in.width <= 0 || in.height <= 0))
				return null;
			in = new Rectangle(0, 0, d.width, d.height);
		}


		Graphics2D g2d = pair != null ? pair.getTwo() : null;
		if (g2d == null || out == null || out.getWidth() != in.width || out.getHeight() != in.height) {
			if (g2d != null)
				g2d.dispose();
			GraphicsConfiguration gc = component.getGraphicsConfiguration();
			out = gc != null ? gc.createCompatibleImage(in.width, in.height) : new BufferedImage(in.width, in.height, BufferedImage.TYPE_INT_ARGB);
			g2d = (Graphics2D) out.getGraphics();
			graphicsDrawers.put(component, pair = pair(new AtomicBoolean(), g2d));
		}

		//TODO cleanup
		Rectangle finalRegion = in;
		Graphics2D finalG2d = g2d;
		//SwingUtilities.invokeLater(() -> { //TODO optional double buffer. without it, waiting for AWT thread may display tearing
		repaint(component, finalRegion, finalG2d);
		pair.getOne().set(false);
		//});

		return out;
	}

	public static void repaint(Component c, Rectangle in, Graphics2D out) {
		if (!c.isDisplayable()) {
			Dimension d1 = c.getSize();

			if (d1.width == 0 || d1.height == 0) {
				d1 = c.getPreferredSize();
				c.setSize(d1);
			}

			layout(c);
		}

		if (!c.isOpaque()) {
			out.setColor(c.getBackground());
			out.fillRect(in.x, in.y, in.width, in.height);
		}
		out.setTransform(AffineTransform.getTranslateInstance(0, 0));
		out.translate(-in.x, -in.y);
		c.paint(out);
	}

	/**
	 * Convenience method to create a BufferedImage of the desktop
	 *
	 * @param fileName name of file to be created or null
	 * @return image the image for the given region
	 * @throws AWTException see Robot class constructors
	 * @throws IOException  if an error occurs during writing
	 */
	public static BufferedImage createDesktopImage()
		throws AWTException {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle region = new Rectangle(0, 0, d.width, d.height);
		return AWTCamera.get(region);
	}

	/*
	 *  Create a BufferedImage for AWT components.
	 *
	 *  @param  component AWT component to create image from
	 *  @return	image the image for the given region
	 *  @exception AWTException see Robot class constructors
	 */
	public static BufferedImage get(Component component) throws AWTException {
		Point p = new Point(0, 0);
		SwingUtilities.convertPointToScreen(p, component);
		Rectangle region = component.getBounds();
		region.x = p.x;
		region.y = p.y;
		return AWTCamera.get(region);
	}

	/**
	 * Create a BufferedImage from a rectangular region on the screen.
	 * This will include Swing components JFrame, JDialog and JWindow
	 * which all extend from Component, not JComponent.
	 *
	 * @param region region on the screen to create image from
	 * @return image the image for the given region
	 * @throws AWTException see Robot class constructors
	 */
	private static BufferedImage get(Rectangle region) throws AWTException {
		return new Robot().createScreenCapture(region);
	}


	private static void layout(Component component) {
		component.doLayout();

		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents())
				layout(child);
		}
	}


}