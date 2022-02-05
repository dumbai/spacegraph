package spacegraph.audio;

import jcog.Config;
import jcog.Log;
import jcog.WTF;
import jcog.data.list.Lst;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.sound.sampled.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;


public class JSoundAudio extends Audio {

	private static final Logger logger = Log.log(JSoundAudio.class);

	public SourceDataLine out;

	@Deprecated private int outID = Config.INT("AUDIODEVICE", 9);

	public Thread thread;

	private FileOutputStream rec;

	public JSoundAudio(int polyphony) {
		super(polyphony);

		printMixerInfo();
		open();

	}

	public final void open() {
		open(outID);
	}

	public synchronized void open(int deviceID) {
		stop();

		AudioFormat format = new AudioFormat(rate, 16, 2, true, false);

//		List<Mixer.Info> x = new Lst<>(AudioSystem.getMixerInfo());
////		x.removeIf(xx ->
////			!(AudioSystem.getMixer(xx).getSourceLineInfo().length > 0));
//
//		//Line.Info[] x = AudioSystem.getSourceLineInfo(new DataLine.Info(SourceDataLine.class, format));
//
//		for (int i = 0, xLength = x.size(); i < xLength; i++) {
//			Mixer.Info xx = x.get(i);
//			System.out.println(i + " " + xx.getDescription());
//		}
//
//		if (deviceID >= x.size()) deviceID = 0;
//		SourceDataLine out;
//
//		for (int i = deviceID; i < x.size(); i++) {
//			Mixer xx = AudioSystem.getMixer(x.get(i));
//			try {
//
//				out = (SourceDataLine) xx
//						.getLine(new DataLine.Info(SourceDataLine.class, format));
//						//.get
//						//.getLine(s);
//				if (out == null) continue;
//
//				start(format, out, xx);
//				this.outID = i;
//
//				return;
//
//			} catch (Throwable e) {
//				//logger.info("try open: {} {}", xx.getMixerInfo().getDescription(), e);
//				//thread = null;
//				//throw new WTF(e);
//			}
//		}

		try {
			//out = AudioSystem.getSourceDataLine(format);


			List<Mixer.Info> x = new Lst<>(AudioSystem.getMixerInfo());
			Mixer.Info xo = x.get(outID);
			//System.out.println(xo + "\t" + xo.getDescription());

			var out = AudioSystem.getSourceDataLine(format, xo);
			//var out = AudioSystem.getSourceDataLine(format);
			start(format, out, null);

			return;

		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		throw new UnsupportedOperationException("no devices available");
	}

	private void start(AudioFormat format, SourceDataLine out, @Deprecated @Nullable Mixer xx) throws LineUnavailableException {
		int bufferBytes = bufferSamples * 2 * 2;
		out.open(format, bufferBytes);
//				out.open();
		out.start();

		this.out = out;

		logger.info("start {}\n\t{}\n\t{}",
				xx!=null ? xx.getMixerInfo().getDescription() : null,
				out, out.getLineInfo());


		try {
			FloatControl volumeControl = (FloatControl) out.getControl(FloatControl.Type.MASTER_GAIN);
			volumeControl.setValue(volumeControl.getMaximum());
		} catch (IllegalArgumentException ignored) {
			System.out.println("Failed to setAt the sound volume");
		}
	}

	public synchronized void stop() {
		if (this.out!=null) {
			this.out.close();
			this.out = null;
		}
		if (this.thread!=null) {
			this.thread.interrupt();
			//this.thread.stop();
			this.thread = null;
		}
	}



	/**
	 * Prints information about the current Mixer to System.out.
	 */
	static void printMixerInfo() {

		Mixer.Info[] mi = AudioSystem.getMixerInfo();
		for (int i = 0; i < mi.length; i++) {

			String name = mi[i].getName();
			if (name.isEmpty()) name = "Unnamed";

			System.out.println(i + ") " + name + " --- " + mi[i].getDescription());
			Mixer m = AudioSystem.getMixer(mi[i]);

			for (Line.Info s : m.getSourceLineInfo())
				System.out.println("  -> " + s);

			for (Line.Info t : m.getTargetLineInfo())
				System.out.println("  <- " + t);

		}
	}



	public synchronized void record(String path) throws FileNotFoundException {
		if (rec!=null)
			throw new UnsupportedOperationException();
		logger.info("recording to: {}", path);
		rec = new FileOutputStream(path, false);
	}

	@Override
	protected void push(byte[] ba) {
		int bw = bufferSamples * 2 * 2;
		int br = out.write(ba, 0, bw);
		if (br != bw)
			throw new WTF();

		if (rec != null) {
			try {
				rec.write(ba, 0, bw);
				rec.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}