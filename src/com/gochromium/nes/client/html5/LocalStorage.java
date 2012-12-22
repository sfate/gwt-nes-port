package com.gochromium.nes.client.html5;

import java.io.IOException;

import com.google.gwt.core.client.JavaScriptException;

public class LocalStorage {

  public static String getItem(String key) throws IOException {
    try {
      return getItemImpl(key);
    } catch (JavaScriptException e) {

      throw new IOException("" + e);
    }
  }

  public static String key(int index) throws IOException {
    try {
      return keyImpl(index);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static int length() throws IOException {
    try {
      return lengthImpl();
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static void removeItem(String key) throws IOException {
    try {
      removeItemImpl(key);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static void setItem(String key, String value) throws IOException {
    try {
      setItemImpl(key, value);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  private native static String getItemImpl(String key) /*-{
    return $wnd.localStorage.getItem(key);
  }-*/;

  private native static String keyImpl(int index) /*-{
    return $wnd.localStorage.key(index);
  }-*/;

  public native static int lengthImpl() /*-{
    return $wnd.localStorage.length;
  }-*/;

  public native static void setItemImpl(String key, String value) /*-{
    $wnd.localStorage.setItem(key, value);
  }-*/;

  public native static void removeItemImpl(String key) /*-{
		$wnd.localStorage.removeItem(key);
	}-*/;
}