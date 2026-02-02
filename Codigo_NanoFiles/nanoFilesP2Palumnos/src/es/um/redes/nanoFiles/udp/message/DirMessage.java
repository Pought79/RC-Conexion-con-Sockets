package es.um.redes.nanoFiles.udp.message;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */

public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_PROTOCOL = "protocol";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_FILENAME_SUBSTRING = "filename_substring";
	private static final String FIELDNAME_FILENAMES = "names";
	private static final String FIELDNAME_FILESIZES = "sizes";
	private static final String FIELDNAME_FILEHASHES = "hashes";
	private static final String FIELDNAME_SERVERS = "servers";
	private static final int INVALID_NUMBER = -1;

	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del
	 * directorio.
	 */
	private String protocolId;
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */

	private int port;
	private String filename_substring;
	private StringBuffer fileNames;
	private StringBuffer fileSizes;
	private StringBuffer fileHashes;
	private StringBuffer servers;

	public DirMessage(String op) {
		operation = op;
		port = INVALID_NUMBER;
		protocolId = null;
		filename_substring = null;
		fileNames = null;
		fileSizes = null;
		fileHashes = null;
		servers = null;

	}

	public DirMessage(String op, String _protocolID) { // Constructor para hacer PING
		this(op);
		protocolId = _protocolID;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	public String getOperation() { // No incluimos el método setOperation porque una vez establecida en el
									// constructor no tiene sentido cambiarla
		return operation;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int _port) {
		if (!(operation.equals(DirMessageOps.OPERATION_SERVE)
				|| (operation.equals(DirMessageOps.OPERATION_UNPUBLISH_FILES))))
			throw new RuntimeException("DirMessage: setPort called for message of unexpected type (" + operation + ")");
		port = _port;
	}

	public String getFileNameSubstring() {
		return filename_substring;
	}

	public void setFileNameSubstring(String _filename_substring) {
		if (!(operation.equals(DirMessageOps.OPERATION_DOWNLOAD)
				|| operation.equals(DirMessageOps.OPERATION_DOWNLOAD_REQUEST)))
			throw new RuntimeException(
					"DirMessage: setFileNameSubstring called for message of unexpected type (" + operation + ")");
		filename_substring = _filename_substring;
	}

	public StringBuffer getFileNames() {
		return fileNames;
	}

	public void setFileNames(StringBuffer name) {
		if (!(operation.equals(DirMessageOps.OPERATION_SERVE) || operation.equals(DirMessageOps.OPERATION_FILELIST)))
			throw new RuntimeException(
					"DirMessage: fileNames called for message of unexpected type (" + operation + ")");
		fileNames = name;
	}

	public StringBuffer getFileSizes() {
		return fileSizes;
	}

	public void setFileSizes(StringBuffer size) {
		if (!(operation.equals(DirMessageOps.OPERATION_SERVE) || operation.equals(DirMessageOps.OPERATION_FILELIST)))
			throw new RuntimeException(
					"DirMessage: fileSizes called for message of unexpected type (" + operation + ")");
		fileSizes = size;
	}

	public StringBuffer getFileHashes() {
		return fileHashes;
	}

	public void setFileHashes(StringBuffer hash) {
		if (!(operation.equals(DirMessageOps.OPERATION_SERVE) || operation.equals(DirMessageOps.OPERATION_FILELIST)))
			throw new RuntimeException(
					"DirMessage: fileHashes called for message of unexpected type (" + operation + ")");
		fileHashes = hash;
	}

	public StringBuffer getServers() {
		return servers;
	}

	public void setServers(StringBuffer _servers) {
		if (!(operation.equals(DirMessageOps.OPERATION_FILELIST)
				|| operation.equals(DirMessageOps.OPERATION_DOWNLOAD)))
			throw new RuntimeException(
					"DirMessage: servers called for message of unexpected type (" + operation + ")");
		servers = _servers;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public void setProtocolID(String protocolIdent) {
		if (!(operation.equals(DirMessageOps.OPERATION_PING)
				|| operation.equals(DirMessageOps.OPERATION_PING_SUCCESS))) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {
		return protocolId;
	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_PROTOCOL: {
				if (m != null && value != null)
					m.setProtocolID(value);
				break;
			}
			case FIELDNAME_PORT: {
				if (m != null && value != null)
					m.setPort(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_FILENAME_SUBSTRING: {
				if (m != null && value != null)
					m.setFileNameSubstring(value);
				break;
			}
			case FIELDNAME_FILENAMES: {
				if (m != null && value != null)
					m.setFileNames(new StringBuffer(value));
				break;
			}
			case FIELDNAME_FILESIZES: {
				if (m != null && value != null)
					m.setFileSizes(new StringBuffer(value));
				break;
			}
			case FIELDNAME_FILEHASHES: {
				if (m != null && value != null)
					m.setFileHashes(new StringBuffer(value));
				break;
			}

			case FIELDNAME_SERVERS: {
				if (m != null && value != null)
					m.setServers(new StringBuffer(value));
				break;
			}

			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_PING_SUCCESS: {
			sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_FILELIST: {
			sb.append(FIELDNAME_FILENAMES + DELIMITER + fileNames + END_LINE);
			sb.append(FIELDNAME_FILESIZES + DELIMITER + fileSizes + END_LINE);
			sb.append(FIELDNAME_FILEHASHES + DELIMITER + fileHashes + END_LINE);
			sb.append(FIELDNAME_SERVERS + DELIMITER + servers + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SERVE: {
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			sb.append(FIELDNAME_FILENAMES + DELIMITER + fileNames + END_LINE);
			sb.append(FIELDNAME_FILESIZES + DELIMITER + fileSizes + END_LINE);
			sb.append(FIELDNAME_FILEHASHES + DELIMITER + fileHashes + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD_REQUEST: {
			sb.append(FIELDNAME_FILENAME_SUBSTRING + DELIMITER + filename_substring + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD: {
			sb.append(FIELDNAME_FILENAME_SUBSTRING + DELIMITER + filename_substring + END_LINE);
			sb.append(FIELDNAME_SERVERS + DELIMITER + servers + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_UNPUBLISH_FILES: {
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_PING_FAIL:
		case DirMessageOps.OPERATION_GETFILELIST:
		case DirMessageOps.OPERATION_DOWNLOAD_FAIL:
		case DirMessageOps.OPERATION_UNPUBLISH_FILES_SUCCESS:
		case DirMessageOps.OPERATION_SERVE_SUCCESS: {
			break;
		}
		}
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}