package spacegraph.audio.speech;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

/**
 * Voice synthesizer models the vocal tract
 *
 * @author Joel Niederhauser
 * @version 0.1
 */
public class VocSynth extends Canvas implements MouseListener, MouseMotionListener
// Interface for displaying and changing vocal tract
{
//	VocSynth parent;
	double[] tract;
	int tlength;

	public VocSynth(VocSynth par, double[] tract, int tlength) {
//		parent = par;
		this.tract = tract;
		this.tlength = tlength;
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground(Color.white);
	}

	public void paint(Graphics g) {
		Dimension d = size();
		int w = d.width;
		int h = d.height;
		//draw frame
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, w - 1, 0);
		g.drawLine(0, 0, 0, h - 1);
		g.setColor(Color.gray);
		g.drawLine(w - 1, h - 1, w - 1, 0);
		g.drawLine(w - 1, h - 1, 0, h - 1);
		g.setColor(Color.black);
		//end of frame
		for (int i = 0; i < tlength; i++)
		// draw all tubes of vocal tract
		{
			g.drawLine(i * w / tlength, h / 2 - (int) (tract[i] * h / 2), (i + 1) * w / tlength, h / 2 - (int) (tract[i] * h / 2));
			g.drawLine(i * w / tlength, h / 2 + (int) (tract[i] * h / 2), (i + 1) * w / tlength, h / 2 + (int) (tract[i] * h / 2));
			if (i < tlength - 1) {
				g.drawLine((i + 1) * w / tlength, h / 2 - (int) (tract[i] * h / 2), (i + 1) * w / tlength, h / 2 - (int) (tract[i + 1] * h / 2));
				g.drawLine((i + 1) * w / tlength, h / 2 + (int) (tract[i] * h / 2), (i + 1) * w / tlength, h / 2 + (int) (tract[i + 1] * h / 2));
			}
		}
	}

	public void mousePressed(MouseEvent e)
	// adjust crossarea to where mouse clicked
	{
		Dimension d = size();
		int w = d.width;
		int h = d.height;
		int x = e.getX();
		int y = e.getY();
		tract[x * tlength / w] = (y > h / 2 ? y * 2.0 - h : h - 2.0 * y) / h;
		repaint(x - w / tlength - 1, 0, w * 2 / tlength + 1, h - 1);
//		parent.calcresponse();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e)
	//drag tubecrossareas with mouse
	{
		Dimension d = size();
		int w = d.width;
		int h = d.height;
		int x = e.getX();
		int y = e.getY();
		if (x < 0) x = 0;
		if (x > w) x = w - 1;
		if (y < 1) y = 1;
		if (y > h - 2) y = h - 2;
		tract[x * tlength / w] = (y > h / 2 ? y * 2.0 - h : h - 2.0 * y) / h;
		repaint(x - w / tlength - 1, 0, w * 2 / tlength + 1, h - 1);
//		parent.calcresponse();
	}

	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {

	}
}

class WaveDraw extends Canvas
// draws input and output pulse
{
	Container parent;
	int w;
	int h;
	int len;
	Dimension size;
	boolean trueSizeKnown = true;
	double[] buf;

	WaveDraw(Container par, int iniw, int inih, double[] buf, int len) {
		parent = par;
		this.len = len;
		this.buf = buf;
		w = iniw;
		h = inih;
		size = new Dimension(w, h);
		setBackground(Color.white);
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	public Dimension getMinimumSize() {
		return size;
	}

	public void paint(Graphics g) {
		//g.drawRect(0,0,w-1,h-1);
		// frame
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, w - 1, 0);
		g.drawLine(0, 0, 0, h - 1);
		g.setColor(Color.lightGray);
		g.drawLine(w - 1, h - 1, w - 1, 0);
		g.drawLine(w - 1, h - 1, 0, h - 1);
		g.setColor(Color.black);
		//end of frame
		for (int i = 0; i < len - 1; i++) {
			g.drawLine((i * w) / len, h / 2 - (int) (buf[i] * h / 2), ((i + 1) * w) / len, h / 2 - (int) (buf[i + 1] * h / 2));
		}
	}
}

class SPanel extends Panel
// Show Lip or Glottisimpedance
{
	Label value;

	SPanel(String myTitle) {
		setLayout(new BorderLayout());
		Label label = new Label(myTitle, Label.CENTER);
		add("Center", label);
		setBackground(Color.gray);
	}

	public void paint(Graphics g) {
		Dimension d = size();
		int w = d.width;
		int h = d.height;
		// frame
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, w - 1, 0);
		g.drawLine(0, 0, 0, h - 1);
		g.setColor(Color.white);
		g.drawLine(w - 1, h - 1, w - 1, 0);
		g.drawLine(w - 1, h - 1, 0, h - 1);
		//end of frame
	}

	public Insets insets() {
		return new Insets(5, 5, 5, 5);
	}
}

class CPanel extends Panel
// display all buttons
{
	CPanel(ActionListener par) {
        Label l = new Label("Presets:");
		add(l);
        //add presetbuttons
        Button b = new Button("AH");
		b.setActionCommand("AH");
		b.addActionListener(par);
		add(b);
		b = new Button("EH");
		b.setActionCommand("EH");
		b.addActionListener(par);
		add(b);
		b = new Button("EE");
		b.setActionCommand("EE");
		b.addActionListener(par);
		add(b);
		b = new Button("OH");
		b.setActionCommand("OH");
		b.addActionListener(par);
		add(b);
		b = new Button("OO");
		b.setActionCommand("OO");
		b.addActionListener(par);
		add(b);
		l = new Label("Sound:");
		add(l);
		b = new Button("Start");
		b.setActionCommand("start");
		b.addActionListener(par);
		add(b);
		b = new Button("Stop");
		b.setActionCommand("stop");
		b.addActionListener(par);
		add(b);
		setBackground(Color.gray);
	}

	public void paint(Graphics g) {
		Dimension d = size();
		int w = d.width;
		int h = d.height;
		// frame
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, w - 1, 0);
		g.drawLine(0, 0, 0, h - 1);
		g.setColor(Color.white);
		g.drawLine(w - 1, h - 1, w - 1, 0);
		g.drawLine(w - 1, h - 1, 0, h - 1);
		//end of frame
	}

	public Insets insets() {
		return new Insets(5, 5, 5, 5);
	}
}

class WPanel extends Panel
// frame for two wavediplayareas
{
	WaveDraw inw;
	WaveDraw outw;

	WPanel(double[] input, int lenin, double[] output, int lenout) {
		inw = new WaveDraw(this, 200, 64, input, lenin);
		outw = new WaveDraw(this, 200, 64, output, lenout);
        Label l = new Label("Input:");
		add(l);
		add(inw);
		l = new Label("Output:");
		add(l);
		add(outw);
		setBackground(Color.gray);
	}

	public void paint(Graphics g) {
		Dimension d = size();
		int w = d.width;
		int h = d.height;
		// frame
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, w - 1, 0);
		g.drawLine(0, 0, 0, h - 1);
		g.setColor(Color.white);
		g.drawLine(w - 1, h - 1, w - 1, 0);
		g.drawLine(w - 1, h - 1, 0, h - 1);
		//end of frame
	}

	public Insets insets() {
		return new Insets(5, 5, 5, 5);
	}
}

class VocSynthApplet extends Applet implements ActionListener {
//	AudioData sampleData;
//	ContinuousAudioDataStream sampleStream;
	byte[] sample;        //array for playing sound
	double[] inpwave;    //glottal wave
	double[] outwave;    //lip wave
	int length = 64;        //samples per
	double[] tract;        //cross areas of tubes
	int tlength = 8;        //no of tubes
	boolean playing = false;
	//TubeCanvas tubecan;
	WPanel wpan;
	CPanel pan;
	SPanel glotslider;
	SPanel lipslider;

	public void init() {
		sample = new byte[length];
		inpwave = new double[length];
		outwave = new double[length];
		tract = new double[tlength];
		//tract[0]=.5;tract[1]=.3;tract[2]=.2;tract[3]=.6;tract[4]=.9;tract[5]=.6;tract[6]=.2;tract[7]=.9;
		//sampleData = new AudioData(sample);
		//sampleStream = new ContinuousAudioDataStream(sampleData);
		setLayout(new BorderLayout(5, 5));
//		tubecan = new TubeCanvas(this, tract, tlength);
		wpan = new WPanel(inpwave, length, outwave, length);
		pan = new CPanel(this);
		glotslider = new SPanel("Zglo=0");
		lipslider = new SPanel("Zl=Inf.");
		add("North", wpan);
		add("South", pan);
		add("East", lipslider);
		add("West", glotslider);
//		add("Center", tubecan);
		setBackground(Color.lightGray);
	}

//	public void pcm2ulaw(double src[], byte dest[], int l)
//	// Routine for converting PCM into u-Law
//	// Written 1998 by Joel Niederhauser
//	//
//	// scr:  source PCM array          Values between -1..1
//	// dest: destination u-Law array   Values between -128..127
//	// l:    length of array
//	{
//	  /*
//		double value;								// Temporal Value
//		double factor=1.0/Math.log(255.0 +1.0);		// Multiplication Factor for speed
//		for (int i=0;i<l;i++)
//		{								// Calculation of u-Law value
//			value=factor*Math.log(1.0+255.0*Math.abs(src[i]));
//			if (src[i]<0.0) //signum
//			{
//				dest[i]=(byte) (128.0-12.0*value); 	// Assign calculated value to destination
//			}
//			else
//			{
//				dest[i]=(byte) (-128.0+12.0*value); 	// Assign calculated value to destination
//			}
//		}*/
//		// unfortunately this routine didn't quite work because of the signed byte format!
//		for (int i = 0; i < l; i++) {
//			dest[i] = Mulaw.linear2ulaw((short) (src[i] * 32760));
//		}
//	}

	public void generateglwave(boolean voiced) {    //Calulation of glottal pulse
		if (voiced) {
			for (int i = 0; i < length / 4; i++)            // Slope
			{
				inpwave[i] = -1.0 + i * 4.0 / length;
			}
			for (int i = length / 4; i < 3 * length / 4; i++)    // Half circle
			{
				inpwave[i] = 4.0 * Math.sqrt(length * length / 16.0 - (i - length / 2.0) * (i - length / 2.0)) / length;
			}
			for (int i = 3 * length / 4; i < length; i++)        // Closed Glottis
			{
				inpwave[i] = -1.0;
			}
			for (int i = 0; i < length - 1; i++)            // Smooth the waveform
			{
				inpwave[i] = (inpwave[i] + inpwave[i + 1]) * 0.95 / 2.0;
			}
			inpwave[length - 1] = (inpwave[length - 1] + inpwave[0]) * 0.95 / 2.0;
		} else {
			for (int i = 0; i < length - 1; i++) inpwave[i] = -1.0 + Math.random() * 2.0;
		}
	}

	public void calcresponse()    // DSP routine to calculate output of lattice Filter
	{
		double c = 1.0; //constant k=qc
		double[] r = new double[tlength + 1];    //reflections coefficients
		double[] li = new double[tlength + 1];    //to lips input to reflection --(z-1)--li----lo--
		double[] lo = new double[tlength + 1];    //to lips output of reflection          |refl|
		double[] gi = new double[tlength + 1];    //to glottis input to reflection ------go----gi--
		double[] go = new double[tlength + 1];    //to glottis output of reflection
		for (int i = 0; i <= tlength; i++) //clear all the values
		{
			r[i] = 0;
			li[i] = 0;
			lo[i] = 0;
			gi[i] = 0;
			go[i] = 0;
		}
		r[0] = 1.0; //Zgl=0
		for (int i = 1; i < tlength; i++) {    // r=(A2-A1)/(A2+A1);
			r[i] = (tract[i] * tract[i] - tract[i - 1] * tract[i - 1]) / (tract[i - 1] * tract[i - 1] + tract[i] * tract[i]);
		}
		r[tlength] = 1.0;//Zl=Inf.
		//Main loop
		for (int l = 0; l < 10; l++)
			for (int k = 0; k < length; k++) {
				//Input into system
				li[0] = inpwave[k] / 2.0;
				//Calculate all reflections
				for (int i = tlength; i >= 0; i--) {
					//to lips
					lo[i] = (1 + r[i]) * li[i] + r[i] * gi[i];
					//to glottis
					go[i] = (1 - r[i]) * gi[i] - r[i] * li[i];
					//To glottis without delay!
					if (i > 1) {
						gi[i - 1] = go[i];
					}
				}
				//calculate delays towards lips
                System.arraycopy(lo, 0, li, 1, tlength);
				//Lip output
				outwave[k] = lo[tlength];
			}
		// smooth start and end of array
        double dif = outwave[length - 1] - outwave[0];
		outwave[1] += dif * 0.2;
		outwave[length - 2] -= dif * 0.2;
		outwave[0] += dif * 0.4;
		outwave[length - 1] -= dif * 0.4;
		// Normalize to 0.9 amplitude to prevent clipping
        boolean seen = false;
        double best = 0;
        int bound = length;
        for (int i1 = 0; i1 < bound; i1++) {
            double abs = Math.abs(outwave[i1]);
            if (abs >= 0) {
                if (!seen || Double.compare(abs, best) > 0) {
                    seen = true;
                    best = abs;
                }
            }
        }
        double max = seen ? best : 0;
        for (int i = 0; i < length; i++) {
			outwave[i] = outwave[i] * 0.9 / max;
		}
		wpan.outw.repaint();            //Redraw Waveform
//		pcm2ulaw(outwave, sample, length);//Move to samplebuffer
	}

	public void preset(String p)// Preset Values for certain Vowels
	{
		if (p == "AH") {
			tract[7] = 0.56;
			tract[6] = 0.68;
			tract[5] = 0.68;
			tract[4] = 0.48;
			tract[3] = 0.32;
			tract[2] = 0.16;
			tract[1] = 0.36;
			tract[0] = 0.32;
		}
		if (p == "EE") {
			tract[7] = 0.44;
			tract[6] = 0.20;
			tract[5] = 0.16;
			tract[4] = 0.36;
			tract[3] = 0.64;
			tract[2] = 0.80;
			tract[1] = 0.72;
			tract[0] = 0.36;
		}
		if (p == "EH") {
			tract[7] = 0.44;
			tract[6] = 0.28;
			tract[5] = 0.40;
			tract[4] = 0.56;
			tract[3] = 0.68;
			tract[2] = 0.72;
			tract[1] = 0.52;
			tract[0] = 0.28;
		}
		if (p == "OH") {
			tract[7] = 0.16;
			tract[6] = 0.72;
			tract[5] = 0.48;
			tract[4] = 0.40;
			tract[3] = 0.24;
			tract[2] = 0.20;
			tract[1] = 0.40;
			tract[0] = 0.28;
		}
		if (p == "OO") {
			tract[7] = 0.12;
			tract[6] = 0.68;
			tract[5] = 0.48;
			tract[4] = 0.32;
			tract[3] = 0.28;
			tract[2] = 0.32;
			tract[1] = 0.60;
			tract[0] = 0.32;
		}
//		tubecan.repaint();
		calcresponse();
	}

	public void start()    // Start Applet
	{
//		if (sampleStream != null && !playing) {
//			AudioPlayer.player.start(sampleStream);
//			playing = true;
//		}
		generateglwave(true);
		preset("AH");
	}

//	public void stop()    // Stop Applet
//	{
//		if (sampleStream != null) {
//			AudioPlayer.player.stop(sampleStream);
//			playing = false;
//		}
//	}

	public void actionPerformed(ActionEvent e)    // Handle Buttons
	{
		String command = e.getActionCommand();
        //			if (sampleStream != null && !playing) {
        //				AudioPlayer.player.start(sampleStream);
        //			}
        //			if (sampleStream != null) {
        //				AudioPlayer.player.stop(sampleStream);
        //			}
        switch (command) {
            case "start" -> {
                playing = true;
                calcresponse();
            }
            case "stop" -> playing = false;
            case "Hello" -> {
                generateglwave(false);
                wpan.inw.repaint();
                preset("EH");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                generateglwave(true);
                wpan.inw.repaint();
                preset("EH");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                preset("OH");
            }
            default -> preset(command);
        }
	}

	public Insets insets() {
		return new Insets(5, 5, 5, 5);
	}
}

// end of vocsynth.java