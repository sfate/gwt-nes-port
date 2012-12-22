package com.gochromium.nes.client.activity;

import com.gochromium.nes.client.ClientFactory;
import com.gochromium.nes.client.event.SearchEvent;
import com.gochromium.nes.client.place.GameListPlace;
import com.gochromium.nes.client.place.GameSettingsPlace;
import com.gochromium.nes.client.place.GameUploadPlace;
import com.gochromium.nes.client.ui.ApplicationView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ApplicationActivity extends AbstractActivity implements ActivityMapper, ApplicationView.Presenter {

	private ClientFactory clientFactory;
	private ApplicationView view;
	private EventBus eventBus;
	private ActivityManager activityManager;


	public ApplicationActivity(ClientFactory clientFactory, EventBus eventBus) {
		this.clientFactory = clientFactory;
		this.eventBus = eventBus;
		this.activityManager = new ActivityManager(this, eventBus);
	}

	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);	
	}

	@Override
	public Activity getActivity(Place place) {
		Activity activity = null;
		
		if(place instanceof GameUploadPlace) {
			activity = new GameUploadActivity((GameUploadPlace) place, clientFactory);
			view.setSelectedTab(8);
		} else if(place instanceof GameListPlace) {
			activity = new GameListActivity((GameListPlace) place, clientFactory);
			if(view.getSelectedTab()>7)
				view.setSelectedTab(0);
		} else if(place instanceof GameSettingsPlace) {
			activity = new GameSettingsActivity((GameSettingsPlace) place, clientFactory);
			view.setSelectedTab(9);
		}
		
		return activity;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
	
		view = clientFactory.getApplicationView();

		view.setPresenter(this);
		panel.setWidget(view.asWidget());

		
		activityManager.setDisplay(view);
	}


	@Override
	public void onTabSelected(int index) {
		
		Place goToPlace = null;
		
		if(index == 8) {
			goToPlace = new GameUploadPlace();
		} else if(index == 9) {
			goToPlace = new GameSettingsPlace();
		} else {
			goToPlace = new GameListPlace(
					view.getSearchValue().getValue(),  getGenreFilterText());
		}

		
//		//there is a good chance we are already on the Game List place
//		// so let's double-check to avoid an unnecessary refresh
//		if(index<7 && view.getSelectedTab()>7) {
//			goTo(goToPlace);
//			return;
//		}
		
		goTo(goToPlace);
	}

	@Override
	public void onSearchValueChanged(String text) {
		eventBus.fireEvent(new SearchEvent(
				text, getGenreFilterText()));
	}

	public String getGenreFilterText() {
		return getGenreFilterText(view.getSelectedTab());
	}
	
	public String getGenreFilterText(int tabIndex) {
		switch(tabIndex) {
		case 1 : return "Action";
		case 2 : return "Adventure";
		case 3 : return "Fighting";
		case 4 : return "Racing";
		case 5 : return "Platform";
		case 6 : return "Sports";
		}
		return "";
	}
}
