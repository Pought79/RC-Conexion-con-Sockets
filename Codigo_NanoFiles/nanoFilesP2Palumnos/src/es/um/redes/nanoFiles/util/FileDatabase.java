package es.um.redes.nanoFiles.util;

import java.io.File;
import java.util.Map;

/**
 * @author rtitos
 * 
 *         Utility class acting as database of local files shared by this peer.
 */

public class FileDatabase {

	private Map<String, FileInfo> files;

	public FileDatabase(String sharedFolder) {
		File theDir = new File(sharedFolder);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		this.files = FileInfo.loadFileMapFromFolder(new File(sharedFolder));
		if (files.size() == 0) {
			System.err.println("*WARNING: No files found in folder " + sharedFolder);
		}
	}

	public FileInfo[] getFiles() {
		FileInfo[] fileinfoarray = new FileInfo[files.size()];
		int numFiles = 0;
		for (FileInfo f : files.values()) {
			fileinfoarray[numFiles++] = f;
		}
		return fileinfoarray;
	}

	public String lookupFilePath(String fileHash) {
		FileInfo f = files.get(fileHash);
		if (f != null) {
			return f.filePath;
		}
		return null;
	}
	
	/**
	 * Añade un nuevo archivo a la base de datos de archivos compartidos
	 * 
	 * @param fileInfo Información del archivo a añadir
	 * @return true si el archivo se añadió correctamente, false en caso contrario
	 */
	public boolean addFile(FileInfo fileInfo) {
		if (fileInfo == null) {
			return false;
		}
		
		// Comprobar si el archivo ya existe en la base de datos
		if (files.containsKey(fileInfo.fileHash)) {
			// El archivo ya existe, no es necesario añadirlo de nuevo
			return false;
		}
		
		// Añadir el archivo a la base de datos
		files.put(fileInfo.fileHash, fileInfo);
		System.out.println("* Added file to database: " + fileInfo.fileName + " (" + fileInfo.fileSize + " bytes)");
		return true;
	}
}
