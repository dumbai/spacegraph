package spacegraph.audio.midi;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.event.KeyEvent.*;

/** from https://github.com/Xyene/VirtualSynth */
public class MIDIPiano extends JPanel {
    private static final Font KEY_FONT = new Font("Monospace", Font.BOLD, 18);
    private static final int KEYS = 14;
    private static final int KEY_WIDTH = 35;
    private static final int KEY_HEIGHT = 140;
    private static final int INTERKEY_SPACE = 9;
    private static final Rectangle[] KEY_BOUNDS = new Rectangle[KEYS];
    private static final char[][] KEY_CHARS = {
            {'\u21C6', 'Q'},
            {'A', 'Z', '1'},
            {'S', 'X', '2'},
            {'D', 'C', '3'},
            {'F', 'V', '4'},
            {'G', 'B', '5'},
            {'H', 'N', '6'},
            {'J', 'M', '7'},
            {'K', ',', '8'},
            {'L', '.', '9'},
            {';', '/', '0'},
            {'"', '-'},
            {'=', '\u21b5'},
            {'\u2190'}
    };
    private static final int[][] KEY_BINDINGS = {
            {VK_TAB, VK_Q},
            {VK_A, VK_Z, VK_1, VK_NUMPAD1},
            {VK_S, VK_X, VK_2, VK_NUMPAD2},
            {VK_D, VK_C, VK_3, VK_NUMPAD3},
            {VK_F, VK_V, VK_4, VK_NUMPAD4},
            {VK_G, VK_B, VK_5, VK_NUMPAD5},
            {VK_H, VK_N, VK_6, VK_NUMPAD6},
            {VK_J, VK_M, VK_7, VK_NUMPAD7},
            {VK_K, VK_COMMA, VK_8, VK_NUMPAD8},
            {VK_L, VK_PERIOD, VK_9, VK_NUMPAD9},
            {VK_SEMICOLON, VK_SLASH, VK_0, VK_NUMPAD0},
            {VK_QUOTE, VK_MINUS},
            {VK_EQUALS, VK_ENTER},
            {VK_BACK_SPACE}
    };
    private static final int[] NOTE_MAP = {
            55, 57, 59, 60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77
    };


    static {
        for (int key = 0; key != KEYS; key++) {
            KEY_BOUNDS[key] = new Rectangle(4 + key * (KEY_WIDTH + INTERKEY_SPACE), 5, KEY_WIDTH, KEY_HEIGHT);
        }
    }

    public final MIDISynth synth;

    private final boolean[] selected = new boolean[KEYS];

    public MIDIPiano(MIDISynth synth) {
        this.synth = synth;
        setFocusable(true);
        setRequestFocusEnabled(true);
        addMouseListener(new MouseAdapter() {
            private int clicked;

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                for (int i = 0; i != KEY_BOUNDS.length; i++) {
                    if (!selected[i] && KEY_BOUNDS[i].contains(p)) {
                        selected[clicked = i] = true;
                        synth.press(note(e, i), force());
                        repaint();
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selected[clicked] = false;
                synth.release(note(e, clicked), force());
                repaint();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                for (int i = 0; i != KEY_BINDINGS.length; i++) {
                    for (int c : KEY_BINDINGS[i]) {
                        if (c == code && !selected[i]) {
                            selected[i] = true;
                            synth.press(note(e, i), force());
                            //repaint();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                for (int i = 0; i != KEYS; i++) {
                    for (int c = 0; c != KEY_BINDINGS[i].length; c++) {
                        if (code == KEY_BINDINGS[i][c]) {
                            selected[i] = false;
                            synth.release(note(e, i), force());
                            //repaint();
                        }
                    }
                }
            }
        });

        (instrumentControl = new JComboBox()).setFocusable(false);
        instrumentControl.addActionListener(new ChangeInstrumentAction());
        for (Instrument is : synth.soundbank) instrumentControl.addItem(is.getName());

        JPanel controls = new JPanel(new BorderLayout());
        {
            controls.add(group(new JLabel("Instruments: "), instrumentControl), BorderLayout.NORTH);
            controls.add(group(new JLabel("Volume:      "), volumeControl = new JSlider()), BorderLayout.CENTER);
            controls.add(group(new JLabel("Velocity:     "), forceControl = new JSlider()), BorderLayout.SOUTH);
            volumeControl.setMaximum(127);
            volumeControl.setValue(127);
            forceControl.setMaximum(127);
            forceControl.setValue(127);
            volumeControl.addChangeListener(e -> synth.channel.controlChange(7, volumeControl.getValue()));
            volumeControl.setFocusable(false);
            forceControl.setFocusable(false);
        }
        this.requestFocusInWindow();
        this.requestFocus();
        setLayout(new BorderLayout());
        add(controls, BorderLayout.SOUTH);
    }

    private int force() {
        return forceControl.getValue();
    }

    private static JPanel group(JComponent one, JComponent two) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.add(one, BorderLayout.WEST);
        holder.add(two, BorderLayout.CENTER);
        return holder;
    }

    private static int note(InputEvent key, int code) {
        int note = NOTE_MAP[code];
        if (key.isControlDown())
            note -= 24;
        if (key.isShiftDown())
            note += 24;
        if (key.isAltDown())
            note += 48;
        return note;
    }

    @Override
    public void paintComponent(Graphics _g) {
        super.paintComponent(_g);
        Graphics2D g = (Graphics2D) _g;
        g.translate(0, -10); // Make top part of keys invisible
        for (int key = 0; key != KEYS; key++) {
            Rectangle bounds = KEY_BOUNDS[key];

            if (selected[key]) {
                paintRoundRect(g, bounds, 6, Color.GRAY);
			} else {
                // Draw a nice shadow
                paintRoundRect(g, bounds, 7, Color.BLACK);
                paintRoundRect(g, bounds, 5, Color.GRAY);
			}
			paintRoundRect(g, bounds, 3, Color.LIGHT_GRAY);
			paintRoundRectFill(g, bounds, Color.WHITE);
			g.setColor(Color.BLACK);
            char[] binds = KEY_CHARS[key];
            for (int c = 0; c != binds.length; c++) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setFont(KEY_FONT);
                g.drawChars(binds, c, 1, bounds.x + KEY_WIDTH / 2 - g.getFontMetrics().charWidth(binds[c]) / 2,
                        bounds.y + (int) (KEY_HEIGHT * 0.6) + c * g.getFontMetrics().getHeight());
            }
        }
    }

    private static void paintRoundRect(Graphics2D g, Rectangle rect, int size, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(size));
        //g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 6, 6);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    private static void paintRoundRectFill(Graphics2D g, Rectangle rect, Color color) {
        g.setColor(color);
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 6, 6);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(KEYS * (KEY_WIDTH + INTERKEY_SPACE), KEY_HEIGHT);
    }

    private JComboBox instrumentControl;
    private JSlider volumeControl;
    private JSlider forceControl;

    class ChangeInstrumentAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            synth.synth.loadInstrument(synth.soundbank[instrumentControl.getSelectedIndex()]);
            synth.channel.programChange(instrumentControl.getSelectedIndex());
        }
    }

    public static MIDIPiano piano() throws MidiUnavailableException {
        MIDISynth ctrl = new MIDISynth();

        MIDIPiano piano = new MIDIPiano(ctrl);

        JFrame frame = new JFrame("Virtual MIDI Synthesizer");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(piano, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return piano;
    }

    public static void main(String[] args) throws MidiUnavailableException {
        piano();
    }


}