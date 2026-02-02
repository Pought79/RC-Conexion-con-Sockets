package es.um.redes.nanoFiles.application;

import java.io.IOException;
import java.net.SocketException;

import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;

public class Directory {
	public static final double DEFAULT_CORRUPTION_PROBABILITY = 0.0;
	public static boolean dir_debug_0 = false;
	public static boolean dir_debug_1 = true; // Se ejecutará con esta opción por defecto
	public static boolean dir_debug_2 = false;

	public static void main(String[] args) {
		double datagramCorruptionProbability = DEFAULT_CORRUPTION_PROBABILITY;

		/**
		 * Command line argument to directory is optional, if not specified, default
		 * value is used: -loss: probability of corruption of received datagrams
		 */
		String arg;

		// Analizamos si hay parámetro
		if (args.length > 0 && args[0].startsWith("-")) {
			arg = args[0];
			// Examinamos si es un parámetro válido
			if (arg.equals("-loss")) {
				if (args.length == 2) {
					try {
						// El segundo argumento contiene la probabilidad de descarte
						datagramCorruptionProbability = Double.parseDouble(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("Wrong value passed to option " + arg);
						return;
					}
				} else if (args.length == 3) {
					if (args[2].startsWith("--debug-level=")) {
						if (args[2].equals("--debug-level=0")) {
							dir_debug_0 = true;
							dir_debug_1 = false;
							dir_debug_2 = false;
						} else if (args[2].equals("--debug-level=1")) {
							dir_debug_0 = false;
							dir_debug_1 = true;
							dir_debug_2 = false;

						} else if (args[2].equals("--debug-level=2")) {
							dir_debug_0 = false;
							dir_debug_1 = false;
							dir_debug_2 = true;

						} else {
							System.out
									.println("Usage: java -jar Directory.jar [-loss <0.0-1.0>] [--debug-level=[012]]");
							return;
						}
						try {
							// El segundo argumento contiene la probabilidad de descarte
							datagramCorruptionProbability = Double.parseDouble(args[1]);
						} catch (NumberFormatException e) {
							System.err.println("Wrong value passed to option " + arg);
							return;
						}

					}
				} else {
					System.err.println("option " + arg + " requires a value");
				}
			} else if (args.length == 1 && args[0].startsWith("--debug-level=")) {
					if (args[0].equals("--debug-level=0")) {
						dir_debug_0 = true;
						dir_debug_1 = false;
						dir_debug_2 = false;
					} else if (args[0].equals("--debug-level=1")) {
						dir_debug_0 = false;
						dir_debug_1 = true;
						dir_debug_2 = false;

					} else if (args[0].equals("--debug-level=2")) {
						dir_debug_0 = false;
						dir_debug_1 = false;
						dir_debug_2 = true;

					} else {
						System.out.println("Usage: java -jar Directory.jar [-loss <0.0-1.0>] [--debug-level=[012]]");
						return;
					}

			} else {
				System.err.println("Illegal option " + arg);
			}
		}
		System.out.println("Probability of corruption for received datagrams: " + datagramCorruptionProbability);
		try {
			NFDirectoryServer dir = new NFDirectoryServer(datagramCorruptionProbability);
			if (NanoFiles.testModeUDP) {
				dir.runTest();
			} else {
				dir.run();
			}
		} catch (SocketException e) {
			System.err.println("Directory cannot create UDP socket");
			System.err.println("Most likely a Directory process is already running and listening on that port...");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unexpected I/O error when running NFDirectoryServer.run");
			System.exit(-1);
		}

	}

}
