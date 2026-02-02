
package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.Socket;

public class NFServerThread extends Thread {
	private Socket clientSocket;

	public NFServerThread(Socket socket) {
		this.clientSocket = socket;
	}

	@Override
	public void run() {
		/*
		 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
		 * de los cuales simplemente se encarga de invocar a NFServer.serveFilesToClient
		 * con el socket retornado por el método accept
		 */
		NFServer.serveFilesToClient(clientSocket); //Llamamos al método NFServer.serveFilesToClient

		try {
			if (clientSocket != null ) {
				clientSocket.close(); //Cerramos el socket
			}
		} catch (IOException e) {
			System.err.println("Error al cerrar socket del cliente: " + e.getMessage());
		}
	}
}
