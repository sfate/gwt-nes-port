package com.gochromium.nes.client.ui;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface GameUploadView extends IsWidget {
	
	void setPresenter(Presenter listener);
	void alert(String msg);

	public interface Presenter {
		void goTo(Place place);
		void onLoadError(String message);
		void onLoadFile(String fileName, byte[] contents);
	}

}
