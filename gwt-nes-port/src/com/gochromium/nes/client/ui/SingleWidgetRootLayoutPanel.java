package com.gochromium.nes.client.ui;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SingleWidgetRootLayoutPanel implements HasOneWidget {

	public static SingleWidgetRootLayoutPanel rootPanelWrapper = new SingleWidgetRootLayoutPanel();
	
	public static SingleWidgetRootLayoutPanel get() {
		return rootPanelWrapper;
	}
	
	@Override
	public Widget getWidget() {
		return RootLayoutPanel.get();
	}
	
	@Override
	public void setWidget(IsWidget w) {
		clear();
		RootLayoutPanel.get().add(w);
	}

	@Override
	public void setWidget(Widget w) {
		clear();
		RootLayoutPanel.get().add(w);
	}

	protected void clear() {
		if(RootLayoutPanel.get().getWidgetCount()>0) {
			RootLayoutPanel.get().clear();
		}
	}
}
