package com.gochromium.nes.client.activity;

import com.gochromium.nes.client.ClientFactory;
import com.gochromium.nes.client.place.GameSettingsPlace;
import com.gochromium.nes.client.ui.GameSettingsView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GameSettingsActivity extends AbstractActivity
	implements GameSettingsView.Presenter {


	
	private ClientFactory clientFactory;
	private GameSettingsView view;
	@SuppressWarnings("unused")
	private EventBus eventBus;
	

	public GameSettingsActivity(GameSettingsPlace place, ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		this.view = clientFactory.getGameSettingsView();
		this.view.setPresenter(this);
		panel.setWidget(view.asWidget());
		this.eventBus = eventBus;
	}

	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);	
	}
}
