package spacegraph.space2d.widget.meter;

import jcog.Is;
import jcog.data.list.Lst;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/** from: https://github.com/jdrem/chernoff-faces */
@Is("Chernoff_face") public class ChernoffFace1 {

    final static private AffineTransform IDENTITY_TRANSFORM = new AffineTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    static public void createRandomImage(Graphics2D graphics) throws UnsupportedOperationException {
        createRandomImage(graphics, IDENTITY_TRANSFORM);
    }

    static public void createRandomImage(Graphics2D graphics, AffineTransform affineTransform) throws UnsupportedOperationException {
        Random r = new Random();
        ChernoffFace1.create(graphics, r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble());
    }
    public static void create(Graphics2D graphics, double head, double eyeShape, double pupilSize, double eyeBrowTilt, double noseSize,
                              double mouthHeight, double eyeSpacing, double eyeSize, double mouthWidth, double mouthOpeness) throws UnsupportedOperationException {
        create(graphics, IDENTITY_TRANSFORM, head, eyeShape, pupilSize, eyeBrowTilt, noseSize, mouthHeight, eyeSpacing, eyeSize, mouthWidth, mouthOpeness);
    }

    public static void create(Graphics2D graphics, AffineTransform transform, double head, double eyeShape, double pupilSize, double eyeBrowTilt, double noseSize,
                              double mouthHeight, double eyeSpacing, double eyeSize, double mouthWidth, double mouthOpeness) throws UnsupportedOperationException {
        java.util.List<Area> drawList = new Lst<>();
        java.util.List<Area> fillList = new Lst<>();

        if (head < 0.0 || head > 1.0)
            throw new UnsupportedOperationException("head");
        if (eyeShape < 0.0 || eyeShape > 1.0)
            throw new UnsupportedOperationException("eye shape");
        if (pupilSize < 0.0 || pupilSize > 1.0)
            throw new UnsupportedOperationException("pupil size");
        if (noseSize < 0.0 || noseSize > 1.0)
            throw new UnsupportedOperationException("nose size");
        if (mouthHeight < 0.0 || mouthHeight > 1.0)
            throw new UnsupportedOperationException("mouth height");
        if (eyeSpacing < 0.0 || eyeSpacing > 1.0)
            throw new UnsupportedOperationException("eye spacing");
        if (eyeSize < 0.0 || eyeSize > 1.0)
            throw new UnsupportedOperationException("eye size");
        if (mouthWidth < 0.0 || mouthWidth > 1.0)
            throw new UnsupportedOperationException("mouth width");
        if (mouthOpeness < 0.0 || mouthOpeness > 1.0)
            throw new UnsupportedOperationException("mouth openess");

        double width = 60.0;
        double height = 60.0;
        double xOffset = 0.0;
        skull(head, drawList, width, height, xOffset);


        eye(eyeShape, pupilSize, eyeSpacing, eyeSize, drawList, fillList);

        nose(noseSize, drawList);


        eyebrow(eyeBrowTilt, fillList);


        mouth(mouthHeight, mouthWidth, mouthOpeness, drawList);

        AffineTransform x = AffineTransform.getTranslateInstance(50.0, 50.0);

        x.concatenate(transform);
        x.concatenate(AffineTransform.getScaleInstance(4.0, 4.0));

        graphics.setColor(Color.BLACK);

        for (Area a : drawList) {
            a.transform(x);
            graphics.draw(a);
        }
        for (Area a : fillList) {
            a.transform(x);
            graphics.fill(a);
        }
    }

    private static void skull(double head, java.util.List<Area> drawList, double width, double height, double xOffset) {
        if (head < 0.5) {
            width = width - (0.5 - head) * 20.0;
            xOffset = (60.0 - width) / 2.0;
        } else if (head > 0.5) {
            height = height - (head - 0.5) * 20.0;
        }
        drawList.add(new Area(new Ellipse2D.Double(xOffset, 0.0, width, height)));
    }

    private static void eye(double eyeShape, double pupilSize, double eyeSpacing, double eyeSize, java.util.List<Area> drawList, java.util.List<Area> fillList) {
        double width;
        double height;
        // Eyes have 3 attributes:  shape (i.e. eccentricity, how eillipitical it is), distance apart and size.
        // default eye size is 12.5

        // eyesize min of 5, max of 15
        width = 5.0 + eyeSize * 15.0;
        height = 5.0 + eyeSize * 15.0;

        if (eyeShape < 0.5) {
            width -= width / 2.0 * eyeShape;
        } else if (eyeShape > 0.5) {
            height -= height / 2.0 * eyeShape;
        }

        double eyeOffset = (0.5 - eyeSpacing) * 10.0;

        // Default center of eyes is at (20,20) and (40,20)
        drawList.add(new Area(new Ellipse2D.Double(20.0 - width / 2.0 - eyeOffset, 20.0 - height / 2.0, width, height)));
        drawList.add(new Area(new Ellipse2D.Double(40.0 - width / 2.0 + eyeOffset, 20.0 - height / 2.0, width, height)));

        pupil(pupilSize, fillList, eyeOffset);
    }

    private static void pupil(double pupilSize, java.util.List<Area> fillList, double eyeOffset) {
        double height;
        double width;
        // Now do pupil size, reusing eyeOffset
        // Max pupil size should be less than min eye size
        // Default size is 2.5, Shrink or expand by a maximum of 2
        width = 0.5 + pupilSize * 4.0;
        height = 0.5 + pupilSize * 4.0;
        fillList.add(new Area(new Ellipse2D.Double(20.0 - width / 2.0 - eyeOffset, 20.0 - height / 2.0, width, height)));
        fillList.add(new Area(new Ellipse2D.Double(40.0 - width / 2.0 + eyeOffset, 20.0 - height / 2.0, width, height)));
    }

    private static void nose(double noseSize, java.util.List<Area> drawList) {
        // Nose
        // Top of nose is (30,20)
        // Base is at (25,length) and (35, length)

        double length = 5.0 + noseSize * 10.0;

        Path2D nose = new Path2D.Double();
        nose.moveTo(30, 20);
        nose.lineTo(27.5, 20 + length);
        nose.lineTo(32.5, 20 + length);
        nose.lineTo(30, 20);
        drawList.add(new Area(nose));
    }

    private static void eyebrow(double eyeBrowTilt, java.util.List<Area> fillList) {
        // eyebrow tilt
        double eyeBrowOffset = (0.5 - eyeBrowTilt) * 10.0;

        Path2D leb = new Path2D.Double();
        leb.moveTo(15, 10 + eyeBrowOffset);
        leb.lineTo(25, 10 - eyeBrowOffset);
        leb.lineTo(25, 10.5 - eyeBrowOffset);
        leb.lineTo(15, 10.5 + eyeBrowOffset);
        leb.lineTo(15, 10 + eyeBrowOffset);
        fillList.add(new Area(leb));

        Path2D reb = new Path2D.Double();
        reb.moveTo(35, 10 - eyeBrowOffset);
        reb.lineTo(45, 10 + eyeBrowOffset);
        reb.lineTo(45, 10.5 + eyeBrowOffset);
        reb.lineTo(35, 10.5 - eyeBrowOffset);
        reb.lineTo(35, 10 - eyeBrowOffset);
        fillList.add(new Area(reb));
    }

    private static void mouth(double mouthHeight, double mouthWidth, double mouthOpeness, java.util.List<Area> drawList) {
        // Smile has three attributes:
        // height of the mouth (on the y-axis i.e. how smiley or frowny it is)
        // width of the mouth (on the x-axis)
        // how open the mouth is (distance between lips

        Area mouthArea = new Area();

        double mouthWidthOffset = (0.5 - mouthWidth) * 12.5;
        double mouthHeightOffset = (mouthHeight - 0.5) * 2.0;
        double mouthOpenessOffset = mouthOpeness * 10.0 + 2.5;

        Point2D p1 = new Point2D.Double(17.5 + mouthWidthOffset, 40);
        Point2D p2 = new Point2D.Double(42.5 - mouthWidthOffset, 40);

        Point2D cpTop = new Point2D.Double(30.0, 40 + mouthHeightOffset * 20.0);

        Point2D cpBot = new Point2D.Double(30.0, 40 + mouthHeightOffset * 20.0  + mouthOpenessOffset);

        QuadCurve2D top = new QuadCurve2D.Double(p1.getX(), p1.getY(), cpTop.getX(), cpTop.getY(), p2.getX(), p2.getY());
        QuadCurve2D bottom = new QuadCurve2D.Double(p1.getX(), p1.getY(), cpBot.getX(), cpBot.getY(), p2.getX(), p2.getY());

        if (mouthHeight < 0.5) {
            mouthArea.add(new Area(top));
            mouthArea.subtract(new Area(bottom));
        } else if (mouthHeight > 0.5) {
            mouthArea.add(new Area(bottom));
            mouthArea.subtract(new Area(top));
        } else {
            mouthArea.add(new Area(top));
            mouthArea.add(new Area(bottom));
        }
        drawList.add(mouthArea);
    }

    public static void main(String[] args) throws UnsupportedOperationException, IOException {
        // Create some random images
        Random random = new Random(1L);
        BufferedImage image = new BufferedImage(2000, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 2000, 400));
        for (int i = 0; i <= 5; i++) {
            double p1 = random.nextDouble();
            double p2 = random.nextDouble();
            double p3 = random.nextDouble();
            double p4 = random.nextDouble();
            double p5 = random.nextDouble();
            double p6 = random.nextDouble();
            double p7 = random.nextDouble();
            double p8 = random.nextDouble();
            double p9 = random.nextDouble();
            double p10 = random.nextDouble();
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
        }
        ImageIO.write(image, "PNG", new File("/tmp/random.png"));

        // Do min/max
        double[] d = {0.0, 0.25, 0.5, 0.75, 1.0};
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, d[i], d[i], d[i], d[i], d[i], d[i], d[i], d[i], d[i], d[i]);
        }
        ImageIO.write(image, "PNG", new File("/tmp/min-max.png"));

        // Head shape
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, d[i], 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/head-shape.png"));

        // Eye shape
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, d[i], 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/eye-shape.png"));

        // Pupil size
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, d[i], 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/pupile-size.png"));

        // Eye brow tilt
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, 0.5, d[i], 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/eye-brow-tilt.png"));

        // Nose size
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, 0.5, 0.5, d[i], 0.5, 0.5, 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/nose-size.png"));

        // Mouth height
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, 0.5, 0.5, 0.5, d[i], 0.5, 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/mouth-height.png"));

        // Eye spacing
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, d[i], 0.5, 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/eye-spacing.png"));

        // Eye size
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, d[i], 0.5, 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/eye-size.png"));

        // Mouth width
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, d[i], 0.5);
        }
        ImageIO.write(image, "PNG", new File("/tmp/mouth-width.png"));

        // mouth openess
        image = new BufferedImage(400 * d.length, 400, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, 400 * d.length, 400));
        for (int i = 0; i < d.length; i++) {
            AffineTransform transform = AffineTransform.getTranslateInstance(i * 400.0, 0.0);
            ChernoffFace1.create(graphics, transform, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, d[i]);
        }
        ImageIO.write(image, "PNG", new File("/tmp/mouth-openess.png"));
    }
}