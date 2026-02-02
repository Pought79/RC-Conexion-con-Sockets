package es.um.redes.nanoFiles.application;

import es.um.redes.nanoFiles.logic.NFController;
import es.um.redes.nanoFiles.util.FileDatabase;

public class NanoFiles {

	public static final String DEFAULT_SHARED_DIRNAME = "nf-shared";
	/**
	 * Identificador único para cada grupo de prácticas. TODO: Establecer a un valor
	 * que combine los DNIs de ambos miembros del grupo de prácticas.
	 */
	public static final String PROTOCOL_ID = "123456789A";
	private static final String DEFAULT_DIRECTORY_HOSTNAME = "localhost";
	public static String sharedDirname = DEFAULT_SHARED_DIRNAME;
	public static FileDatabase db;
	public static boolean nano_debug_0 = false;
	public static boolean nano_debug_1 = true; // Se ejecutará con esta opción por defecto
	public static boolean nano_debug_2 = false;
	/**
	 * Flag para pruebas iniciales con UDP, desactivado una vez que la comunicación
	 * cliente-directorio está implementada y probada.
	 */
	public static boolean testModeUDP = false; // Desactivamos
	/**
	 * Flag para pruebas iniciales con TCP, desactivado una vez que la comunicación
	 * cliente-servidor de ficheros está implementada y probada.
	 */
	public static boolean testModeTCP = false; // Desactivamos

	public static void main(String[] args) {
		// Comprobamos los argumentos
		if (args.length > 2) {
			System.out.println("Usage: java -jar NanoFiles.jar [--debug-level=[012] [<local_shared_directory>]");
			return;
		} else if (args.length == 1 && !args[0].startsWith("--debug-level=")) {
			// Establecemos el directorio compartido especificado
			sharedDirname = args[0];
		} else if (args.length == 1 && args[0].startsWith("--debug-level=")) {
			if (args[0].equals("--debug-level=0")) {
				nano_debug_0 = true;
				nano_debug_1 = false;
				nano_debug_2 = false;
			} else if (args[0].equals("--debug-level=1")) {
				nano_debug_0 = false;
				nano_debug_1 = true;
				nano_debug_2 = false;

			} else if (args[0].equals("--debug-level=2")) {
				nano_debug_0 = false;
				nano_debug_1 = false;
				nano_debug_2 = true;

			} else {
				System.out.println("Usage: java -jar NanoFiles.jar [--debug-level=[012]] [<local_shared_directory>]");
				return;
			}
			sharedDirname = DEFAULT_SHARED_DIRNAME;
			System.out.println("* No directory specified - using default shared directory: " + DEFAULT_SHARED_DIRNAME);
		} else if (args.length == 2 && args[0].startsWith("--debug-level=")) {
			sharedDirname = args[1];
			if (args[0].equals("--debug-level=0")) {
				nano_debug_0 = true;
				nano_debug_1 = false;
				nano_debug_2 = false;
			} else if (args[0].equals("--debug-level=1")) {
				nano_debug_0 = false;
				nano_debug_1 = true;
				nano_debug_2 = false;

			} else if (args[0].equals("--debug-level=2")) {
				nano_debug_0 = false;
				nano_debug_1 = false;
				nano_debug_2 = true;

			} else {
				System.out.println("Usage: java -jar NanoFiles.jar [--debug-level=[012]] [<local_shared_directory>]");
				return;
			}
		} else if (args.length == 0) {
			sharedDirname = DEFAULT_SHARED_DIRNAME;
			System.out.println("* No directory specified - using default shared directory: " + DEFAULT_SHARED_DIRNAME);
		} else {
			System.out.println("Usage: java -jar NanoFiles.jar [--debug-level=[012]] [<local_shared_directory>]");
			return;
		}

		db = new FileDatabase(sharedDirname);

		// Creamos el controlador que aceptará y procesará los comandos
		NFController controller = new NFController(DEFAULT_DIRECTORY_HOSTNAME);

		if (testModeUDP) {
			controller.testCommunication();
		} else {
			// Entramos en el bucle para pedirle al controlador que procese comandos del
			// shell hasta que el usuario quiera salir de la aplicación.
			do {
				controller.readGeneralCommandFromShell();
				controller.processCommand();
			} while (controller.shouldQuit() == false);
			System.out.println("Bye.");
		}
	}
}
