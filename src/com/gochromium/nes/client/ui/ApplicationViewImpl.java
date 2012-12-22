package com.gochromium.nes.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ApplicationViewImpl extends ResizeComposite implements ApplicationView {

	private static ApplicationViewImplUiBinder uiBinder = GWT
			.create(ApplicationViewImplUiBinder.class);

	interface ApplicationViewImplUiBinder extends
			UiBinder<Widget, ApplicationViewImpl> {
	}

	private Presenter listener;
	@UiField ScrollPanel centerPanel;
	@UiField TextBox searchTextBox;
	@UiField VerticalTabBar navigationTabBar;

	
	public ApplicationViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));

		searchTextBox.getElement().setAttribute("type", "search");
		searchTextBox.getElement().setAttribute("results", "5");
		searchTextBox.getElement().setAttribute("placeholder", "   Search Games ...");
	
		navigationTabBar.addTab(new Button("All Games"));
		navigationTabBar.addTab(new Button("Action"));
		navigationTabBar.addTab(new Button("Adventure"));
		navigationTabBar.addTab(new Button("Fighting"));
		navigationTabBar.addTab(new Button("Racing"));
		navigationTabBar.addTab(new Button("Platform"));
		navigationTabBar.addTab(new Button("Sports"));
		navigationTabBar.addTab("<div class='splitter'></div>", true);
		
		
		Button importButton = new Button("Import Game");
		Button settingsButton = new Button("Settings");
		importButton.setStyleName("importButton");
		settingsButton.setStyleName("settingsButton");
		navigationTabBar.addTab(importButton);
		navigationTabBar.addTab(settingsButton);
		
		navigationTabBar.selectTab(0, false);
	}

	@Override
	public void setWidget(IsWidget w) {
		centerPanel.setWidget(w);
	}

	@Override
	public void setPresenter(Presenter listener) {
		this.listener = listener;
	}

	@Override
	public void setWindowTitle(String title) {
		Window.setTitle(title);
	}

	@UiHandler("navigationTabBar")
	void navigationTabChanged(SelectionEvent<Integer> event) {
		listener.onTabSelected(event.getSelectedItem());
	}

	@Override
	public void setSelectedTab(int index) {
		navigationTabBar.selectTab(index, false);
	}
	
	@Override
	public int getSelectedTab() {
		return navigationTabBar.getSelectedTab(); 
	}

	@UiHandler("searchTextBox")
	void onSearchBoxValueChange(ValueChangeEvent<String> e) {
		listener.onSearchValueChanged(e.getValue());
	}

	@UiHandler("searchTextBox")
	void onSearchBoxKeyDown(KeyUpEvent e) {
		listener.onSearchValueChanged(searchTextBox.getText());
	}

	@Override
	public HasValue<String> getSearchValue() {
		return searchTextBox;
	}
}
