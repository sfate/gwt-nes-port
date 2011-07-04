package com.gochromium.nes.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

public class ControllerJso extends JavaScriptObject implements Controller {


	protected ControllerJso() {
		
	}
	

	public final native void setUp(int value)  /*-{  this.up = value; }-*/;
	public final native void setDown(int value)  /*-{ this.down = value; }-*/;
	public final native void setLeft(int value)  /*-{ this.left = value; }-*/;
	public final native void setRight(int value)  /*-{ this.right = value; }-*/;
	public final native void setA(int value)  /*-{ this.a = value; }-*/;
	public final native void setB(int value)  /*-{ this.b = value; }-*/;
	public final native void setSelect(int value)  /*-{ this.select = value; }-*/;
	public final native void setStart(int value)  /*-{ this.start = value; }-*/;

	public final native int getUp()  /*-{ return this.up; }-*/;
	public final native int getDown()  /*-{  return this.down;}-*/;
	public final native int getLeft()  /*-{  return this.left; }-*/;
	public final native int getRight()  /*-{  return this.right; }-*/;
	public final native int getA()  /*-{  return this.a; }-*/;
	public final native int getB()  /*-{  return this.b; }-*/;
	public final native int getSelect()  /*-{  return this.select; }-*/;
	public final native int getStart()  /*-{  return this.start; }-*/;
	
	@Override
	public final String toJSON() {
		return (new JSONObject(this).toString());
	}
	
	public static final native ControllerJso create() /*-{
		return new Object();
	}-*/;
	
	public static final native ControllerJso fromJSON(String json) /*-{
		return eval("["+json+",]")[0];
	}-*/;
}
