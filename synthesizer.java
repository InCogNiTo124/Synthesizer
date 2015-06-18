import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class synthesizer implements MouseListener, ChangeListener,
		ActionListener {
	private static byte SUSTAIN = 0;
	private static byte RECORDING = 0;
	private static short VOLUME = 100;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new synthesizer();
	}

	float screenWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	float screenHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	byte octave = 4;
	short h, w;
	ImageIcon favicon = new ImageIcon(getClass().getResource("/res/images/favicon.png"));
	ArrayList<JLabel> keyPressed = new ArrayList<JLabel>();
	ArrayList<Clip> tonePlayed = new ArrayList<Clip>();
	HashMap<String, Clip> clipCache = new HashMap<String, Clip>();
	HashMap<String, Icon> keyPCache = new HashMap<String, Icon>();
	HashMap<String, Icon> keyRCache = new HashMap<String, Icon>();
	AudioInputStream audio = null;
	Clip clip = null;
	FloatControl gainControl = null;
	String t = "";
	String toWrite = "";
	JFileChooser save = new JFileChooser("log.txt");
	JComponent[] xyz = new JComponent[10];
	String[] order = { "1", "2", "3", "1", "2", "2", "3" };
	String[] whiteKeyTitles = { "C", "D", "E", "F", "G", "A", "H" };
	String[] blackKeyTitles = { "C#", "D#", "F#", "G#", "A#" };
	String[] nazivi = { "sesnaestinka", "osminka", "cetvrtinka", "polovinka", "cijelaNota", "tocka" };
	String[] restplpa = { "rec", "stop", "play" };
	String[] bTipke = { "Q", "W", "E", "R", "T", "Z", "U", "Y", "X", "C", "V", "B", "N", "M", "COMMA" };
	String[] cTipke = { "2", "3", "5", "6", "7", "S", "D", "G", "H", "J" };
	JToggleButton[] note = new JToggleButton[5];
	JToggleButton tocka;
	JButton OK = new JButton("<html><b>OK</b></html>");
	JCheckBox akord = new JCheckBox("AKORD");
	JSlider bmp = new JSlider(JSlider.HORIZONTAL, 0, 180, 60);
	JSlider sVolume = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
	JLabel[] whiteKeys = new JLabel[15];
	JLabel[] blackKeys = new JLabel[10];
	JLabel[] ReStPlPa = new JLabel[3];
	JLabel status = new JLabel();
	JLabel sustain = new JLabel();
	JLabel t_bmp = new JLabel();
	JLabel rec = new JLabel();
	JLabel stop = new JLabel();
	JLabel play = new JLabel();
	JLabel pause = new JLabel();
	JLabel img_volume = new JLabel();
	JLabel t_volume = new JLabel();
	PressedAction pressedAction = new PressedAction();
	ReleasedAction releasedAction = new ReleasedAction();
	SustainSwitch sustainSwitch = new SustainSwitch();
	ImageIcon key;
	JFrame frame = new JFrame("Synthesizer by Marijan Smetko");
	ButtonGroup group = new ButtonGroup();
	JPanel keyboard = new JPanel();
	JPanel inputMode = new JPanel();
	//Play PLAY = null;

	synthesizer() {

		frame.add(inputMode);
		inputMode.setBounds(0, 0, 665, 70);
		inputMode.setSize(665, 70);
		inputMode.setLayout(null);
		short x = 15;
		short y = 15;
		for (int i = 0; i < nazivi.length; i += 1) {
			ImageIcon slika = new ImageIcon(getClass().getResource("/res/images/" + nazivi[i] + ".png"));
			if (i < 5) {
				h = (short) slika.getIconHeight();
				w = (short) slika.getIconWidth();
				note[i] = new JToggleButton(slika, true);
				note[i].setBounds(x, y, w + 5, h + 5);
				note[i].repaint();
				note[i].setName(String.valueOf(Math.pow(2, i - 2)));
				group.add(note[i]);
				xyz[i] = note[i];
				inputMode.add(note[i]);
				x += w + 10;
			} else { // ZNAÈI TOÈKA
				x += 10;
				h = (short) slika.getIconHeight();
				w = (short) slika.getIconWidth();
				tocka = new JToggleButton(slika);
				tocka.setName(String.valueOf(Math.pow(2, i - 2)));
				tocka.setBounds(x, y, w + 5, h + 5);
				tocka.repaint();
				xyz[i] = tocka;
				inputMode.add(tocka);
			}
		}
		// ZNAÈI DONJI RED INPUTA
		// ZNAÈI AKORD
		x = 15;
		y = 15 + 30 + 5;
		akord.setBounds(x, y, 60, 20);
		akord.repaint();
		xyz[6] = akord;
		inputMode.add(akord);

		// ZNAÈI OK
		x += 65;
		y -= 3;
		OK.setBounds(x, y, 50, 20);
		OK.repaint();
		OK.setVerticalAlignment(SwingConstants.CENTER);
		OK.setHorizontalAlignment(SwingConstants.CENTER);
		OK.addActionListener(this);
		xyz[7] = OK;
		inputMode.add(OK);

		// ZNAÈI SLIDER BMP
		x += 75;
		y += 3;
		bmp.setBounds(x, y, 180, 20);
		bmp.repaint();
		xyz[8] = bmp;
		bmp.setValue(60);
		bmp.setName("bmp");
		bmp.addChangeListener(this);
		inputMode.add(bmp);

		// ZNAÈI BMP
		x += 185;
		t_bmp.setBounds(x, y, 50, 20);
		t_bmp.setText(String.valueOf(bmp.getValue()) + " BMP");
		t_bmp.repaint();
		xyz[9] = t_bmp;
		inputMode.add(t_bmp);

		// ZNAÈI POSTAVLJANJE ELEMENATA !Enabled
		for (int i = 0; i < xyz.length; i += 1) {
			xyz[i].setEnabled(false);
		}

		// ZNAÈI ReStPlPa
		x += 60 + 10;
		for (int i = 0; i < ReStPlPa.length; i += 1) {
			key = new ImageIcon(getClass().getResource("/res/images/" + restplpa[i] + ".png"));
			h = (short) key.getIconHeight();
			w = (short) key.getIconWidth();
			ReStPlPa[i] = new JLabel(key);
			ReStPlPa[i].setBounds(x, y, w, h);
			ReStPlPa[i].setName("restplpa_" + restplpa[i]);
			ReStPlPa[i].repaint();
			ReStPlPa[i].addMouseListener(this);
			inputMode.add(ReStPlPa[i]);
			x += 25;
		}

		// ZNAÈI VOLUME
		x += 10;
		key = new ImageIcon(getClass().getResource("res/images/volumeIcon.png"));
		h = (short) key.getIconHeight();
		w = (short) key.getIconWidth();
		img_volume.setIcon(key);
		img_volume.setBounds(x, y, w, h);
		img_volume.repaint();
		inputMode.add(img_volume);

		x += w + 5;
		sVolume.setBounds(x, y, 100, 20);
		sVolume.repaint();
		sVolume.setName("volume");
		sVolume.addChangeListener(this);
		inputMode.add(sVolume);

		x += 100 + 5;
		t_volume.setBounds(x, y, 50, 20);
		t_volume.repaint();
		t_volume.setText(String.valueOf(sVolume.getValue()) + " %");
		inputMode.add(t_volume);

		// ZNAÈI ZA TIPKE
		frame.add(keyboard);
		keyboard.setBounds(0, 70, 665, 225);
		keyboard.setLayout(null);
		String img;
		y = 15 + 70;

		x = 15 - 83 + 30 + 1;
		h = 0;
		w = 0;
		octave = 4;

		// ZNAÈI CRNE TIPKE
		for (int i = 0; i < blackKeys.length; i += 1) {
			if (i == 5) {
				octave += 1;
			}

			if (i % 5 == 0 || i % 5 == 2) {
				x += 84;
			} else {
				x += 42;
			}
			key = new ImageIcon(getClass().getResource("/res/images/cTipka_00.png"));
			h = (short) key.getIconHeight();
			w = (short) key.getIconWidth();
			blackKeys[i] = new JLabel(key);
			blackKeys[i].setName(blackKeyTitles[i % 5] + String.valueOf(octave));
			blackKeys[i].setBounds(x, y, w, h);
			blackKeys[i].repaint();
			blackKeys[i].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(cTipke[i]), blackKeyTitles[i % 5] + String.valueOf(octave));
			blackKeys[i].getActionMap().put(blackKeyTitles[i % 5] + String.valueOf(octave), pressedAction);
			blackKeys[i].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released " + cTipke[i]), "r" + blackKeyTitles[i % 5] + String.valueOf(octave));
			blackKeys[i].getActionMap().put("r" + blackKeyTitles[i % 5] + String.valueOf(octave), releasedAction);
			blackKeys[i].addMouseListener(this);
			keyboard.add(blackKeys[i]);
		}

		octave = 4;
		x = 15;
		y = 15 + 70;
		w = 0;
		h = 0;

		// ZNAÈI BIJELE TIPKE
		for (int i = 0; i < whiteKeys.length; i += 1) {
			img = "";
			if (i == 7) {
				octave += 1;
			}
			if (i < 14) {
				img = "/res/images/bTipka_" + String.valueOf(order[i % 7]) + "0.png";
				key = new ImageIcon(getClass().getResource(img));
				h = (short) key.getIconHeight();
				w = (short) key.getIconWidth();
				whiteKeys[i] = new JLabel(key);
				whiteKeys[i].setName(whiteKeyTitles[i % 7] + String.valueOf(octave));
				whiteKeys[i].setBounds(x, y, w, h);
				whiteKeys[i].repaint();
				whiteKeys[i].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(bTipke[i]), String.valueOf(whiteKeyTitles[i % 7] + String.valueOf(octave)));
				whiteKeys[i].getActionMap().put(String.valueOf(whiteKeyTitles[i % 7] + String.valueOf(octave)), pressedAction);
				whiteKeys[i].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released " + bTipke[i]), "r" + whiteKeyTitles[i % 5] + String.valueOf(octave));
				whiteKeys[i].getActionMap().put("r" + whiteKeyTitles[i % 5] + String.valueOf(octave), releasedAction);
				whiteKeys[i].addMouseListener(this);
				keyboard.add(whiteKeys[i]);
				x += w;
			} else { // ZNAÈI ONA ZADNJA
				key = new ImageIcon(getClass().getResource("/res/images/bTipka_00.png"));
				h = (short) key.getIconHeight();
				w = (short) key.getIconWidth();
				whiteKeys[i] = new JLabel(key);
				whiteKeys[i].setName("C6");
				whiteKeys[i].setBounds(x, y, w, h);
				whiteKeys[i].repaint();
				whiteKeys[i].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(bTipke[i]), "C6");
				whiteKeys[i].getActionMap().put("C6", pressedAction);
				whiteKeys[i].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released " + bTipke[i]), "rC6");
				whiteKeys[i].getActionMap().put("rC6", releasedAction);
				whiteKeys[i].addMouseListener(this);
				keyboard.add(whiteKeys[i]);
			}
		}

		// ZNAÈI PEDALA
		ImageIcon sustainIcon = new ImageIcon(getClass().getResource("/res/images/pedala_00.png"));
		h = (short) sustainIcon.getIconHeight();
		w = (short) sustainIcon.getIconWidth();
		sustain.setName("sustain");
		sustain.setIcon(sustainIcon);
		sustain.setBounds((int) 252.50, 70 + 15 + 140 + 15, w, h);
		sustain.repaint();
		sustain.addMouseListener(this);
		sustain.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space");
		sustain.getActionMap().put("space", sustainSwitch);
		keyboard.add(sustain);

		// ZNAÈI STATUS
		status.setName("status");
		status.setBounds(665 - 80, 320 - 50, 65, 20);
		status.repaint();
		keyboard.add(status);

		frame.setBounds((int) (screenWidth / 2 - 332.5),(int) (screenHeight / 2 - 160), 665, 320);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(favicon.getImage());
		frame.setSize(320, 665);
		frame.setSize(665, 320);
		frame.setVisible(true);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		String name = e.getComponent().getName();
		Icon _img;
		_img = ((JLabel) e.getComponent()).getIcon();
		String img = String.valueOf(_img);

		if (name.equals("sustain")) {

			if (SUSTAIN == 0) {
				img = img.substring(img.length() - 24, img.length() - 5) + "x.png";
				SUSTAIN = 1;
			} else {
				img = img.substring(img.length() - 24, img.length() - 5)+ "0.png";
				SUSTAIN = 0;
			}
			_img = new ImageIcon((getClass().getResource(img)));
			((JLabel) e.getComponent()).setIcon(_img);

		} else if (name.contains("restplpa")) {

			if (name.contains("rec")) { // ZNAÈI RECORD

				RECORDING = 1;
				for (int i = 0; i < xyz.length; i += 1) {
					xyz[i].setEnabled(true);
				}
				status.setText("<html><font color=red>Recording...</font></html>");

			} else if (name.contains("stop")) { // ZNAÈI
																		// STOP

				RECORDING = 0;
				for (int i = 0; i < xyz.length; i += 1) {
					xyz[i].setEnabled(false);
				}

				if (!toWrite.equals("")) {
					save.addChoosableFileFilter(new FileNameExtensionFilter(
							"Native Format - MSS", "mss"));
					int response = save.showSaveDialog(frame);
					if (response == JFileChooser.APPROVE_OPTION) {
						try {
							File file = save.getSelectedFile();
							if (!file.exists()) {
								file.createNewFile();
							}
							FileWriter fw = new FileWriter(file.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(toWrite + "\n");
							bw.close();
							toWrite = "";
							status.setText("<html><font color=#007fff>Recorded!</font></html>");
						} catch (IOException e1) {
							status.setText("<html><font color=red>Error! :(</font></html>");
							e1.printStackTrace();
						}
					} else {
						status.setText("");
					}

				}
			} else if (name.contains("play")) { // ZNAÈI
																		// PLAY
				String string = "";
				img = img.substring(img.length() - 20, img.length() - 8) + "pause.png";
				_img = new ImageIcon((getClass().getResource(img)));
				((JLabel) e.getComponent()).setIcon(_img);
				((JLabel) e.getComponent()).setName("restplpa_pause");
				status.setText("<html><font color=green>Playing...</font></html>");

				save.addChoosableFileFilter(new FileNameExtensionFilter(
						"Native Format - MSS", "mss"));
				int response = save.showOpenDialog(frame);
				if (response == JFileChooser.APPROVE_OPTION) {
					try {
						File file = save.getSelectedFile();

						FileReader fr = new FileReader(file.getAbsoluteFile());
						BufferedReader br = new BufferedReader(fr);
						string = br.readLine();
						while (!string.equals("")) {
							if (string.contains(" ")) {
								String[] xD = string.split(" ");
								for (int i = 0; i < xD.length; i += 1) {
									float time = Float.parseFloat(xD[i].substring(0, 4));
									String f = xD[i].substring(4, xD[i].length());
									play(f);
									if (i == xD.length - 1) {
										try {
											Thread.sleep((long) (time * 1000));
										} catch (InterruptedException e1) {
											e1.printStackTrace();
										}
									}
								}
							} else {
								float time = Float.parseFloat(string.substring(0, 4));
								String f = string.substring(4, string.length());
								play( f);
								try {
									Thread.sleep((long) (time * 1000));
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							}
							string = br.readLine();
						}

						img = img.substring(img.length() - 20, img.length() - 9) + "play.png";
						_img = new ImageIcon((getClass().getResource(img)));
						((JLabel) e.getComponent()).setIcon(_img);
						((JLabel) e.getComponent()).setName("restplpa_play");
						status.setText("<html><font color=green>Play over!</font></html>");

					} catch (IOException e1) {
						status.setText("<html><font color=red>Error! :(</font></html>");
						e1.printStackTrace();
					}
				}
			} else if (name.contains("pause")) { // ZNAÈI PAUSE

				img = img.substring(img.length() - 20, img.length() - 9) + "play.png";
				_img = new ImageIcon((getClass().getResource(img)));
				((JLabel) e.getComponent()).setIcon(_img);
				((JLabel) e.getComponent()).setName("restplpa_play");
				status.setText("<html><font color=green>Paused!</font></html>");

			}

		} else {

			if (RECORDING % 2 == 1) {
				float BMP = bmp.getValue();
				float x = (60 / BMP) * Float.parseFloat(selected(group));
				if (tocka.isSelected()) {
					x *= 1.5;
				}
				
				if (akord.isSelected()) {
					t += String.format("%.2f", x).replaceAll(",", ".") + name + " ";
				} else {
					toWrite += String.format("%.2f", x).replaceAll(",", ".") + name + " ";
				}
			}
			if (keyPCache.containsKey(name)){
				_img = keyPCache.get(name);
			} else {
				img = img.substring(img.length() - 24, img.length() - 5) + "x.png";
				_img = new ImageIcon((getClass().getResource(img)));
				keyPCache.put(name, _img);
			}
			((JLabel) e.getComponent()).setIcon(_img);
			play(name);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		String name = e.getComponent().getName();
		Icon _img;
		_img = ((JLabel) e.getComponent()).getIcon();
		String img = String.valueOf(_img);

		if (name.equals("sustain")) {
			// ZNAÈI NIŠTA, SVE JE PODRŽANO U mousePressed()
		} else if (name.contains("restplpa")) {
			// ZNAÈI OPET NIŠTA, SVE JE U mousePressed()
			if (name.contains("rec")) { // ZNAÈI RECORD

			} else if (name.contains("stop")) { // ZNAÈI STOP

			} else if (name.contains("play")) { // ZNAÈI PLAY

			} else if (name.contains("pause")) { // ZNAÈI PAUSE
				
			}

		} else {
			if (!akord.isSelected()) {
				if (keyRCache.containsKey(name)){
					_img = keyRCache.get(name);
				} else {
					img = img.substring(img.length() - 24, img.length() - 5) + "0.png";
					_img = new ImageIcon((getClass().getResource(img)));
					keyRCache.put(name, _img);
				}
				((JLabel)e.getComponent()).setIcon(_img);
			} else {
				keyPressed.add((JLabel) e.getComponent());
			}
			
			if (SUSTAIN == 0) {
				for(Clip clip: tonePlayed) {
					clip.stop();
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// ZNAÈI NE TREBA MI
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// ZNAÈI NE TREBA MI
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// ZNAÈI NE TREBA MI
	}

	public void play(String file) {
		clip = null;
		audio = null;
		try {
			if (clipCache.containsKey(file)) {
				clip = clipCache.get(file);
				clip.setFramePosition(0);
			} else {
				audio = AudioSystem.getAudioInputStream(getClass().getResource("/res/sounds/" + file + ".wav"));
				clip = AudioSystem.getClip();
				clip.open(audio);
				audio.close();
				clipCache.put(file, clip);
			}
			gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			double gain = (double) 2 * (VOLUME / (float) 100);
			float dB = (float) (Math.log(gain) / Math.log(10) * 20);
			gainControl.setValue(dB);
			
			clip.stop();
			tonePlayed.add(clip);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		if (((Component) e.getSource()).getName().equals("bmp")) {
			t_bmp.setText(String.valueOf(bmp.getValue()) + " BMP");
		} else {
			VOLUME = (short) sVolume.getValue();
			t_volume.setText(String.valueOf(VOLUME + " %"));
		}
	}

	public String selected(ButtonGroup grupa) {
		Enumeration<AbstractButton> buttons = grupa.getElements();
		while (buttons.hasMoreElements()) {
			JToggleButton temp = (JToggleButton) buttons.nextElement();
			if (temp.isSelected()) {
				return temp.getName();
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String name = "";
		String img = "";
		Icon _img = null;
		akord.setSelected(false);
		if (SUSTAIN == 1) {
			t = "+ " + t.replaceAll("+ ", "");
		}
		toWrite += t + "\n";
		t = "";
		for (JLabel label : keyPressed) {
			name = label.getName();
			if (keyRCache.containsKey(name)){
				_img = keyRCache.get(name);
			} else {
				_img = label.getIcon();
				img = String.valueOf(_img);
				img = img.substring(img.length() - 24, img.length() - 5) + "0.png";
				_img = new ImageIcon((getClass().getResource(img)));
				keyRCache.put(name, _img);
			}
			label.setIcon(_img);
		}
		keyPressed.clear();
	}

	class PressedAction extends AbstractAction {
		private static final long serialVersionUID = -5953687751175492779L;

		Icon _img = null;
		String img = "";
		String name = "";
		float BMP = 0;
		float x = 0;
		JLabel label = new JLabel();

		@Override
		public void actionPerformed(ActionEvent e) {
			label = (JLabel) (e.getSource());
			name = label.getName();
			if (keyPCache.containsKey(name)){
				_img = keyPCache.get(name);
			} else {
				_img = label.getIcon();
				img = String.valueOf(_img);
				img = img.substring(img.length() - 24, img.length() - 5) + "x.png";
				_img = new ImageIcon((getClass().getResource(img)));
				keyPCache.put(name, _img);
			}
			label.setIcon(_img);

			play(name);

			if (RECORDING == 1) {
				BMP = (float) bmp.getValue();
				x = (60 / BMP) * Float.parseFloat(selected(group));
				if (tocka.isSelected()) {
					x *= 1.5;
				}

				if (akord.isSelected()) {
					t += String.format("%.2f", x).replaceAll(",", ".") + name + " ";
				} else {
					toWrite += String.format("%.2f", x).replaceAll(",", ".") + name + "\n";
				}
			}
		}

	}

	class ReleasedAction extends AbstractAction {
		private static final long serialVersionUID = 6575619429483056612L;

		Icon _img = null;
		String img = "";
		JLabel label = new JLabel();
		String name = "";
		@Override
		public void actionPerformed(ActionEvent e) {
			label = (JLabel) e.getSource();
			name = label.getName();
			if (!akord.isSelected()) {
				if (keyRCache.containsKey(name)){
					_img = keyRCache.get(name);
				} else {
					_img = label.getIcon();
					img = String.valueOf(_img);
					img = img.substring(img.length() - 24, img.length() - 5) + "0.png";
					_img = new ImageIcon((getClass().getResource(img)));
					keyRCache.put(name, _img);
				}
				label.setIcon(_img);
			} else {
				keyPressed.add(label);
			}
			
			if (SUSTAIN == 0) {	
				for(Clip clip: tonePlayed) {
					clip.stop();
					clip.setFramePosition(0);
				}
			}

		}
	}

	class SustainSwitch extends AbstractAction {
		private static final long serialVersionUID = -4203084820416890852L;
		String img = "";
		Icon _img = null;

		@Override
		public void actionPerformed(ActionEvent e) {
			_img = ((JLabel) e.getSource()).getIcon();
			img = String.valueOf(_img);
			if (SUSTAIN == 0) {
				img = img.substring(img.length() - 24, img.length() - 5) + "x.png";
				SUSTAIN = 1;
			} else {
				img = img.substring(img.length() - 24, img.length() - 5) + "0.png";
				SUSTAIN = 0;
			}
			_img = new ImageIcon((getClass().getResource(img)));
			((JLabel) e.getSource()).setIcon(_img);
		}
	}
}

/*
 * DEVELOPED BY InCogNiTo, AKA Marijan Smetko COPYRIGHT: InCogNiTo
 */