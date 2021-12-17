//package spacegraph.space2d.dyn2d;
//
//
//import boofcv.alg.background.BackgroundModelStationary;
//import boofcv.factory.background.ConfigBackgroundGaussian;
//import boofcv.factory.background.FactoryBackgroundModel;
//import boofcv.io.image.ConvertBufferedImage;
//import boofcv.struct.image.GrayF32;
//import boofcv.struct.image.GrayU8;
//import boofcv.struct.image.ImageType;
//import jcog.exe.Loop;
//import spacegraph.SpaceGraph;
//import spacegraph.space2d.container.grid.Gridding;
//import spacegraph.video.Tex;
//import spacegraph.video.WebCam;
//
//import java.awt.image.BufferedImage;
//
//public class ExampleBackgroundRemovalStationary {
//    public static void main(String[] args) {
//
//        WebCam c = WebCam.the();
//
//        Tex output = new Tex();
//        SpaceGraph.window(new Gridding(
//                new WebCam.WebCamSurface(c),
//            output.view()
//        ), 800, 800);
//
//
//
//
//
//        ImageType imageType = ImageType.single(GrayF32.class);
//
//
//
//
//
//        ConfigBackgroundGaussian configGaussian = new ConfigBackgroundGaussian(40, 0.0005f);
//        configGaussian.initialVariance = 100;
//        configGaussian.minimumDifference = 10f;
//
//
//        BackgroundModelStationary background =
//
//                FactoryBackgroundModel.stationaryGaussian(configGaussian, imageType);
//
//
//
//
//        GrayU8 segmented = new GrayU8(c.width, c.height);
//        GrayF32 input = new GrayF32(c.width, c.height);
//
//
//
//        BufferedImage segmentedVis = new BufferedImage(c.width, c.height, BufferedImage.TYPE_INT_RGB);
//
//        new Loop(10f) {
//            @Override
//            public boolean next() {
//                BufferedImage img = c.image;
//                if (img != null) {
//
//
//                    ConvertBufferedImage.convertFrom(img, input, true);
//
//
//                    background.segment(input, segmented);
//
//                    background.updateBackground(input);
//
//                    byte[] b = segmented.data;
//                    for (int i = 0; i < b.length; i++) {
//                        if (b[i]!=0)
//                            b[i] = 127;
//                    }
//
//                    output.update(
//
//                        ConvertBufferedImage.convertTo(segmented, segmentedVis)
//                    );
//                }
//
//
//
//
//
//
//
//
//
//
//
//                return true;
//            }
//        };
//    }
//}