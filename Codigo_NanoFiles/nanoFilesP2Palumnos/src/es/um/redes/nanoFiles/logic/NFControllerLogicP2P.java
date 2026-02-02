package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * TODO: Se necesita un atributo NFServer que actuará como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;
	private Thread thread = null;

	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		boolean running = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("* File server is already running");
		} else {

			/*
			 * TODO: (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
			 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
			 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
			 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
			 * y tratadas en este método. Si se produce una excepción de entrada/salida
			 * (error del que no es posible recuperarse), se debe informar sin abortar el
			 * programa
			 * 
			 */
			assert (fileServer == null);
			try {
				// Crear el servidor de ficheros
				// En este caso, pasaremos 0 como parámetro para tener un servidor con puerto
				// efímero.
				fileServer = new NFServer(0);

				// Verificar que el puerto es válido
				int port = fileServer.getPort();
				if (port > 0) { // Si el puerto es un número válido
					// Crear un hilo para ejecutar el servidor en segundo plano
					thread = new Thread(fileServer);
					// Iniciar el hilo
					thread.start();
					if (NanoFiles.nano_debug_2)
						System.out.println("* File server started and listening on port " + port);
					running = true;
				} else {
					if (!NanoFiles.nano_debug_0)
						System.err.println("* Cannot start the file server - Invalid port");
					fileServer = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				if (!NanoFiles.nano_debug_0)
					System.err.println("* Cannot start the file server: " + e.getMessage());
				fileServer = null;
			}

		}
		return running;

	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// A partir de aquí el código es inalcanzable, puesto que fileServer.test() no
			// termina su ejecución
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("* Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector connector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			connector.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectará
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardará el fichero
	 *                                descargado
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetFileNameSubstring,
			String localFileName) {
		boolean downloaded = false;

		if (serverAddressList.length == 0) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}

		// Comprobamos si ya existe el fichero
		boolean existe = false;
		FileInfo[] ficheros = NanoFiles.db.getFiles();
		for (FileInfo fichero : ficheros) {
			if (fichero.fileName.equals(localFileName)) {
				existe = true;
				break;
			}
		}

		if (existe) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("* Cannot start download - File with name '" + localFileName + "' already exists");
			return false;
		}

		/*
		 * Verificamos si hay sólo hay un fichero que se corresponda con la subcadena
		 * pasada como parámetro.
		 */

		// Crear un array de conectores, uno para cada servidor disponible
		NFConnector[] connectors = new NFConnector[serverAddressList.length];
		int conectados = 0;

		// Establecer conexiones con todos los servidores disponibles
		for (InetSocketAddress serverAddr : serverAddressList) {
			try {
				connectors[conectados] = new NFConnector(serverAddr);
				conectados++;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Verificar que al menos un servidor está disponible
		if (conectados == 0) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("* Cannot start download - None of the servers are online");
			return false;
		}

		// Inicializar variables para la descarga
		short chunkSizeMax = 16384; // Tamaño máximo de cada chunk
		int offset = 0; // Posición actual en el archivo
		int indiceServer = 0; // Índice del servidor actual
		String fileHash = null; // Hash del archivo para verificar integridad
		long tamanoFichero = 0;

		// Contadores para estadísticas
		int[] chunks = new int[conectados];

		// Crear el archivo local
		File outputFile = new File(NanoFiles.sharedDirname, localFileName);
		String hashArchivo = "";
		for (FileInfo fichero : ficheros) {
			if (fichero.fileName.contains(targetFileNameSubstring)) {
				hashArchivo = fichero.fileHash;
				break;
			}
		}

		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			boolean downloadDone = false;
			// Descargar chunks secuencialmente hasta completar el archivo
			while (!downloadDone) {
				if (NanoFiles.nano_debug_2)
					System.out.print("\r" + barraProgreso(offset, tamanoFichero, downloadDone));
				// Seleccionar el siguiente servidor (rotación)
				int currentServer = indiceServer % conectados;
				indiceServer++;

				NFConnector connector = connectors[currentServer];

				// Crear y enviar mensaje solicitando un chunk
				PeerMessage mensajeEnviar = new PeerMessage(PeerMessageOps.OPCODE_FILEDOWNLOAD_CHUNK,
						targetFileNameSubstring, hashArchivo, offset, chunkSizeMax);
				mensajeEnviar.writeMessageToOutputStream(connector.dos);

				// Recibir respuesta del servidor
				PeerMessage mensajeRecibir = PeerMessage.readMessageFromInputStream(connector.dis);

				// Procesar la respuesta según su tipo
				if (mensajeRecibir.getOpcode() == PeerMessageOps.OPCODE_FILE_CHUNK) {
					// Guardar el hash del archivo si es la primera vez
					if (fileHash == null) {
						fileHash = mensajeRecibir.getFileHash();
						tamanoFichero = mensajeRecibir.getFileSize();
					} else if (!fileHash.equals(mensajeRecibir.getFileHash())) {
						// Verificar que el hash coincide con chunks anteriores
						if (NanoFiles.nano_debug_2)
							System.err.println("* Error: Inconsistent file hash between servers");
						fos.close();
						outputFile.delete(); // Eliminar archivo incompleto
						break;
					}

					// Obtener el tamaño real del chunk recibido
					short chunkSize = mensajeRecibir.getSizeChunk();
					byte[] chunkData = mensajeRecibir.getChunk();

					// Escribir los datos en el archivo
					fos.write(chunkData, 0, chunkSize);
					offset += chunkSize;

					// Incrementar contador de chunks para este servidor
					chunks[currentServer]++;
					// Verificar si hemos llegado al final del archivo
					if (chunkSize < chunkSizeMax) {
						downloadDone = true;
					}
				} else if (mensajeRecibir.getOpcode() == PeerMessageOps.OPCODE_FILE_NOT_FOUND) {
					// El archivo no se encuentra en este servidor, probar con otro
					if (NanoFiles.nano_debug_2)
						System.out.println("* File not found on server " + connector.getServerAddr());
				} else {
					// Respuesta inesperada
					if (NanoFiles.nano_debug_2)
						System.err.println("* Unexpected response from server: " + mensajeRecibir.getOpcode());
				}
			}

			// Cerrar el archivo
			fos.close();

			if (downloadDone) {
				if (NanoFiles.nano_debug_2)
					System.out.println("\r" + barraProgreso(offset, tamanoFichero, downloadDone));
				// Verificar la integridad del archivo descargado
				String hashFileCalculated = FileDigest.computeFileChecksumString(outputFile.getPath());

				if (fileHash != null && fileHash.equals(hashFileCalculated)) {
					// Registrar el archivo en la base de datos local
					long fileSize = outputFile.length();
					FileInfo fileInfo = new FileInfo(hashFileCalculated, localFileName, fileSize, outputFile.getPath());
					NanoFiles.db.addFile(fileInfo);

					// Mostrar resumen de la descarga
					if (!NanoFiles.nano_debug_0) {
						System.out.println("* Download complete: " + localFileName + " (" + fileSize + " bytes)");
						System.out.println("* File hash: " + hashFileCalculated);}
					if(NanoFiles.nano_debug_2)
						System.out.println("* Chunks downloaded from each server:");
					for (int i = 0; i < conectados; i++) {
						if(NanoFiles.nano_debug_2)
							System.out.println(
									"  - Server " + connectors[i].getServerAddr() + ": " + chunks[i] + " chunks");
					}
					downloaded = true;
				} else {
					if (NanoFiles.nano_debug_2) {
						System.err.println("* Download failed: Hashes do not match");
						System.err.println("* Expected hash: " + fileHash);
						System.err.println("* Calculated hash: " + hashFileCalculated);
					}
					outputFile.delete(); // Eliminar archivo corrupto
				}
			}
		}catch(

	IOException e)
	{
		if (!NanoFiles.nano_debug_0)
			System.err.println("* Error downloading file: " + e.getMessage());
		e.printStackTrace();
	}finally
	{
		// Cerrar todas las conexiones
		for (int i = 0; i < conectados; i++) {
			if (connectors[i] != null) {
				connectors[i].closeConnection();
			}
		}
	}

	return downloaded;
	}

	private String barraProgreso(long bytesDescargados, long tamanoFichero, boolean completado) {
		int longitud = 20;
		double max = 100.0;
		double percentage;

		if (tamanoFichero > 0) {
			percentage = Math.min(1.0, (double) bytesDescargados / tamanoFichero);
		} else {
			percentage = 0.0;
		}
		int ocupado = (int) (percentage * longitud);
		int espacios = longitud - ocupado;

		// Construir la barra de progreso
		StringBuffer barraProgreso = new StringBuffer();
		barraProgreso.append("* Downloading: [");

		//
		for (int i = 0; i < ocupado; i++) {
			barraProgreso.append("=");
		}

		// Barras vacías
		for (int i = 0; i < espacios; i++) {
			barraProgreso.append(" ");
		}
		barraProgreso.append("], " + bytesDescargados + "/" + tamanoFichero + " bytes -> "
				+ String.format("%.2f%%", percentage * 100));
		if (!completado)
			return barraProgreso.toString();
		StringBuffer salidaCompletado = new StringBuffer("* Downloading: [");
		for (int i = 0; i < ocupado; i++)
			salidaCompletado.append("=");
		salidaCompletado
				.append("] " + tamanoFichero + "/" + tamanoFichero + " bytes -> " + String.format("%.2f%%", max));
		return salidaCompletado.toString();
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		int port = 0;
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros
		 */

		port = fileServer.getPort();

		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		fileServer.stopServer();
		try {
			thread.join();
			fileServer = null;
		} catch (InterruptedException e) {
			if (NanoFiles.nano_debug_2)
				System.err.println("* Error waiting for thread to finish");
			e.printStackTrace();
		}

	}

	protected boolean serving() {
		boolean result = false;
		if (fileServer != null) {
			result = fileServer.isServing();
		}
		return result;

	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer) {
		boolean result = false;

		return result;
	}

}