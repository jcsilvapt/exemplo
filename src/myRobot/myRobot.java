package myRobot;

public class myRobot {
	private final String ROBOTNAME = "EV3";

	public myRobot() {
		System.out.println("myRobot - Classe Inicializada");
	}

	public boolean OpenEV3(String name) {
		if (name.equalsIgnoreCase(ROBOTNAME)) {
			System.out.println("My Robot # Open Comms**(" + name + ")");
			return true;
		} else {
			System.out.println("Nome Errado...");
		}
		return false;
	}

	public void CurvarDireita(int raio, int angulo) {
		System.out.println("A virar " + angulo + " graus num raio de " + raio + ".");
	}

	public void CurvarEsquerda(int raio, int angulo) {
		System.out.println("A virar " + angulo + " graus num raio de " + raio + ".");
	}

	public void Reta(int distancia) {
		if (distancia >= 0) {
			System.out.println("A andar " + distancia + "cm");
		} else {
			System.out.println("A recuar " + Math.abs(distancia) + "cm");
		}
	}

	public void Parar(boolean value) {
		if (value) {
			System.out.println("Parar!");
		} else {
			System.out.println("Parar assim que conseguir!");
		}
	}

	public void CloseEV3() {
		System.out.println("Desligar");
	}

	public void AjustarVME(int offset) {
		System.out.println("VME ajustado: " + offset);
	}

	public void AjustarVMD(int offset) {
		System.out.println("VMD ajustado: " + offset);
	}

	public void SetVelocidade(int percentagem) {
		System.out.println("Speed ajustada: " + percentagem);
	}

	public boolean SensorToque() {

		int bater = (int) Math.round(Math.random());
		System.out.println("Sensor: " + bater);
		if (bater == 1) {
			return true;
		}
		return false;
	}

}
