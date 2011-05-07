package com.gochromium.nes.client;

import com.gochromium.nes.client.activity.ApplicationActivity;
import com.gochromium.nes.client.place.GameListPlace;
import com.gochromium.nes.client.ui.SingleWidgetRootLayoutPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ApplicationEntryPoint implements EntryPoint {


	private Place defaultPlace = new GameListPlace();
	
	public void onModuleLoad() {
		ClientFactory clientFactory = new ClientFactoryImpl();// GWT.create(ClientFactory.class);
		EventBus eventBus = clientFactory.getEventBus();
		PlaceController placeController = clientFactory.getPlaceController();

		// Start PlaceHistoryHandler with our PlaceHistoryMapper
		ApplicationPlaceHistoryMapper historyMapper = GWT.create(ApplicationPlaceHistoryMapper.class);
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(placeController, eventBus, defaultPlace);
		
		//create the main activity manager / application panel
		ApplicationActivity activityMapper = new ApplicationActivity(clientFactory, eventBus);

		//this will load the default page, kick off the mapper code
		activityMapper.start(SingleWidgetRootLayoutPanel.get(), eventBus);
		
		// Goes to place represented on URL or default place
		historyHandler.handleCurrentHistory();
	}
}
