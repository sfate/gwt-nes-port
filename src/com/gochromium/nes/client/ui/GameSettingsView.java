package com.gochromium.nes.client.ui;

import com.gochromium.nes.client.model.Controller;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface GameSettingsView extends IsWidget {
	
	void setPresenter(Presenter listener);
	void setController(Controller controller);
	Controller getController();

	public interface Presenter {
		void goTo(Place place);
		void onSave();
	}

}
