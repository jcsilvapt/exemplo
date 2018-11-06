package Comunicar;

public interface iMensagem {
	/* Processos */
	public static byte GESTOR 	= 1;
	public static byte GUI 		= 2;
	public static byte VAGUEAR 	= 3;
	public static byte EVITAR 	= 4;
	
	/* Accoes  de Movimento*/
	public static byte MOVE 	= 1;
	public static byte ESQ	    = 2;
	public static byte DRT     	= 3;
	public static byte PARAR 	= 4;
	
	/* Accoes de Robot*/
	public static byte OPEN		= 5;
	public static byte CLOSE	= 6;
	public static byte VME 		= 7;
	public static byte VMD		= 8;
	public static byte SPD		= 9;
	
	/* Relacionado com Sensores */
	public static byte SENSOR	= 14;
	public static byte AVOIDON  = 11;	
	
	/* Desligar processos */
	public static byte STOP		= 12;
	
	public static byte FALSE 	= 0;
	public static byte TRUE 	= 1;
	
	public static int DEFAULTWAIT = 250;
	
	public final String[] EMPTY = new String[] {""};
	
	
	
	void enviarMsg(byte[] msg, String[] value);
	
	String receberMsg();
	
}
