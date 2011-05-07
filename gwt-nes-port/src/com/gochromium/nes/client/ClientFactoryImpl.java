package com.gochromium.nes.client;

import com.gochromium.nes.client.dao.GameDao;
import com.gochromium.nes.client.dao.GameDaoLocalStorageImpl;
import com.gochromium.nes.client.dao.GameDaoWebDatabaseImpl;
import com.gochromium.nes.client.service.MetaDataService;
import com.gochromium.nes.client.service.MetaDataServiceImpl;
import com.gochromium.nes.client.ui.ApplicationView;
import com.gochromium.nes.client.ui.ApplicationViewImpl;
import com.gochromium.nes.client.ui.GameListView;
import com.gochromium.nes.client.ui.GameListViewImpl;
import com.gochromium.nes.client.ui.GameSettingsView;
import com.gochromium.nes.client.ui.GameSettingsViewImpl;
import com.gochromium.nes.client.ui.GameUploadView;
import com.gochromium.nes.client.ui.GameUploadViewImpl;
import com.google.code.gwt.database.client.Database;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;

@SuppressWarnings("deprecation")
public class ClientFactoryImpl implements ClientFactory
{
	
	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);
	private static final MetaDataService metaDataService = new MetaDataServiceImpl();
	private static final ApplicationView applicationView = new ApplicationViewImpl();
	private static final GameListView gameListView = new GameListViewImpl();
	private static final GameUploadView gameUploadView = new GameUploadViewImpl();
	private static final GameSettingsView gameSettingsView = new GameSettingsViewImpl();
	private static GameDao gameDao;
	
	
	static {
		if(Database.isSupported()) {
			gameDao = new GameDaoWebDatabaseImpl();
		} else {
			gameDao = new GameDaoLocalStorageImpl();
		}
	}
	
	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	@Override
	public GameListView getGameListView() {
		return gameListView;
	}

	@Override
	public GameUploadView getGameUploadView() {
		return gameUploadView;
	}

	@Override
	public GameDao getGameDao() {
		return gameDao;
	}

	@Override
	public MetaDataService getMetaDataService() {
		return metaDataService;
	}

	@Override
	public ApplicationView getApplicationView() {
		return applicationView;
	}

	@Override
	public GameSettingsView getGameSettingsView() {
		return gameSettingsView;
	}

}
