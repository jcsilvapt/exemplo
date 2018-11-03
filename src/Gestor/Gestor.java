package Gestor;

import java.util.ArrayList;
import java.util.List;

import Comunicar.Comunicar;
import myRobot.myRobot;

public class Gestor {

	// Variaveis
	private String msg;

	// Caixas de Correio
	@SuppressWarnings("unused")
	private Comunicar inbox, gui, vaguear, evitar;

	// Ligaçoes ao Robot
	private myRobot robot;
	private boolean avoidON = false;
	private List<String> commands = new ArrayList<String>();

	public Gestor() {
		System.out.println("Gestor Inicializado");
		inbox = new Comunicar("gestor");
		gui = new Comunicar("gui");
		vaguear = new Comunicar("vaguear");
		evitar = new Comunicar("evitar");
	}

	private void connectRobot(String name) {
		robot = new myRobot();
		boolean control = true;
		while (control) {
			if (robot.OpenEV3(name)) {
				gui.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.OPEN, Comunicar.TRUE }, Comunicar.EMPTY);
				control = false;
			} else {
				try {
					Thread.sleep(Comunicar.DEFAULTWAIT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void decode(String message) {
		String[] campos = message.split(";");

		int wait = Comunicar.DEFAULTWAIT;

		switch (Byte.parseByte(campos[0])) {
		case Comunicar.GUI:
		case Comunicar.VAGUEAR:
			switch (Byte.parseByte(campos[1])) {
			case Comunicar.MOVE:
				robot.Reta(Integer.parseInt(campos[2]));
				wait = delay(Integer.parseInt(campos[2]), false, 0);
				break;
			case Comunicar.ESQ:
				robot.CurvarEsquerda(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
				wait = delay(Integer.parseInt(campos[2]), true, Integer.parseInt(campos[3]));
				break;
			case Comunicar.DRT:
				robot.CurvarDireita(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
				wait = delay(Integer.parseInt(campos[2]), true, Integer.parseInt(campos[3]));
				break;
			case Comunicar.PARAR:
				switch (Byte.parseByte(campos[2])) {
				case Comunicar.TRUE:
					robot.Parar(true);
					break;
				default:
					robot.Parar(false);
					break;
				}
				break;
			case Comunicar.OPEN:
				System.out.println(campos[campos.length - 1]);
				connectRobot(campos[campos.length - 1]);
				break;
			case Comunicar.CLOSE:
				gui.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.CLOSE }, Comunicar.EMPTY);
				robot.CloseEV3();
				break;
			case Comunicar.VME:
				robot.AjustarVME(Integer.parseInt(campos[2]));
				break;
			case Comunicar.VMD:
				robot.AjustarVMD(Integer.parseInt(campos[2]));
				break;
			case Comunicar.SPD:
				robot.SetVelocidade(Integer.parseInt(campos[2]));
				break;
			}
			break;
		case Comunicar.EVITAR:
			switch (Byte.parseByte(campos[1])) {
			case Comunicar.MOVE:
				robot.Reta(Integer.parseInt(campos[2]));
				wait = delay(Integer.parseInt(campos[2]), false, 0);
				break;
			case Comunicar.ESQ:
				robot.CurvarEsquerda(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
				wait = delay(Integer.parseInt(campos[2]), true, Integer.parseInt(campos[3]));
				break;
			case Comunicar.PARAR:
				robot.Parar(true);
				break;
			case Comunicar.SENSOR:
				requestSensor();
				break;
			case Comunicar.AVOIDON:
				switch (Byte.parseByte(campos[2])) {
				case Comunicar.TRUE:
					avoidON = true;
					break;
				case Comunicar.FALSE:
					avoidON = false;
					break;
				}
			default:
				break;
			}
			break;
		default:
			break;
		}
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void requestSensor() {
		if (robot.SensorToque()) {
			evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.SENSOR, Comunicar.TRUE }, Comunicar.EMPTY);
		} else {
			evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.SENSOR, Comunicar.FALSE }, Comunicar.EMPTY);
		}
	}

	private int delay(int valor, boolean raio, int angulo) {
		// Robot demora 5 segundos a precorrer 100 cm
		int convCm = 100;
		int convMs = 5000;
		int delay = 0;
		int aux = valor;

		if (raio) {
			aux = (int) (2. * Math.PI * valor);
			aux = aux * angulo / 360;
		}

		delay = aux * convMs / convCm;
		System.out.println("Gestor [delay]: " + delay + "ms");
		return delay;
	}

	public void run() throws InterruptedException {
		for (;;) {
			int defaultWAIT;
			if (avoidON) {
				defaultWAIT = 1;
			} else {
				defaultWAIT = 250;
				msg = inbox.receberMsg();
				System.out.println("Gestor [MSG] : " + msg);
				decode(msg);
				Thread.sleep(defaultWAIT);
			}
		}
	}

	public static void main(String[] args) {
		Gestor gestor = new Gestor();
		try {
			gestor.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
