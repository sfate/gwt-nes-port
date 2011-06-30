package com.googlecode.gwtnes.client.halfnes;

public class ControllerDummyImpl implements ControllerInterface {

	private int controllerbyte = 0x00;
	
	@Override
	public int getbyte() {
		return controllerbyte;
	}

}
