package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

import es.um.redes.nanoFiles.application.Directory;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;

	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	// Almacena los ficheros disponibles por su nombre
	private HashMap<String, FileInfo> ficherosDisponibles = new HashMap<>();
	// Almacena los ficheros junto con la lista de servidores a la que pertenecen
	private HashMap<InetSocketAddress, FileInfo[]> ficherosEnServidor = new HashMap<>();

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {

		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */

		socket = new DatagramSocket(DIRECTORY_PORT);

		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */

		ficherosDisponibles = new HashMap<>();
		ficherosEnServidor = new HashMap<>();
		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		byte[] bufferRecepcion;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			bufferRecepcion = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);

			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */

			socket.receive(datagramReceivedFromClient);
			// Hemos comentado este fragmento de código porque sino nos sale un warning de
			// "dead code"
			/*
			 * if (datagramReceivedFromClient == null) { System.err.
			 * println("[testMode] NFDirectoryServer.receiveDatagram: code not yet fully functional.\n"
			 * + "Check that all TODOs have been correctly addressed!"); System.exit(-1); }
			 * else {
			 */
			// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
			double rand = Math.random();
			if (rand < messageDiscardProbability) {
				System.err.println("Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
			} else {
				datagramReceived = true;
				System.out.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
						+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
			}
			// }

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */
		byte[] bytes = new byte[pkt.getLength()];
		System.arraycopy(pkt.getData(), 0, bytes, 0, pkt.getLength());
		String cadena_recibida = new String(bytes);
		System.out.println("La cadena del cliente es: \n" + cadena_recibida);
		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */

		String cadena_ping = "ping";
		if (cadena_ping.equals(cadena_recibida)) {
			String cadena_acierto = "pingok";
			byte[] respuesta = cadena_acierto.getBytes();
			DatagramPacket datos_respuesta = new DatagramPacket(respuesta, respuesta.length, pkt.getSocketAddress());
			socket.send(datos_respuesta);
		} else if (cadena_recibida.startsWith("ping&")) {

			if (cadena_recibida.equals("ping&" + NanoFiles.PROTOCOL_ID)) {
				String cadena_bienvenido = "welcome";
				byte[] respuesta = cadena_bienvenido.getBytes();
				DatagramPacket datos_respuesta = new DatagramPacket(respuesta, respuesta.length,
						pkt.getSocketAddress());
				socket.send(datos_respuesta);
			} else {
				String cadena_denegado = "denied";
				byte[] respuesta = cadena_denegado.getBytes();
				DatagramPacket datos_respuesta = new DatagramPacket(respuesta, respuesta.length,
						pkt.getSocketAddress());
				socket.send(datos_respuesta);
			}
		} else {
			String cadena_fallo = "invalid";
			byte[] respuesta = cadena_fallo.getBytes();
			DatagramPacket datos_fallo = new DatagramPacket(respuesta, respuesta.length, pkt.getSocketAddress());
			socket.send(datos_fallo);
			System.out.println("La cadena pasada como parámetro no es igual a la cadena 'ping'. ");
		}

		/*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */

		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);

	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		String recibido = new String(pkt.getData(), 0, pkt.getLength());
		if (!Directory.dir_debug_0)
			System.out.print("* [RECEIVED] Message content: " + recibido);
		DirMessage mensaje = DirMessage.fromString(recibido);

		String operation = mensaje.getOperation();
		DirMessage msgToSend = null;

		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			if (Directory.dir_debug_2)
				System.out.println("* [PING] Processing ping message...");

			if (mensaje.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_SUCCESS);
				msgToSend.setProtocolID(NanoFiles.PROTOCOL_ID);
				if (Directory.dir_debug_2)
					System.out.println("* [PING] PING message processed successfully.");
			} else {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_FAIL);
			}

			break;
		}
		case DirMessageOps.OPERATION_GETFILELIST: {
			if (Directory.dir_debug_2)
				System.out.println("* [FILELIST] Processing GETFILELIST message...");

			msgToSend = new DirMessage(DirMessageOps.OPERATION_FILELIST);
			StringBuffer nombres = new StringBuffer();
			StringBuffer tamanos = new StringBuffer();
			StringBuffer hashes = new StringBuffer();
			StringBuffer servidores = new StringBuffer();

			boolean primeraVez = true;

			for (String hash : ficherosDisponibles.keySet()) {
				FileInfo fileInfo = ficherosDisponibles.get(hash);

				// Añadir separadores si no es la primera vez
				if (!primeraVez) {
					nombres.append(", ");
					tamanos.append(", ");
					hashes.append(", ");
					servidores.append(", ");
				}

				// Añadir datos del archivo
				nombres.append(fileInfo.fileName);
				tamanos.append(fileInfo.fileSize);
				hashes.append(hash);

				// Buscar servidores para este archivo
				servidores.append("{");
				boolean primeraVezServidor = true;

				for (InetSocketAddress servidor : ficherosEnServidor.keySet()) {
					FileInfo[] ficheros = ficherosEnServidor.get(servidor);
					String[] hashesEnServidor = new String[ficheros.length];
					for (int i = 0; i < ficheros.length; i++)
						hashesEnServidor[i] = ficheros[i].fileHash;

					for (String hashServidor : hashesEnServidor) {
						if (hashServidor.equals(hash)) {
							if (!primeraVezServidor) {
								servidores.append(", ");
							}
							servidores.append(servidor);
							primeraVezServidor = false;
							break;
						}
					}
				}
				servidores.append("}");

				primeraVez = false;
			}
			msgToSend.setFileNames(nombres);
			msgToSend.setFileHashes(hashes);
			msgToSend.setFileSizes(tamanos);
			msgToSend.setServers(servidores);

			if (Directory.dir_debug_2)
				System.out.println("* [FILELIST] FILELIST message processed successfully.");
			break;
		}
		case DirMessageOps.OPERATION_SERVE: {
			if (Directory.dir_debug_2)
				System.out.println("* [SERVE] Processing SERVE message...");

			InetSocketAddress clientAddress = (InetSocketAddress) pkt.getSocketAddress();
			int port = mensaje.getPort();
			InetSocketAddress serverAddress = new InetSocketAddress(clientAddress.getAddress(), port);
			if (mensaje.getFileNames().isEmpty()) {
				if (Directory.dir_debug_2)
					System.err.println("* [WARNING] Directory has no files.");
			} else {
				String[] nombres = mensaje.getFileNames().toString().split(", ");
				String[] tamanos = mensaje.getFileSizes().toString().split(", ");
				String[] hashes = mensaje.getFileHashes().toString().split(", ");
				FileInfo[] ficheros = new FileInfo[nombres.length];
				for (int i = 0; i < nombres.length; i++) {
					ficheros[i] = new FileInfo(hashes[i], nombres[i], Long.parseLong(tamanos[i]), null);
					ficherosDisponibles.put(hashes[i], ficheros[i]);
				}
				ficherosEnServidor.put(serverAddress, ficheros);
				if (Directory.dir_debug_2)
					System.out.println("* [SERVE] SERVE message processed successfully.");
			}
			msgToSend = new DirMessage(DirMessageOps.OPERATION_SERVE_SUCCESS);
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD_REQUEST: {
			if (Directory.dir_debug_2)
				System.out.println("* [DOWNLOAD] Processing DOWNLOAD message...");

			String subcadena = mensaje.getFileNameSubstring();
			int numArchivosCoincidentes = 0;
			FileInfo ficheroABuscar = null;
			StringBuffer servidores = new StringBuffer();
			for (FileInfo fichero : ficherosDisponibles.values()) {
				if (fichero.fileName.contains(subcadena)) {
					ficheroABuscar = fichero;
					numArchivosCoincidentes++;
				}
			}
			msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_FAIL);
			if (numArchivosCoincidentes > 1) {
				if (Directory.dir_debug_2)
					System.err
							.println("* [WARNING] Ambiguous files found, please enter command with another substring:");
			} else if (numArchivosCoincidentes == 0) {
				if (Directory.dir_debug_2)
					System.err.println(

							"* [WARNING] No files match this substring, please enter command with another substring:");
			} else if (numArchivosCoincidentes == 1) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD);
				String hashABuscar = ficheroABuscar.fileHash;
				boolean primeraVez = true;
				for (InetSocketAddress servidor : ficherosEnServidor.keySet()) {
					FileInfo[] ficheros = ficherosEnServidor.get(servidor);
					String[] hashes = new String[ficheros.length];
					for (int i = 0; i < ficheros.length; i++)
						hashes[i] = ficheros[i].fileHash;
					for (String hash : hashes) {
						if (hashABuscar.equals(hash)) {
							if (!primeraVez)
								servidores.append(",");
							servidores.append(servidor.getAddress().getHostAddress() + ":" + servidor.getPort());
							primeraVez = false;
						}
					}
				}
				if (servidores.length() < 1) {
					if (Directory.dir_debug_2)
						System.err.println("* [WARNING] No servers found for this file, please try again:");
					break;
				}
				msgToSend.setServers(servidores);
				msgToSend.setFileNameSubstring(subcadena);
				if (Directory.dir_debug_2)
					System.out.println("* [DOWNLOAD] DOWNLOAD message processed successfully.");
			}
			break;
		}
		case DirMessageOps.OPERATION_UNPUBLISH_FILES: {
			if (Directory.dir_debug_2)
				System.out.println("* [UNPUBLISH] Processing UNPUBLISH_FILES message...");

			InetSocketAddress clientAddress = (InetSocketAddress) pkt.getSocketAddress();
			int port = mensaje.getPort();
			InetSocketAddress serverAddress = new InetSocketAddress(clientAddress.getAddress(), port);
			msgToSend = new DirMessage(DirMessageOps.OPERATION_UNPUBLISH_FILES_SUCCESS);

			FileInfo[] archivosDelServidor = ficherosEnServidor.get(serverAddress);
			if (archivosDelServidor == null) {
				ficherosEnServidor.remove(serverAddress);
				if (Directory.dir_debug_2)
					System.out.println("* [UNPUBLISH] Server removed (no files): " + serverAddress);
				break;
			}

			// Recorrer cada archivo del servidor que se va a eliminar
			for (FileInfo archivo : archivosDelServidor) {
				String hash = archivo.fileHash;
				boolean hashEnOtroServidor = false;
				FileInfo archivoDeServidorActivo = null;

				// Buscar si este hash existe en otros servidores
				for (InetSocketAddress otroServidor : ficherosEnServidor.keySet()) {
					if (!otroServidor.equals(serverAddress)) {
						FileInfo[] archivosEnOtroServidor = ficherosEnServidor.get(otroServidor);

						for (FileInfo otroArchivo : archivosEnOtroServidor) {
							if (otroArchivo.fileHash.equals(hash)) {
								hashEnOtroServidor = true;
								archivoDeServidorActivo = otroArchivo;
								break;
							}
						}
					}
					if (hashEnOtroServidor)
						break;
				}

				if (!hashEnOtroServidor) {
					// Como no se encuentra en otro servidor, lo podemos eliminar directamente
					ficherosDisponibles.remove(hash);
					if (Directory.dir_debug_2)
						System.out.println("* [UNPUBLISH] Removed file: " + archivo.fileName + " (hash: " + hash + ")");
				} else {
					// Actualizar el nombre del archivo
					ficherosDisponibles.put(hash, archivoDeServidorActivo);
					if (Directory.dir_debug_2)
						System.out.println("* [UNPUBLISH] Updated file \"" + archivo.fileName + "\" -> \""
								+ archivoDeServidorActivo.fileName + "\"");
				}
			}

			// Eliminar el servidor
			ficherosEnServidor.remove(serverAddress);
			if (Directory.dir_debug_2)
				System.out.println("* [UNPUBLISH] Server unregistered successfully");
			break;
		}
		default:
			if (!Directory.dir_debug_0)
				System.err.println("* [ERROR] Unexpected message operation: \"" + operation + "\"");
			System.exit(-1);
		}
		if (Directory.dir_debug_2)
			System.out.print("* [RESPONSE] Message: " + msgToSend.toString());
		String textToSend = msgToSend.toString();
		byte[] respuesta = textToSend.getBytes();
		DatagramPacket respuesta_pkt = new DatagramPacket(respuesta, respuesta.length, pkt.getSocketAddress());
		socket.send(respuesta_pkt);
	}
}