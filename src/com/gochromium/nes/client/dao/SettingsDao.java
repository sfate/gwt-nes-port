package com.gochromium.nes.client.dao;

import com.gochromium.nes.client.model.Controller;

public interface SettingsDao {

	public Controller getController1();
	public Controller getController2();
	public void setController1(Controller controller);
	public void setController2(Controller controller);
}
