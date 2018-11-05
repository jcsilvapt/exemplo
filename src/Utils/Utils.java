package Utils;

public class Utils {
	public static int delay(int valor, boolean raio, int angulo) {
		// Robot demora 5 segundos a precorrer 100 cm
		if(valor < 0) 
			valor = valor *-1;
		int convCm = 100;
		int convMs = 5000;
		int delay = 0;
		int aux = valor;

		if (raio) {
			aux = (int) (2. * Math.PI * valor);
			aux = aux * angulo / 360;
		}

		delay = aux * convMs / convCm;
		return delay;
	}
}
