package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServer implements Runnable {

	public static final int PORT = 10000;
	private boolean running = false;
	private ServerSocket serverSocket = null;

	/*
	 * Constructor de NFServer que usa el puerto pasado como parámetro En caso de
	 * que dicho puerto sea 0, estaremos ante un caso de puerto efímero.
	 */
	public NFServer(int port) throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		InetSocketAddress socketAddress = new InetSocketAddress(port);
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(socketAddress);
			if (NanoFiles.nano_debug_2)
				System.out.println("* [Server] Running on port " + getPort());
		} catch (IOException e) {
			if (!NanoFiles.nano_debug_0)
				System.out.println("* [Error] Server startup failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/* Constructor de NFServer por defecto. */
	public NFServer() throws IOException {
		this(PORT);
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println("* [Test Mode] Failed to run file server - socket not bound");
			return;
		} else {
			System.out.println("* [Test Mode] NFServer running on " + serverSocket.getLocalSocketAddress());
		}

		while (true) {
			try {

				/*
				 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
				 * otros peers que soliciten descargar ficheros.
				 */
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] Waiting for client connection...");
				Socket socket = serverSocket.accept();
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] Client connected from " + socket.getInetAddress().toString() + ":"
							+ socket.getPort());

				// Crear flujos de entrada/salida para la comunicación
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				// Recibir el mensaje PeerMessage enviado por el cliente
				PeerMessage mensajeRecibir = PeerMessage.readMessageFromInputStream(dis);
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] Received message with opcode: " + mensajeRecibir.getOpcode());

				// Enviar un mensaje de respuesta al cliente
				PeerMessage mensajeEnviar = new PeerMessage(PeerMessageOps.OPCODE_FILE_CHUNK, "test", (short) 4,
						new byte[] { 1, 2, 3, 4 });
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] Sending response with opcode: " + mensajeEnviar.getOpcode());
				mensajeEnviar.writeMessageToOutputStream(dos);

				// Cerrar la conexión
				socket.close();
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] Connection closed");

			} catch (IOException e) {
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] Error: " + e.getMessage());
				e.printStackTrace();
			}

			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */
		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		running = true;
		try {
			if (NanoFiles.nano_debug_2)
				System.out.println("* [Server] Listening on port " + getPort() + " (background mode)");
			while (running && !serverSocket.isClosed()) {

				Socket clientSocket = serverSocket.accept();
				if (NanoFiles.nano_debug_2)
					System.out.println("* [Server] New client connected: " + clientSocket.getInetAddress());
				new NFServerThread(clientSocket).start();
			}
		} catch (IOException e) {
			if (running) {
				if (!NanoFiles.nano_debug_0)
					System.err.println("* [Error] Failed to accept connection: " + e.getMessage());
			}
		}
	}
	/*
	 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
	 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
	 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
	 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
	 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
	 * más de un cliente conectado a este servidor.
	 */

	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */

	public boolean isServing() {
		return !serverSocket.isClosed();
	}

	public void stopServer() {
		running = false;
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("* [Error] Failed to close server: " + e.getMessage());
		}
	}

	public int getPort() {
		if (serverSocket != null) {
			return serverSocket.getLocalPort();
		}
		return 0;
	}

	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			boolean running = true;
			while (running) {
				PeerMessage recibirMensaje;
				try {
					recibirMensaje = PeerMessage.readMessageFromInputStream(dis);
				} catch (EOFException e) {
					if (NanoFiles.nano_debug_2)
						System.out.println("* [Server] Client closed connection");
					break;
				}

				byte opcode = recibirMensaje.getOpcode();

				if (opcode == PeerMessageOps.OPCODE_FILEDOWNLOAD_CHUNK) {
					FileInfo[] ficheros = NanoFiles.db.getFiles();
					String hashBuscado = recibirMensaje.getFileHash();
					FileInfo escogido = null;
					for (FileInfo fichero : ficheros) {
						if (fichero.fileHash.equals(hashBuscado)) {
							escogido = fichero;
							break;
						}
					}

					if (escogido == null) {
						escogido = FileInfo.lookupFilenameSubstring(ficheros, recibirMensaje.getFileName())[0];
					}

					if (escogido != null) {
						String ruta = NanoFiles.db.lookupFilePath(escogido.fileHash);

						RandomAccessFile raf = new RandomAccessFile(ruta, "r");
						int pos = recibirMensaje.getPosChunk();
						int tam = recibirMensaje.getSizeChunk();
						raf.seek(pos);

						long resta = escogido.fileSize - pos;
						if (resta < tam)
							tam = (int) resta;

						byte[] datos = new byte[tam];
						raf.readFully(datos);
						raf.close();

						PeerMessage respuesta = new PeerMessage(PeerMessageOps.OPCODE_FILE_CHUNK, escogido.fileHash,
								(short) datos.length, datos, escogido.fileSize);
						respuesta.writeMessageToOutputStream(dos);
					} else {
						PeerMessage respuesta = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
						respuesta.writeMessageToOutputStream(dos);
					}
				} else {
					if (NanoFiles.nano_debug_2)
						System.out.println("* [Server] Invalid opcode - closing connection");
					running = false;
				}
			}

			socket.close();
			if (NanoFiles.nano_debug_2)
				System.out.println("* [Server] Connection closed");

		} catch (IOException e) {
			if (NanoFiles.nano_debug_2)
				System.err.println("* [Error] Server error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
	 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
	 * tipo de mensaje recibido, enviando los correspondientes mensajes de
	 * respuesta.
	 */
	/*
	 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
	 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
	 * compartidos. Los ficheros compartidos se pueden obtener con
	 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
	 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
	 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
	 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
	 * de su hash completo.
	 */

}