package Gestor;

import Comunicar.Comunicar;
import myRobot.myRobot;

public class Gestor {

	// Variaveis
	private String msg;
	private boolean toBreak = false;

	// Caixas de Correio
	private Comunicar inbox, gui, vaguear, evitar;

	// Ligaçoes ao Robot
	private myRobot robot;
	private boolean avoidON = false;

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

	private void decode(String message) throws InterruptedException {

		String[] campos = message.split(";");
		if (avoidON && Byte.parseByte(campos[0]) == Comunicar.EVITAR) {
			switch (Byte.parseByte(campos[1])) {
			case Comunicar.SENSOR:
				requestSensor();
				break;
			case Comunicar.PARAR:
				robot.Parar(true);
				break;
			case Comunicar.MOVE:
				robot.Reta(Integer.parseInt(campos[2]));
				break;
			case Comunicar.ESQ:
				robot.CurvarEsquerda(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
				break;
			case Comunicar.AVOIDON:
				switch (Byte.parseByte(campos[2])) {
				case Comunicar.TRUE:
					System.out.println("gestor avoidOn");
					avoidON = true;
					break;
				case Comunicar.FALSE:
					System.out.println("gestor avoidOff");
					avoidON = false;
					break;
				}
				break;
			}
		} else if(!avoidON){
			switch (Byte.parseByte(campos[0])) {
			case Comunicar.GUI:
			case Comunicar.VAGUEAR:
				switch (Byte.parseByte(campos[1])) {
				case Comunicar.MOVE:
					robot.Reta(Integer.parseInt(campos[2]));
					break;
				case Comunicar.ESQ:
					robot.CurvarEsquerda(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
					break;
				case Comunicar.DRT:
					robot.CurvarDireita(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
					break;
				case Comunicar.PARAR:
					switch (Byte.parseByte(campos[2])) {
					case Comunicar.TRUE:
						robot.Parar(true);
						break;
					case Comunicar.FALSE:
						robot.Parar(false);
						break;
					}
				case Comunicar.STOP:
					switch (Byte.parseByte(campos[2])) {
					case Comunicar.GESTOR:
						vaguear.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.STOP }, Comunicar.EMPTY);
						Thread.sleep(10);
						evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.STOP }, Comunicar.EMPTY);
						Thread.sleep(10);
						toBreak = true;
						break;
					case Comunicar.EVITAR:
						evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.STOP }, Comunicar.EMPTY);
						break;
					case Comunicar.VAGUEAR:
						vaguear.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.STOP }, Comunicar.EMPTY);
						break;
					default:
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
				case Comunicar.SENSOR:
					requestSensor();
					break;
				case Comunicar.PARAR:
					robot.Parar(true);
					break;
				case Comunicar.MOVE:
					robot.Reta(Integer.parseInt(campos[2]));
					break;
				case Comunicar.ESQ:
					robot.CurvarEsquerda(Integer.parseInt(campos[2]), Integer.parseInt(campos[3]));
					break;
				case Comunicar.AVOIDON:
					switch (Byte.parseByte(campos[2])) {
					case Comunicar.TRUE:
						System.out.println("gestor avoidOn");
						avoidON = true;
						break;
					case Comunicar.FALSE:
						System.out.println("gestor avoidOff");
						avoidON = false;
						break;
					}
					break;
				}
				break;
			}
		}
	}

	private void requestSensor() {
		if (avoidON) {
			evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.SENSOR, Comunicar.TRUE }, Comunicar.EMPTY);
		} else {
			if (robot.SensorToque()) {
				evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.SENSOR, Comunicar.TRUE }, Comunicar.EMPTY);
			} else {
				evitar.enviarMsg(new byte[] { Comunicar.GESTOR, Comunicar.SENSOR, Comunicar.FALSE }, Comunicar.EMPTY);
			}
		}
	}

	public void run() throws InterruptedException {
		for (;;) {
			msg = inbox.receberMsg();
			// System.out.println("Gestor [READ MSG] : " + msg);
			decode(msg);
			Thread.sleep(50);
			if (toBreak)
				break;
		}
		inbox.enviarMsg(new byte[] { Comunicar.GESTOR }, Comunicar.EMPTY);
		inbox.fecharCanal();
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
