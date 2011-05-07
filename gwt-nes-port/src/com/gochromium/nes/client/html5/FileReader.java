package com.gochromium.nes.client.html5;

import com.google.gwt.core.client.JavaScriptObject;

public class FileReader extends JavaScriptObject {

	/**
	 * Error code thrown when the file resource cannot be
	 * found at the time the read was processed.
	 */
	public static final int NOT_FOUND_ERR = 8;
	/**
	 * Error code thrown when:
	 * <ul>
	 * <li>Certain files are unsafe for access within a Web application</ul>
	 * <li>Too many read calls are being made on file resources</ul>
	 * <li>Tile has changed on disk since the user selected it</ul>
	 * </ul>
	 */
	public static final int SECURITY_ERR = 18;
	/**
	 * Error code thrown when the read operation was aborted,
	 * typically with a call to {@code abort()}.
	 */
	public static final int ABORT_ERR = 20;
	/**
	 * Error code thrown when the file cannot be read, typically 
	 * due to permission problems that occur after a reference to
	 * a file has been acquired (e.g. concurrent lock with another application).
	 */
	public static final int NOT_READABLE_ERR = 24;
	/**
	 * Error code thrown if the URL length limitations for Data URLs
	 * in their implementations place limits on the file data that can
	 * be represented as a Data URL.
	 */
	public static final int ENCODING_ERR = 26;
	
	/**
	 * The object has been constructed, and there are no pending reads.
	 */
	public static final int EMPTY = 0;
	/**
	 * A file is being read. One of the read methods is being processed.
	 */
	public static final int LOADING = 1;
	/**
	 * The entire file has been read into memory, or a file error
	 * occurred during read, or the read was aborted using {@code abort{}}.
	 * The FileReader is no longer reading a {@link File}.
	 */
	public static final int DONE = 2;
	

	protected FileReader() {

	}

	public static final FileReader create(){
		return createNative();
	}

	protected final native static FileReader createNative() /*-{
		return new FileReader();
	}-*/;

/**
 * this.@com.gochromium.gwt.file.FileReader::handleLoad(Lcom/gochromium/gwt/file/ProgressCallback;Lcom/gochromium/gwt/file/ProgressEvent;) (callback, event);
 */

	public final void readAsText(File file, ProgressCallback callback) {
		this.createEventHandlers(callback);
		this.readAsText(this, file);
	}

	public final void readAsBinaryString(File file, ProgressCallback callback) {
		this.createEventHandlers(callback);
		this.readAsBinaryString(this, file);
	}
	

	public final void readAsDataURL(File file, ProgressCallback callback) {
		this.createEventHandlers(callback);
		this.readAsDataURL(this, file);
	}

	private final native String createEventHandlers(ProgressCallback callback) /*-{
		this.onload = function(event) {
			@com.gochromium.nes.client.html5.FileReader::handleLoad(Lcom/gochromium/nes/client/html5/ProgressCallback;Lcom/gochromium/nes/client/html5/ProgressEvent;) (callback, event);
		};
		this.onloadstart = function(event) {
			@com.gochromium.nes.client.html5.FileReader::handleLoadStart(Lcom/gochromium/nes/client/html5/ProgressCallback;Lcom/gochromium/nes/client/html5/ProgressEvent;) (callback, event);
		};
		this.onloadend = function(event) {
			@com.gochromium.nes.client.html5.FileReader::handleLoadEnd(Lcom/gochromium/nes/client/html5/ProgressCallback;Lcom/gochromium/nes/client/html5/ProgressEvent;) (callback, event);
		};
		this.onabort = function(event) {
			@com.gochromium.nes.client.html5.FileReader::handleAbort(Lcom/gochromium/nes/client/html5/ProgressCallback;Lcom/gochromium/nes/client/html5/ProgressEvent;) (callback, event);
		};
		this.onerror = function(event) {
			@com.gochromium.nes.client.html5.FileReader::handleError(Lcom/gochromium/nes/client/html5/ProgressCallback;Lcom/gochromium/nes/client/html5/ProgressEvent;) (callback, event);
		};
		this.onprogress = function(event) {
			@com.gochromium.nes.client.html5.FileReader::handleProgress(Lcom/gochromium/nes/client/html5/ProgressCallback;Lcom/gochromium/nes/client/html5/ProgressEvent;) (callback, event);
		};
	}-*/;


	
	protected final native String readAsText(FileReader reader, File file) /*-{
		reader.readAsText(file);
	}-*/;

	protected final native String readAsDataURL(FileReader reader, File file) /*-{
		reader.readAsDataURL(file);
	}-*/;
	//,'ISO-8859-1');
	protected final native String readAsBinaryString(FileReader reader, File file) /*-{
		reader.readAsBinaryString(file);
	}-*/;

	private static final void handleLoad(ProgressCallback callback, ProgressEvent e) {
		callback.onLoad(e);
	}

	private static final void handleAbort(ProgressCallback callback, ProgressEvent e) {
		callback.onAbort(e);
	}

	private static final void handleError(ProgressCallback callback, ProgressEvent e) {
		callback.onError(e);
	}

	private static final void handleProgress(ProgressCallback callback, ProgressEvent e) {
		callback.onProgress(e);
	}

	private static final void handleLoadEnd(ProgressCallback callback, ProgressEvent e) {
		callback.onLoadEnd(e);
	}

	private static final void handleLoadStart(ProgressCallback callback, ProgressEvent e) {
		callback.onLoadStart(e);
	}
}
