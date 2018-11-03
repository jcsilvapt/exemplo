package Vaguear;

import Comunicar.Comunicar;

public class Vaguear {

	// Caixas de Correio
	Comunicar inbox, gestor;
	
	private final int MAXDISTANCE 	= 250; // cm (será pouco :\?)
	private final int MAXANGLE		= 360; // Probably...
	
	
	public Vaguear() {
		System.out.println("Vaguear -  Classe Inicializada");
		inbox = new Comunicar("vaguear");
		gestor = new Comunicar("gestor");
		inbox.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.TRUE}, Comunicar.EMPTY);
	}
	
	private void decode(String message) {
		String[] campo = message.split(";");
		
		// TODO
	}
	
	private void randomMove() {
		int action = (int) (1 + Math.random()*3);
		int move;
		int radius;
		int angle;
	
		switch ((byte) action) {
		case Comunicar.MOVE:
			move = (int) (1 + Math.random()*MAXDISTANCE);
			if((int) 0 + Math.random()*1 == 1) {
				move = move * -1;
			}
			gestor.enviarMsg(new byte[] {Comunicar.VAGUEAR,  Comunicar.MOVE}, new String[] {String.valueOf(move)});
			break;
		case Comunicar.ESQ:
			radius = (int) (1 + Math.random()*MAXDISTANCE);
			angle = (int) (1 + Math.random()*MAXANGLE);
			gestor.enviarMsg(new byte[] {Comunicar.VAGUEAR,  Comunicar.ESQ}, new String[] {String.valueOf(radius), ",", String.valueOf(angle)});
			break;
		case Comunicar.DRT:
			radius = (int) (1 + Math.random()*MAXDISTANCE);
			angle = (int) (1 + Math.random()*MAXANGLE);
			gestor.enviarMsg(new byte[] {Comunicar.VAGUEAR,  Comunicar.DRT}, new String[] {String.valueOf(radius), ",", String.valueOf(angle)});
			break;		
		default:
			break;
		}
	}
	
	public void run() throws InterruptedException {
		System.out.println("Vaguear [inbox reading started...");
		for(;;) {
			String msg = inbox.receberMsg();
			decode(msg);
			randomMove();
			Thread.sleep(Comunicar.DEFAULTWAIT);
			inbox.enviarMsg(new byte[] {Comunicar.EVITAR, Comunicar.TRUE}, Comunicar.EMPTY);
		}
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
