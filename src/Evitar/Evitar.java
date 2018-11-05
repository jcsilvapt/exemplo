package Evitar;

import Comunicar.Comunicar;
import Utils.Utils;

public class Evitar {

	// Variaveis
	private int phase = 0;
	private boolean toBreak = false;
	int delay = Comunicar.DEFAULTWAIT;
	// Caixas de correio
	Comunicar inbox, gestor;

	// Default actions when avoid is ON
	private final String[] MOVEBACK = { "-15" };
	private final String[] TURN = { "1", ",", "90" };

	public Evitar() {
		System.out.println("Evitar - Inicializado");
		inbox = new Comunicar("evitar");
		gestor = new Comunicar("gestor");
	}

	private void decode(String message) {
		String[] campos = message.split(";");

		switch (Byte.parseByte(campos[1])) {
		case Comunicar.STOP:
//			gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.AVOIDON, Comunicar.FALSE }, Comunicar.EMPTY);
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			phase = 0;
			toBreak = true;
			break;
		case Comunicar.SENSOR:
			switch (Byte.parseByte(campos[2])) {
			case Comunicar.TRUE:
				avoid();
				break;
			case Comunicar.FALSE:
				if (phase != 0)
					avoid();
				break;
			}
			break;
		}
	}

	private void avoid() {
		boolean count = true;
		if (phase == 0) {
			gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.AVOIDON, Comunicar.TRUE }, Comunicar.EMPTY);
			delay = 10;
		} else if (phase == 1) {
			gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.PARAR }, Comunicar.EMPTY);
			delay = 10;
		} else if (phase == 2) {
			gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.MOVE }, MOVEBACK);
			delay = Utils.delay(15, false, 0);
		} else if (phase == 3) {
			gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.ESQ }, TURN);
			delay = Utils.delay(1, true, 90);
		} else if (phase == 4) {
			System.out.println("AVOID OFF PAH!");
			gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.AVOIDON, Comunicar.FALSE }, Comunicar.EMPTY);
			phase = 0;
			count = false;
			delay = Comunicar.DEFAULTWAIT;
		}
		if (count)
			phase += 1;
	}

	public void run() {
		for (;;) {
			try {
				gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.SENSOR }, Comunicar.EMPTY);
				String msg = inbox.receberMsg();
				//System.out.println("Evitar [READ MSG]: " + msg);
				decode(msg);
				Thread.sleep(delay);
				if(toBreak)
					break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		gestor.enviarMsg(new byte[] { Comunicar.EVITAR, Comunicar.AVOIDON, Comunicar.FALSE }, Comunicar.EMPTY);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		inbox.fecharCanal();
	}

	public static void main(String[] args) {
		Evitar ev = new Evitar();
		ev.run();
	}

}
