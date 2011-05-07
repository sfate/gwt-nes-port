package com.gochromium.nes.client.ui;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

public interface ApplicationView extends AcceptsOneWidget, IsWidget {

	void setPresenter(Presenter listener);
	void setWindowTitle(String title);
	void setSelectedTab(int index);
	int getSelectedTab();
	HasValue<String> getSearchValue();
	
	public interface Presenter {
		void goTo(Place place);
		void onTabSelected(int index);
		void onSearchValueChanged(String text);
	}
}
