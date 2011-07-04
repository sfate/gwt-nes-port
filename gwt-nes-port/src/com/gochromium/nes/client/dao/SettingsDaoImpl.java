package com.gochromium.nes.client.dao;

import java.io.IOException;

import com.gochromium.nes.client.html5.LocalStorage;
import com.gochromium.nes.client.model.Controller;
import com.gochromium.nes.client.model.ControllerJso;

public class SettingsDaoImpl implements SettingsDao {

	@Override
	public Controller getController1() {
		String json = null;
		
		try {
			json = LocalStorage.getItem("SETTINGS_CONTROLLER1");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(json == null)
			return null;
		
		return ControllerJso.fromJSON(json);
	}

	@Override
	public void setController1(Controller controller) {
		try {
			LocalStorage.setItem("SETTINGS_CONTROLLER1",controller.toJSON());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void setController2(Controller controller) {

	}

	@Override
	public Controller getController2() {
		return null;
	}

}
