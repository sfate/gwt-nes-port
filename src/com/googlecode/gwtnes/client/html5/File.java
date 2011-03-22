package com.googlecode.gwtnes.client.html5;

import com.google.gwt.core.client.JavaScriptObject;

public final class File extends JavaScriptObject {

	protected File() {
		
	}

//	protected final native static File create(
//			String name, String type, String content) /*-{
//		var file = new File();
//		file.name = name;
//		file.type = type;
//		file.content = content;
//		return file;
//	}-*/;
	
	

	
	public static native boolean isFileSupported() /*-{

		//return $wnd.File && $wnd.FileList && $wnd.FileReader && $wnd.Blob;
		
		return typeof $wnd.File != "undefined" &&
			$wnd.FileReader != "undefined" &&
			$wnd.FileList != "undefined" &&
			$wnd.Blob != "undefined";
	}-*/;

	public native String getName() /*-{
		return this.name;
	}-*/;

	public native String getType() /*-{
		return this.type;
	}-*/;

	public native int getSize() /*-{
		return this.size;
	}-*/;
}
