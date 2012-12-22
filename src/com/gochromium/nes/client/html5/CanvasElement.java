package com.gochromium.nes.client.html5;
import com.google.gwt.dom.client.Element;

public class CanvasElement extends Element {

	  protected CanvasElement() {
	  }

	  public final native CanvasRenderingContext2D getContext2D() /*-{
	    return this.getContext("2d");
	  }-*/;

	  public final native int getHeight() /*-{
	    return this.height;
	  }-*/;

	  public final native int getWidth() /*-{
	    return this.width;
	  }-*/;

	  public final native boolean isSupported() /*-{
	    return typeof this.getContext != "undefined";
	  }-*/;

	  public final native void setHeight(int height) /*-{
	    this.height = height;
	  }-*/;

	  public final native void setWidth(int width) /*-{
	    this.width = width;
	  }-*/;

	  public final native String toDataURL() /*-{
	    return this.toDataURL();
	  }-*/;

	  public final native String toDataURL(String mimeType) /*-{
	    return this.toDataURL(mimeType);
	  }-*/;
	  
	}