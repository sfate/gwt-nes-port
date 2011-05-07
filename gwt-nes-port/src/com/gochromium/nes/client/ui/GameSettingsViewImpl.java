package com.gochromium.nes.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GameSettingsViewImpl extends Composite implements GameSettingsView {

	private static GameSettingsViewImplUiBinder uiBinder = GWT
			.create(GameSettingsViewImplUiBinder.class);

	interface GameSettingsViewImplUiBinder extends
			UiBinder<Widget, GameSettingsViewImpl> {
	}

	public GameSettingsViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	private Presenter listener;

	@Override
	public void setPresenter(Presenter listener) {
		this.listener = listener;
	}

}
