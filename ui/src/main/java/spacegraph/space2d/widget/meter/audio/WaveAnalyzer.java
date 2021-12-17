//package spacegraph.space2d.widget.meter.audio;
//
///**
// **   __ __|_  ___________________________________________________________________________  ___|__ __
// **
// **
// **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /
// **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/
// **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\
// ** /  \____\                       http:
// ** \  /   "' _________________________________________________________________________ `"   \  /
// **  \/____.                                                                             .____\/
// **
// ** Main class for the music subsystem of the framework. Provides an easy to use interface for
// ** asynchronous music playback. Given the 3rd party service provider libraries are supplied it
// ** is capable to playback "ogg vorbis" and "mpeg layer 3" music files. Other than simple
// ** playback it internally uses the "KJ-DSS Project" by Kristofer Fudalewski (http:
// ** to provide a joined FFT spectrum via getFFTSpectrum() and a graphical scope and spectrum
// ** analyzer via getScopeAndSpectrumAnalyzerVisualization(). The FFT spectrum can be utilized to
// ** get some easy synchronization of music an visuals.
// **
// **/
//
//import spacegraph.audio.AudioSource;
//import spacegraph.audio.WaveCapture;
//import spacegraph.space2d.Surface;
//import spacegraph.space2d.container.grid.Gridding;
//import spacegraph.video.Tex;
//
//import java.awt.image.BufferedImage;
//
//public class WaveAnalyzer {
//
//    private final BaseMusic_ScopeAndSpectrumAnalyzer analyzer;
//    private final BaseMusic_DigitalSignalSynchronizer synchronizer;
//    private final float[] mFFTSpectrum_Empty;
//
//    private int fps = 30;
//
//    public WaveAnalyzer(WaveCapture src) {
//
//        mFFTSpectrum_Empty = new float[BaseMusic_ScopeAndSpectrumAnalyzer.DEFAULT_SPECTRUM_ANALYSER_BAND_COUNT];
//
//
//            synchronizer = new BaseMusic_DigitalSignalSynchronizer(fps);
//
//            synchronizer.addAt(analyzer = new BaseMusic_ScopeAndSpectrumAnalyzer());
//
//            synchronizer.start((AudioSource)(src.source));
//
//            src.frame.on(f->{
//                AudioSource in = (AudioSource) src.source;
//                synchronizer.writeAudioData(in.preBuffer, 0, in.audioBytesRead );
//
//                synch();
//            });
//
//    }
//
//
//
//
//
//
//
//
//
//    private void synch() {
//
//            BaseMusic_DigitalSignalSynchronizer.Synchronizer tSynchronizer = synchronizer.getInternalSynchronizer();
//            if (tSynchronizer!=null) {
//                tSynchronizer.synchronize();
//            }
//
//    }
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
//    public float[] getFFTSpectrum() {
//        float[] tFFTSpectrum = analyzer.getFFTSpectrum();
//        if (tFFTSpectrum!=null) {
//            return tFFTSpectrum;
//        }
//        return mFFTSpectrum_Empty;
//    }
//
//    public BufferedImage getScopeAndSpectrumAnalyzerVisualization() {
//        return analyzer.getScopeAndSpectrumAnalyzerVisualization();
//    }
//
//    public Surface view() {
//        return new Gridding() {
//
//            final Tex tex = new Tex();
//
//            {
//                setAt(tex.view());
//            }
//
//
//            @Override
//            public void prePaint(int dtMS) {
//                tex.update(analyzer.getScopeAndSpectrumAnalyzerVisualization());
//            }
//        };
//    }
//}
