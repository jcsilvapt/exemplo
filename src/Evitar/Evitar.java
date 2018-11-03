package Evitar;

import Comunicar.Comunicar;

public class Evitar {
	
	// Variaveis
	private boolean avoidON = false;
	
	// Caixas de correio
	Comunicar inbox, gestor;
	
	// Default actions when avoid is ON
	private final String[] MOVEBACK = {"-15"};
	private final String[] TURN		= {"0",",","90"};

	
	public Evitar() {
		System.out.println("Evitar - Inicializado");
		inbox = new Comunicar("evitar");
		gestor = new Comunicar("gestor");
	}
	
	private void decode(String message) {
		String[] campos = message.split(";");
		
		switch (Byte.parseByte(campos[1])) {
		case Comunicar.SENSOR:
			switch (Byte.parseByte(campos[2])) {
			case Comunicar.TRUE:
				avoid();
				break;
			case Comunicar.FALSE:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	private void avoid() {
		try {
			gestor.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.AVOIDON, Comunicar.TRUE}, Comunicar.EMPTY);
			avoidON = true;
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		gestor.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.PARAR}, Comunicar.EMPTY);
		gestor.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.MOVE}, MOVEBACK);
		gestor.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.ESQ}, TURN);
		avoidON = false;
	}
	
	public void run() {
		for(;;) {
			try {
				if(!avoidON)
					gestor.enviarMsg(new byte[] {Comunicar.EVITAR,  Comunicar.SENSOR}, Comunicar.EMPTY);
				String msg = inbox.receberMsg();
				System.out.println("Evitar [MSG]: " + msg);
				decode(msg);
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Evitar ev = new Evitar();
		ev.run();
	}

}
