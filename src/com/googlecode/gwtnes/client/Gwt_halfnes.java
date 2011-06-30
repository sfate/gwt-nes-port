package com.googlecode.gwtnes.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.gwtnes.client.halfnes.ControllerDummyImpl;
import com.googlecode.gwtnes.client.halfnes.ControllerWebImpl;
import com.googlecode.gwtnes.client.halfnes.GuiWebImpl;
import com.googlecode.gwtnes.client.halfnes.NES;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Gwt_halfnes implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
	
		GuiWebImpl gui = new GuiWebImpl();
		RootPanel.get().add(gui);
		
		NES nes = new NES(gui);
		nes.setControllers(new ControllerWebImpl(0), new ControllerDummyImpl());
		nes.run(Base64Util.decodeLines(Games.CONTRA), "Contra.nes");
	}
}
