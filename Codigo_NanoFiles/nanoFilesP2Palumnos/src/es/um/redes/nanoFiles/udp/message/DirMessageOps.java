package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_PING_SUCCESS = "ping_success";
	public static final String OPERATION_PING_FAIL = "ping_fail";
	public static final String OPERATION_GETFILELIST = "getfilelist";
	public static final String OPERATION_FILELIST = "filelist";
	public static final String OPERATION_SERVE = "serve";
	public static final String OPERATION_SERVE_SUCCESS = "serve_success";
	public static final String OPERATION_DOWNLOAD = "download";
	public static final String OPERATION_DOWNLOAD_REQUEST= "download_request";
	public static final String OPERATION_DOWNLOAD_FAIL = "download_fail";
	public static final String OPERATION_UNPUBLISH_FILES = "unpublish_files";
	public static final String OPERATION_UNPUBLISH_FILES_SUCCESS = "unpublish_files_success";



}