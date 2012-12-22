package com.gochromium.nes.client.ui;

import java.util.HashMap;

import com.gochromium.nes.client.model.Controller;
import com.gochromium.nes.client.model.ControllerJso;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class GameSettingsViewImpl extends Composite implements GameSettingsView {

	private static GameSettingsViewImplUiBinder uiBinder = GWT
			.create(GameSettingsViewImplUiBinder.class);

	interface GameSettingsViewImplUiBinder extends
			UiBinder<Widget, GameSettingsViewImpl> {
	}
	
	static HashMap<Integer, String> KEYS = new HashMap<Integer, String>();
	
	static {
		// add characters 0-9, a-z
		for(int i=48;i<91;i++) { 
			KEYS.put(i, String.valueOf(Character.toChars(i)));
		}
		
		KEYS.put(38, "Up Array");
		KEYS.put(40, "Down Array");
		KEYS.put(37, "Left Array");
		KEYS.put(39, "Right Array");
		KEYS.put(13, "Enter");
		KEYS.put(17, "Ctrl");
		KEYS.put(18, "Alt");
		KEYS.put(9, "Tab");
		KEYS.put(32, "Space");
		KEYS.put(186, "Semi-colon");
		KEYS.put(222, "Single Quote");
		KEYS.put(188, "Comma");
		KEYS.put(190, "Period");
		KEYS.put(191, "Forward Slash");
		KEYS.put(219, "Open Bracket");
		KEYS.put(221, "Close Bracket");
	}
	


	private Presenter listener;
	private Controller controller;
	@UiField TextBox upTextBox;
	@UiField TextBox downTextBox;
	@UiField TextBox leftTextBox;
	@UiField TextBox rightTextBox;
	@UiField TextBox aTextBox;
	@UiField TextBox bTextBox;
	@UiField TextBox selectTextBox;
	@UiField TextBox startTextBox;
	@UiField Button saveButton;
	@UiField Button editButton;
	

	public GameSettingsViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	

	@Override
	public void setPresenter(Presenter listener) {
		this.listener = listener;
	}

	@Override
	public void setController(Controller controller) {
		if(controller==null) {
			this.controller = ControllerJso.create();
			this.controller.setA(88);
			this.controller.setB(90);
			this.controller.setSelect(17);
			this.controller.setStart(13);
			this.controller.setUp(38);
			this.controller.setDown(40);
			this.controller.setLeft(37);
			this.controller.setRight(39);
		} else {
			this.controller = controller;
		}
		
		setTextBox(upTextBox, this.controller.getUp());
		setTextBox(downTextBox, this.controller.getDown());
		setTextBox(leftTextBox, this.controller.getLeft());
		setTextBox(rightTextBox, this.controller.getRight());
		setTextBox(aTextBox, this.controller.getA());
		setTextBox(bTextBox, this.controller.getB());
		setTextBox(selectTextBox, this.controller.getSelect());
		setTextBox(startTextBox, this.controller.getStart());
	}

	@Override
	public Controller getController() {
		return controller;
	}
	



	@UiHandler("saveButton")
	void onSave(ClickEvent e) {
		saveButton.setVisible(false);
		enableAll(false);
		editButton.setVisible(true);
		listener.onSave();
	}

	@UiHandler("editButton")
	void onEdit(ClickEvent e) {
		editButton.setVisible(false);
		enableAll(true);
		saveButton.setVisible(true);
	}
	
	void enableAll(boolean enable) {
		upTextBox.setEnabled(enable);
		downTextBox.setEnabled(enable);
		leftTextBox.setEnabled(enable);
		rightTextBox.setEnabled(enable);
		aTextBox.setEnabled(enable);
		bTextBox.setEnabled(enable);
		selectTextBox.setEnabled(enable);
		startTextBox.setEnabled(enable);
	}

	
	@UiHandler({"upTextBox","downTextBox","leftTextBox","rightTextBox",
		"aTextBox","bTextBox","selectTextBox","startTextBox"})
	void onUpText(KeyUpEvent e) {
		TextBox textBox = (TextBox)e.getSource();
		int key = e.getNativeKeyCode();
		setTextBox(textBox, key);
		
		if(textBox.equals(upTextBox))
			controller.setUp(key);
		else if(textBox.equals(downTextBox))
			controller.setDown(key);
		else if(textBox.equals(leftTextBox))
			controller.setLeft(key);
		else if(textBox.equals(rightTextBox))
			controller.setRight(key);
		else if(textBox.equals(aTextBox))
			controller.setA(key);
		else if(textBox.equals(bTextBox))
			controller.setB(key);
		else if(textBox.equals(selectTextBox))
			controller.setSelect(key);
		else if(textBox.equals(startTextBox))
			controller.setStart(key);
	}

	void setTextBox(TextBox text, int key) {
		String keyCode = KEYS.get(key);
		//TODO: code for setting keys kind of sucks. what happens if key code not found?
		if(keyCode==null) return;
		keyCode = (keyCode.length()==1)?keyCode.toUpperCase():keyCode;
		text.setText(keyCode);
	}


}
