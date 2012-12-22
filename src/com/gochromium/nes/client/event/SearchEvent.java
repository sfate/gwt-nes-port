package com.gochromium.nes.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class SearchEvent extends GwtEvent<SearchEventHandler> {
	public static Type<SearchEventHandler> TYPE = new Type<SearchEventHandler>();

	private final String searchText;
	private final String filterText;
	
	public SearchEvent(String searchText, String filterText) {
		super();
		this.searchText = searchText;
		this.filterText = filterText;
	}
	
	@Override
	public Type<SearchEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(SearchEventHandler handler) {
		handler.onSearch(this);
	}
	
	public String getSearchText() {
		return searchText;
	}
	
	public String getFilterText() {
		return filterText;
	}
}
