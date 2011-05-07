package com.gochromium.nes.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

public class GameJso extends JavaScriptObject { 

//	private String name;
//	private String image;
//	private String tags;
//	private String cartridge;
//
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	public String getImage() {
//		return image;
//	}
//	public void setImage(String image) {
//		this.image = image;
//	}
//	public String getTags() {
//		return tags;
//	}
//	public void setTags(String tags) {
//		this.tags = tags;
//	}
//	public String getCartridge() {
//		return cartridge;
//	}
//	public void setCartridge(String cartridge) {
//		this.cartridge = cartridge;
//	}
//
//	public static final GameDTO fromJSON(String json) {
//		GameDTO game = new GameDTO();
//		JSONObject value = (JSONObject)JSONParser.parseStrict(json);
//
//		game.name = value.get("name").isString().stringValue();
//		game.image = value.get("image").isString().stringValue();
//		game.tags = value.get("tags").isString().stringValue();
//		game.cartridge = value.get("cartridge").isString().stringValue();
//	}
//
//	public final String toJSON() {
//		JSONObject result = new JSONObject();
//	    result.put("name", new JSONString(getName()));
//	    result.put("image", new JSONString(getImage()));
//	    result.put("tags", new JSONString(getTags()));
//	    result.put("cartridge", new JSONString(getCartridge()));
//	    return result.toString();
//	}

	protected GameJso() {
		
	} 

	public final native String getKey() /*-{ return this.key; }-*/;
	public final native String getName() /*-{ return this.name; }-*/;
	public final native String getImage() /*-{ return this.image; }-*/;
	public final native String getTags() /*-{ return this.tags; }-*/;
	public final native String getCartridge() /*-{ return this.cartridge; }-*/;
	
	public final native void setKey(String value) /*-{ this.key = value; }-*/;
	public final native void setName(String value) /*-{ this.name = value; }-*/;
	public final native void setImage(String value) /*-{ this.image = value; }-*/;
	public final native void setTags(String value) /*-{ this.tags = value; }-*/;
	public final native void setCartridge(String value) /*-{ this.cartridge = value; }-*/;

	public static final native GameJso create() /*-{
		return new Object();
	}-*/;
	
	public static final native GameJso fromJSON(String json) /*-{
		return eval("["+json+",]")[0];
	}-*/;
	
//	public static final native void eval(String json) /*-{
//    	alert(json);
//    	alert(eval(json));
//	}-*/;	
	
//	public static final GameDTO fromJSON(String json) {
//		GameDTO game = create();
//		JSONObject value = (JSONObject)JSONParser.parseStrict(json);
//
//		game.setName(value.get("name").isString().stringValue());
//		if(value.get("image")!=null)
//			game.setImage(value.get("image").isString().stringValue());
//		if(value.get("tags")!= null)
//			game.setTags(value.get("tags").isString().stringValue());
//		game.setCartridge(value.get("cartridge").isString().stringValue());
//		return game;
//	}
	
	public final String toJSON() {
		
		return (new JSONObject(this).toString());
//		
//		JSONObject result = new JSONObject();
//        result.put("name", new JSONString(getName()));
//        if(getImage()!=null)
//        	result.put("image", new JSONString(getImage()));
//        if(getTags()!=null)
//        	result.put("tags", new JSONString(getTags()));
//        result.put("cartridge", new JSONString(getCartridge()));
//        return result.toString();
	}
}
