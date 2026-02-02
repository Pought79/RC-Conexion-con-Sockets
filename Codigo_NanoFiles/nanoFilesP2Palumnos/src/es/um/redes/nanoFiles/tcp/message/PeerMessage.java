package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PeerMessage {

	private byte opcode;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */

	private String fileName;
	private String fileHash;
	private byte[] chunkData;
	private int posChunk;
	private short sizeChunk;
	private long fileSize;

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	// Constructor con setters y not fount
	public PeerMessage(byte op) {
		opcode = op;
	}

	public PeerMessage(byte op, String fName, String fHash, int pChunk, short sChunk) {
	    opcode = op;
	    fileName = fName;
	    fileHash = fHash;
	    posChunk = pChunk;
	    sizeChunk = sChunk;
	}

	public PeerMessage(byte op, String fHash, short sChunk, byte[] ch) {
		opcode = op;
		fileHash = fHash;
		sizeChunk = sChunk;
		chunkData = ch;
	}

	public PeerMessage(byte op, String fHash, short sChunk, byte[] ch, long _fileSize) {
		opcode = op;
		fileHash = fHash;
		sizeChunk = sChunk;
		chunkData = ch;
		fileSize = _fileSize;
	}

	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		if (opcode != 1 && opcode != 3) {
			throw new RuntimeException(
					"PeerMessage: setFileName called for message of unexpected type (" + opcode + ")");
		}
		this.fileName = fileName;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		if (opcode != 3) {
			throw new RuntimeException(
					"PeerMessage: setFileHash called for message of unexpected type (" + opcode + ")");
		}
		this.fileHash = fileHash;
	}

	public byte[] getChunk() {
		return chunkData;
	}

	public void setChunk(byte[] chunk) {
		if (opcode != 3) {
			throw new RuntimeException("PeerMessage: setChunk called for message of unexpected type (" + opcode + ")");
		}
		this.chunkData = chunk;
	}

	public int getPosChunk() {
		return posChunk;
	}

	public void setPosChunk(int posChunk) {
		if (opcode != 1) {
			throw new RuntimeException(
					"PeerMessage: setPosChunk called for message of unexpected type (" + opcode + ")");
		}
		this.posChunk = posChunk;
	}

	public short getSizeChunk() {
		return sizeChunk;
	}

	public void setSizeChunk(short sizeChunk) {
		if (opcode != 1 && opcode != 3) {
			throw new RuntimeException(
					"PeerMessage: setSizeChunk called for message of unexpected type (" + opcode + ")");
		}
		this.sizeChunk = sizeChunk;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long _fileSize) {
		if (opcode != 3) {
			throw new RuntimeException(
					"PeerMessage: setTotalFileSize called for message of unexpected type (" + opcode + ")");
		}
		fileSize = _fileSize;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message;
		byte opcode = dis.readByte();
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILEDOWNLOAD_CHUNK: {
			// Solicitud de chunk
			message = new PeerMessage(opcode);
			int nameSize = dis.readInt();
		    byte[] nameBytes = new byte[nameSize];
		    dis.readFully(nameBytes);
		    message.fileName = new String(nameBytes, StandardCharsets.UTF_8);
			int hashSize = dis.readInt(); 
			byte[] hashBytes = new byte[hashSize];
			dis.readFully(hashBytes); 
			message.fileHash = new String(hashBytes, StandardCharsets.UTF_8);
			message.posChunk = dis.readInt();
			message.sizeChunk = dis.readShort();
			break;
		}
		case PeerMessageOps.OPCODE_FILE_CHUNK: {
			// Respuesta con chunk
			message = new PeerMessage(opcode);
		    int hashSize = dis.readInt();
		    byte[] hashBytes = new byte[hashSize];
		    dis.readFully(hashBytes);
		    message.fileHash = new String(hashBytes, StandardCharsets.UTF_8);
		    message.sizeChunk = dis.readShort();
		    message.fileSize = dis.readLong();
		    message.chunkData = new byte[message.sizeChunk];
		    dis.readFully(message.chunkData);
		    break;
		}
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
			// El servidor no tiene el fichero
			message = new PeerMessage(opcode);
			break;
		}
		default:
			throw new IOException("Unknown P2P opcode: " + opcode);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILEDOWNLOAD_CHUNK: {
			byte[] nameBytes = fileName.getBytes(StandardCharsets.UTF_8);
		    dos.writeInt(nameBytes.length);
		    dos.write(nameBytes);
		    byte[] hashBytes = fileHash.getBytes(StandardCharsets.UTF_8);
		    dos.writeInt(hashBytes.length);
		    dos.write(hashBytes);
		    dos.writeInt(posChunk);
		    dos.writeShort(sizeChunk);
		    break;
		}
		case PeerMessageOps.OPCODE_FILE_CHUNK: {
			byte[] hashBytes = fileHash.getBytes(StandardCharsets.UTF_8);
		    dos.writeInt(hashBytes.length);
		    dos.write(hashBytes);
		    dos.writeShort(sizeChunk);
		    dos.writeLong(fileSize);
		    dos.write(chunkData);
		    break;
		}
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
			break;
		}
		default:
			throw new IOException("Cannot write unknown P2P opcode: " + opcode);
		}
	}

}
