package Gestor;

import Comunicar.Comunicar;

public class Gestor {
	
	// Variaveis
	String msg;
	
	
	// Caixas de Correio
	Comunicar inbox, gui, vaguear, evitar;
	
	public Gestor() {
		System.out.println("Gestor Inicializado");
		inbox = new Comunicar("gestor");
		gui = new Comunicar("gui");
		vaguear = new Comunicar("vaguear");
		evitar = new Comunicar("evitar");
	}
	
	private void decode(String message) {
		// TODO
	}
	
	public void run() throws InterruptedException{
		for(;;) {
			msg = inbox.receberMsg();
			System.out.println("Gestor [MSG] : " + msg);
			Thread.sleep(250);
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
