package Vaguear;

import Comunicar.Comunicar;
import Utils.Utils;

public class Vaguear {

	// Caixas de Correio
	Comunicar inbox, gestor;
	
	private final int MAXDISTANCE 	= 50; // cm (será pouco :\?)
	private final int MAXANGLE		= 120; // Probably...
	
	private boolean toBreak = false;
	
	public Vaguear() {
		System.out.println("Vaguear -  Classe Inicializada");
		inbox = new Comunicar("vaguear");
		gestor = new Comunicar("gestor");
		inbox.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.TRUE}, Comunicar.EMPTY);
	}
	
	private void decode(String message) {
		String[] campos = message.split(";");
		
		switch (Byte.parseByte(campos[1])) {
		case Comunicar.STOP:
			toBreak = true;
			break;

		default:
			break;
		}
	}
	
	private void randomMove() {
		int action = (int) (1 + Math.random()*3);
		int move;
		int radius;
		int angle;
		
		int delay = 0;
	
		switch ((byte) action) {
		case Comunicar.MOVE:
			move = (int) (1 + Math.random()*MAXDISTANCE);
			if((int) 0 + Math.random()*1 == 1) {
				move = move * -1;
			}
			gestor.enviarMsg(new byte[] {Comunicar.VAGUEAR,  Comunicar.MOVE}, new String[] {String.valueOf(move)});
			delay = Utils.delay(move, false, 0);
			break;
		case Comunicar.ESQ:
			radius = (int) (1 + Math.random()*MAXDISTANCE);
			angle = (int) (1 + Math.random()*MAXANGLE);
			gestor.enviarMsg(new byte[] {Comunicar.VAGUEAR,  Comunicar.ESQ}, new String[] {String.valueOf(radius), ",", String.valueOf(angle)});
			delay = Utils.delay(radius, true, angle);
			break;
		case Comunicar.DRT:
			radius = (int) (1 + Math.random()*MAXDISTANCE);
			angle = (int) (1 + Math.random()*MAXANGLE);
			gestor.enviarMsg(new byte[] {Comunicar.VAGUEAR,  Comunicar.DRT}, new String[] {String.valueOf(radius), ",", String.valueOf(angle)});
			delay = Utils.delay(radius, true, angle);
			break;		
		default:
			break;
		}
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws InterruptedException {
		System.out.println("Vaguear [inbox reading started...");
		for(;;) {
			String msg = inbox.receberMsg();
			decode(msg);
			randomMove();
			Thread.sleep(350);
			inbox.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.TRUE}, Comunicar.EMPTY);
			if(toBreak) 
				break;
		}
		inbox.fecharCanal();
		System.out.println("Vaguear [Disable] - Restart App");
	}
	
	
	public static void main(String[] args) {
		Vaguear vag = new Vaguear();
		try {
			vag.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
