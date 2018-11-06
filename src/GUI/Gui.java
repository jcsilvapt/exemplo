package GUI;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import Comunicar.Comunicar;

public class Gui extends Thread {

	private JFrame frame;
	private JTextField txtNomeRobot;
	private JTextField txtDistance;
	private JTextField txtAngle;
	private JTextField txtRadius;
	private JTextField txtOffsetLeft;
	private JTextField txtOffsetRight;
	private JTextArea txtrLogging;
	private JCheckBox chckbxCheckLogging;
	private JCheckBox chckbxVaguear;
	private JCheckBox chckbxEvitar;
	private JCheckBox chckbxGestor;
	private JButton btnConectar;
	private JLabel lblConectado;

	// Variaveis Globais
	private String name;
	private int offSetLeft, offSetRight, angle, distance, radius;

	// Caixas de correio
	private Comunicar inbox, gestor;

	// Check ligaçao robot
	boolean robotOn = false;

	// Paths Processos / Variaveis
	private final String[] GESTOR = new String[] { "Java", "-jar", ".\\src\\Gestor.jar" };
	private final String[] VAGUEAR = new String[] { "Java", "-jar", ".\\src\\Vaguear\\Vaguear.jar" };
	private final String[] EVITAR = new String[] { "Java", "-jar", ".\\src\\Evitar\\Evitar.jar" };

	private Process pGestor, pVaguear, pEvitar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Gui window = new Gui();
		window.frame.setVisible(true);
		window.start();
	}

	public void run() {
		for (;;) {
			String msg = inbox.receberMsg();
			decode(msg);
			// System.out.println("GUI [READ MSG]: " + msg);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Iniciar variaveis
	 */

	public void init() {
		this.inbox = new Comunicar("gui");
		this.gestor = new Comunicar("gestor");
		this.name = ""; // default - EV3
		this.offSetLeft = 0;
		this.offSetRight = 0;
		this.angle = 90;
		this.distance = 20;
		this.radius = 10;

		this.txtNomeRobot.setText(name);
		this.txtOffsetLeft.setText(String.valueOf(offSetLeft));
		this.txtOffsetRight.setText(String.valueOf(offSetRight));
		this.txtRadius.setText(String.valueOf(radius));
		this.txtAngle.setText(String.valueOf(angle));
		this.txtDistance.setText(String.valueOf(distance));

	}

	private void decode(String message) {
		String[] campos = message.split(";");
		switch (Byte.parseByte(campos[1])) {
		case Comunicar.OPEN:
			switch (Byte.parseByte(campos[2])) {
			case Comunicar.TRUE:
				robotStatus(true);
				break;
			case Comunicar.FALSE:
				robotStatus(false);
				break;
			default:
				break;
			}
			break;
		case Comunicar.CLOSE:
			robotStatus(false);
			break;
		default:
			break;
		}
	}

	private ProcessBuilder buildProcess(byte process) {
		String[] args = null;
		switch (process) {
		case Comunicar.GESTOR:
			args = GESTOR;
			break;
		case Comunicar.EVITAR:
			args = EVITAR;
			break;
		case Comunicar.VAGUEAR:
			args = VAGUEAR;
			break;
		default:
			return null;
		}

		ProcessBuilder processFinal = new ProcessBuilder(java.util.Arrays.asList(args));
		processFinal.inheritIO();
		processFinal.redirectErrorStream(true);

		return processFinal;
	}

	private void connectRobot() {
		if (name.equals("") || name == null || name.length() == 0) {
			txtNomeRobot.setBackground(Color.RED);
			logger("Nome do Robot vazio...");
		} else if (pGestor == null) {
			logger("Gestor está desligado...");
		} else {
			if (robotOn) {
				gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.CLOSE }, Comunicar.EMPTY);
			} else {
				gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.OPEN }, new String[] { name });
			}
		}
	}

	private void robotStatus(boolean value) {
		if (value) {
			btnConectar.setText("Desligar");
			lblConectado.setBackground(Color.green);
			txtNomeRobot.setBackground(Color.white);
			logger("Ligação ao Robot Concluída com sucesso!");
			robotOn = true;
		} else {
			btnConectar.setText("Ligar");
			lblConectado.setBackground(Color.red);
			logger("Ligação ao Robot desligada com sucesso!");
			robotOn = false;
		}
	}

	private void move(boolean backwards) {
		int dis = distance;
		if (robotOn) {
			if (backwards) {
				dis = dis * -1;
			}
			gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.MOVE }, new String[] { String.valueOf(dis) });
		}
	}

	private void turn(boolean right) {
		byte val = Comunicar.ESQ;
		if (robotOn) {
			if (right) {
				val = Comunicar.DRT;
			}
			gestor.enviarMsg(new byte[] { Comunicar.GUI, val },
					new String[] { String.valueOf(radius), ",", String.valueOf(angle) });
		}
	}

	private void stopMove() {
		if (robotOn) {
			gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.PARAR, Comunicar.TRUE }, Comunicar.EMPTY);
		}
	}

	/**
	 * Função que regista o texto no logger caso este esteja activo
	 * 
	 * @param text
	 */
	public void logger(String text) {
		if (txtrLogging.isEnabled()) {
			txtrLogging.append(text + "\n");
		}
	}

	public void clearLog() {
		if (!chckbxCheckLogging.isEnabled()) {
			txtrLogging.setEnabled(true);
			txtrLogging.setText("");
			txtrLogging.setEnabled(false);
		} else {
			txtrLogging.setText("");
		}
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
		init();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("..:FSO-TP1:..");
		frame.setResizable(false);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setBounds(100, 100, 573, 588);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);

		JPanel panelConeccao = new JPanel();
		panelConeccao.setBorder(
				new TitledBorder(null, "CONEC\u00C7\u00C3O", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
		panelConeccao.setBackground(Color.BLACK);
		panelConeccao.setBounds(10, 10, 548, 79);
		frame.getContentPane().add(panelConeccao);
		panelConeccao.setLayout(null);

		JLabel lblNomeRobot = new JLabel("Nome");
		lblNomeRobot.setHorizontalAlignment(SwingConstants.LEFT);
		lblNomeRobot.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNomeRobot.setForeground(Color.WHITE);
		lblNomeRobot.setBounds(10, 24, 47, 40);
		panelConeccao.add(lblNomeRobot);

		txtNomeRobot = new JTextField();
		txtNomeRobot.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				name = txtNomeRobot.getText();
				if (name.length() > 0) {
					txtNomeRobot.setBackground(Color.WHITE);
				}
			}
		});
		txtNomeRobot.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyChar() == KeyEvent.VK_ENTER) {
					name = txtNomeRobot.getText();
					if (name.length() > 0) {
						txtNomeRobot.setBackground(Color.WHITE);
					}
				}
			}
		});
		txtNomeRobot.setHorizontalAlignment(SwingConstants.CENTER);
		txtNomeRobot.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtNomeRobot.setBounds(67, 34, 226, 21);
		panelConeccao.add(txtNomeRobot);
		txtNomeRobot.setColumns(10);

		btnConectar = new JButton("Ligar");
		btnConectar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				connectRobot();
			}
		});
		btnConectar.setToolTipText("Ligar Gestor");
		btnConectar.setEnabled(true);
		btnConectar.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnConectar.setBounds(420, 34, 112, 19);
		panelConeccao.add(btnConectar);

		lblConectado = new JLabel("");
		lblConectado.setBackground(Color.RED);
		lblConectado.setOpaque(true);
		lblConectado.setForeground(Color.RED);
		lblConectado.setBounds(420, 55, 112, 3);
		panelConeccao.add(lblConectado);

		JPanel panelRobot = new JPanel();
		panelRobot.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "ROBOT",
				TitledBorder.LEFT, TitledBorder.TOP, null, new Color(255, 255, 255)));
		panelRobot.setBackground(Color.BLACK);
		panelRobot.setBounds(10, 99, 548, 214);
		frame.getContentPane().add(panelRobot);
		panelRobot.setLayout(null);

		JLabel lblDistancia = new JLabel("Dist\u00E2ncia");
		lblDistancia.setHorizontalAlignment(SwingConstants.LEFT);
		lblDistancia.setForeground(Color.WHITE);
		lblDistancia.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblDistancia.setBounds(10, 33, 60, 50);
		panelRobot.add(lblDistancia);

		JLabel lblAngulo = new JLabel("\u00C2ngulo");
		lblAngulo.setHorizontalAlignment(SwingConstants.LEFT);
		lblAngulo.setForeground(Color.WHITE);
		lblAngulo.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblAngulo.setBounds(10, 93, 60, 50);
		panelRobot.add(lblAngulo);

		JLabel lblRaio = new JLabel("Raio");
		lblRaio.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblRaio.setForeground(Color.WHITE);
		lblRaio.setBounds(10, 153, 60, 50);
		panelRobot.add(lblRaio);

		txtDistance = new JTextField();
		txtDistance.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					distance = Integer.parseInt(txtDistance.getText());
				}
			}
		});
		txtDistance.setHorizontalAlignment(SwingConstants.CENTER);
		txtDistance.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtDistance.setBounds(80, 49, 76, 19);
		panelRobot.add(txtDistance);
		txtDistance.setColumns(10);

		txtAngle = new JTextField();
		txtAngle.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					angle = Integer.parseInt(txtAngle.getText());
				}
			}
		});
		txtAngle.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtAngle.setHorizontalAlignment(SwingConstants.CENTER);
		txtAngle.setBounds(80, 111, 76, 19);
		panelRobot.add(txtAngle);
		txtAngle.setColumns(10);

		txtRadius = new JTextField();
		txtRadius.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					radius = Integer.parseInt(txtRadius.getText());
				}
			}
		});
		txtRadius.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtRadius.setHorizontalAlignment(SwingConstants.CENTER);
		txtRadius.setBounds(80, 171, 76, 19);
		panelRobot.add(txtRadius);
		txtRadius.setColumns(10);

		JButton btnAvancar = new JButton("Avan\u00E7ar");
		btnAvancar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				move(false);
			}
		});
		btnAvancar.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnAvancar.setBounds(272, 44, 76, 32);
		panelRobot.add(btnAvancar);

		JButton btnParar = new JButton("Parar");
		btnParar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				stopMove();
			}
		});
		btnParar.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnParar.setBounds(272, 104, 76, 32);
		panelRobot.add(btnParar);

		JButton btnRecuar = new JButton("Recuar");
		btnRecuar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				move(true);
			}
		});
		btnRecuar.setFont(new Font("Tahoma", Font.PLAIN, 10));

		btnRecuar.setBounds(272, 164, 76, 32);
		panelRobot.add(btnRecuar);

		JButton btnEsquerda = new JButton("Esquerda");
		btnEsquerda.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				turn(false);
			}
		});

		btnEsquerda.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnEsquerda.setBounds(173, 104, 76, 32);
		panelRobot.add(btnEsquerda);

		JButton btnDireita = new JButton("Direita");
		btnDireita.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				turn(true);
			}
		});

		btnDireita.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnDireita.setBounds(371, 104, 76, 32);
		panelRobot.add(btnDireita);

		JLabel lblOffsetEsq = new JLabel("Offset Esquerdo");
		lblOffsetEsq.setVerticalAlignment(SwingConstants.TOP);
		lblOffsetEsq.setForeground(Color.WHITE);
		lblOffsetEsq.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblOffsetEsq.setHorizontalAlignment(SwingConstants.CENTER);
		lblOffsetEsq.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		lblOffsetEsq.setBounds(155, 20, 113, 19);
		panelRobot.add(lblOffsetEsq);

		txtOffsetLeft = new JTextField();
		txtOffsetLeft.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					offSetLeft = Integer.parseInt(txtOffsetLeft.getText());
				}
			}
		});
		txtOffsetLeft.setHorizontalAlignment(SwingConstants.CENTER);
		txtOffsetLeft.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtOffsetLeft.setBounds(173, 49, 76, 19);
		panelRobot.add(txtOffsetLeft);
		txtOffsetLeft.setColumns(10);

		txtOffsetRight = new JTextField();
		txtOffsetRight.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					offSetRight = Integer.parseInt(txtOffsetRight.getText());
				}
			}
		});
		txtOffsetRight.setHorizontalAlignment(SwingConstants.CENTER);
		txtOffsetRight.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtOffsetRight.setBounds(371, 49, 76, 19);
		panelRobot.add(txtOffsetRight);
		txtOffsetRight.setColumns(10);

		JLabel lblOffsetDrt = new JLabel("Offset Direito");
		lblOffsetDrt.setForeground(Color.WHITE);
		lblOffsetDrt.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblOffsetDrt.setHorizontalAlignment(SwingConstants.CENTER);
		lblOffsetDrt.setVerticalAlignment(SwingConstants.TOP);
		lblOffsetDrt.setBounds(359, 20, 95, 19);
		panelRobot.add(lblOffsetDrt);

		chckbxVaguear = new JCheckBox("Vaguear");
		chckbxVaguear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxVaguear.isSelected() && pGestor != null) {
					try {
						pVaguear = buildProcess(Comunicar.VAGUEAR).start();
						logger("Vaguear Inicializado");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (pVaguear != null && pGestor != null) {
					gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.STOP, Comunicar.VAGUEAR }, Comunicar.EMPTY);
					logger("Vaguear desligado!");
					pVaguear.destroy();
				}
			}
		});
		chckbxVaguear.setEnabled(true);
		chckbxVaguear.setFont(new Font("Tahoma", Font.PLAIN, 15));
		chckbxVaguear.setForeground(Color.WHITE);
		chckbxVaguear.setBackground(Color.BLACK);
		chckbxVaguear.setBounds(461, 48, 81, 21);
		panelRobot.add(chckbxVaguear);

		chckbxEvitar = new JCheckBox("Evitar");
		chckbxEvitar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxEvitar.isSelected() && pGestor != null) {
					try {
						pEvitar = buildProcess(Comunicar.EVITAR).start();
						logger("Evitar Inicializado");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (pEvitar != null && pGestor != null) {
					gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.STOP, Comunicar.EVITAR }, Comunicar.EMPTY);
					logger("Evitar Desligado");
					pEvitar.destroy();
				}
			}
		});
		chckbxEvitar.setEnabled(true);
		chckbxEvitar.setForeground(Color.WHITE);
		chckbxEvitar.setFont(new Font("Tahoma", Font.PLAIN, 15));
		chckbxEvitar.setBackground(Color.BLACK);
		chckbxEvitar.setBounds(461, 108, 81, 21);
		panelRobot.add(chckbxEvitar);

		chckbxGestor = new JCheckBox("Gestor");
		chckbxGestor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxGestor.isSelected()) {
					try {
						pGestor = buildProcess(Comunicar.GESTOR).start();
						logger("Gestor Inicializado");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					if (pGestor != null) {
						gestor.enviarMsg(new byte[] { Comunicar.GUI, Comunicar.STOP, Comunicar.GESTOR },
								Comunicar.EMPTY);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						logger("Gestor desligado!");
						pGestor.destroy();
					}
				}

			}
		});
		chckbxGestor.setForeground(Color.WHITE);
		chckbxGestor.setBackground(Color.BLACK);
		chckbxGestor.setFont(new Font("Tahoma", Font.PLAIN, 15));
		chckbxGestor.setBounds(461, 168, 81, 21);
		panelRobot.add(chckbxGestor);

		JPanel panelLogging = new JPanel();
		panelLogging
				.setBorder(new TitledBorder(null, "LOGGING", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
		panelLogging.setBackground(Color.BLACK);
		panelLogging.setBounds(10, 323, 548, 228);
		frame.getContentPane().add(panelLogging);
		panelLogging.setLayout(null);

		chckbxCheckLogging = new JCheckBox("Ativar Logger");
		chckbxCheckLogging.setSelected(true);
		chckbxCheckLogging.setFont(new Font("Tahoma", Font.PLAIN, 15));
		chckbxCheckLogging.setForeground(Color.WHITE);
		chckbxCheckLogging.setBackground(Color.BLACK);
		chckbxCheckLogging.setBounds(6, 17, 157, 27);
		panelLogging.add(chckbxCheckLogging);

		JButton btnClearLogging = new JButton("Limpar Logger");
		btnClearLogging.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnClearLogging.setBounds(428, 22, 110, 21);
		panelLogging.add(btnClearLogging);

		JScrollPane spLogging = new JScrollPane();
		spLogging.setFont(new Font("Tahoma", Font.PLAIN, 15));
		spLogging.setBackground(Color.BLACK);
		spLogging.setBounds(10, 50, 528, 168);
		panelLogging.add(spLogging);

		txtrLogging = new JTextArea();
		txtrLogging.setEditable(false);
		txtrLogging.setLineWrap(true);
		spLogging.setViewportView(txtrLogging);
	}

}
