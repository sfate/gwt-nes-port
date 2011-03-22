package com.googlecode.gwtnes.client.html5;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;

public class FileList extends JsArray<File> { // extends JavaScriptObject {

	protected FileList() {

	}
//
//	public final native File getFile(int index) /*-{
//		return this[index];
//	}-*/;
//
//
//	public final native int getLength() /*-{
//		return this.length;
//	}-*/;
	
	public static final native FileList fromEvent(NativeEvent event) /*-{
		return event.target.files;
	}-*/;
}
