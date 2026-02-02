package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	public DataInputStream dis;
	public DataOutputStream dos;

	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {

		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */

		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */

		serverAddr = fserverAddr;
		socket = new Socket(fserverAddr.getAddress(), fserverAddr.getPort()); // Iniciamos la conexión en el puerto del
																				// servidor de dirección fserverAddr
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

	}

	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			// Crear y enviar un mensaje PeerMessage al servidor
			PeerMessage mensajeEnviar = new PeerMessage(PeerMessageOps.OPCODE_FILEDOWNLOAD_CHUNK, "test_file", "abcd",
					0, (short) 1024);
			if (!NanoFiles.nano_debug_0)
				System.out.println("[Cliente]: Enviando mensaje con opcode: " + mensajeEnviar.getOpcode());
			mensajeEnviar.writeMessageToOutputStream(dos);

			// Recibir la respuesta del servidor
			PeerMessage mensajeRecibido = PeerMessage.readMessageFromInputStream(dis);
			if (!NanoFiles.nano_debug_0)
				System.out.println("[Cliente]: Recibido mensaje con opcode: " + mensajeRecibido.getOpcode());

			// Verificar la respuesta
			if (mensajeRecibido.getOpcode() == PeerMessageOps.OPCODE_FILE_CHUNK) {
				if (!NanoFiles.nano_debug_0) {
					System.out.println("[Cliente]: Verificación correcta - Recibido chunk de archivo");
					System.out.println("[Cliente]: Hash del archivo: " + mensajeRecibido.getFileHash());
					System.out.println("[Cliente]: Tamaño del chunk: " + mensajeRecibido.getSizeChunk());
				}
			} else if (mensajeRecibido.getOpcode() == PeerMessageOps.OPCODE_FILE_NOT_FOUND) {
				if (!NanoFiles.nano_debug_0)
					System.out.println("[Cliente]: Archivo no encontrado en el servidor");
			} else {
				if (!NanoFiles.nano_debug_0)
					System.out.println(
							"[Cliente]: Recibido mensaje con opcode no válido: " + mensajeRecibido.getOpcode());
			}
		} catch (IOException e) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("Error en la comunicación con el servidor");
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			socket.close(); // Cerramos el socket
		} catch (IOException e) {
			if (!NanoFiles.nano_debug_0)
				System.err.println("Error al intentar cerrar el socket cliente");
			e.printStackTrace();
		}
	}

	// Método para obtener la dirección de socket
	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
