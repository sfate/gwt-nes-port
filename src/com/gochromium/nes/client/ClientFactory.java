package com.gochromium.nes.client;

import com.gochromium.nes.client.dao.GameDao;
import com.gochromium.nes.client.dao.SettingsDao;
import com.gochromium.nes.client.service.MetaDataService;
import com.gochromium.nes.client.ui.ApplicationView;
import com.gochromium.nes.client.ui.GameListView;
import com.gochromium.nes.client.ui.GameSettingsView;
import com.gochromium.nes.client.ui.GameUploadView;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;

public interface ClientFactory {

	EventBus getEventBus();
	PlaceController getPlaceController();

	GameDao getGameDao();
	SettingsDao getSettingsDao();
	MetaDataService getMetaDataService();
	
	GameListView getGameListView();
	GameUploadView getGameUploadView();
	GameSettingsView getGameSettingsView();
	ApplicationView getApplicationView();
}
