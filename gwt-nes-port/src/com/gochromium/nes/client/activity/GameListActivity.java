package com.gochromium.nes.client.activity;

import java.util.ArrayList;
import java.util.List;

import com.gochromium.nes.client.ClientFactory;
import com.gochromium.nes.client.event.SearchEvent;
import com.gochromium.nes.client.event.SearchEventHandler;
import com.gochromium.nes.client.model.Game;
import com.gochromium.nes.client.place.GameListPlace;
import com.gochromium.nes.client.ui.GameListView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GameListActivity extends AbstractActivity implements GameListView.Presenter {

	
	private final ClientFactory clientFactory;
	private GameListView view;
	private EventBus eventBus;
	private final GameListPlace place;
	
	private List<Game> gameList = null;

	public GameListActivity(GameListPlace place, ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		this.place = place;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		this.view = clientFactory.getGameListView();
		this.view.setPresenter(this);
		panel.setWidget(view.asWidget());
		this.eventBus = eventBus;
		
		//bind to eventBus
		bind();
		
		//load the Games
		drawGameList();
	}
	
	void bind() {
		//add search handler
		this.eventBus.addHandler(SearchEvent.TYPE, new SearchEventHandler(){
			@Override
			public void onSearch(SearchEvent event) {
				drawGameList(gameList, event.getSearchText(), event.getFilterText());
			}
		});
	}
	
	public void drawGameList() {
		clientFactory.getGameDao().getGameList(new AsyncCallback<List<Game>>(){

			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(List<Game> result) {
				gameList = result;
				drawGameList(gameList, place.getSearchText(), place.getGenreText());

				//If no games, display getting started message
				view.displayGettingStarted(gameList.isEmpty());
			}
		});
	}
	
	public void drawGameList(List<Game> games, String searchText, String tags) {
		
		ArrayList<Game> gameListToDraw = new ArrayList<Game>();
		String filter = searchText.trim().toUpperCase();
		
		//TODO: no need for loop & search if filter is null or empty
		
		//iterate through list and find games that match search criteria
		for(Game game : games) {
			if(assertTextMatch(game.getSearchableName(),filter) &&
					assertTextMatch(game.getTags(),tags)) {
				gameListToDraw.add(game);
			}
		}
		
		//render games on the view
		view.renderGameList(gameListToDraw);
	}
	
	boolean assertTextMatch(String text, String searchString) {
		return searchString.isEmpty() || (text != null && text.indexOf(searchString)>-1);
	}

	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);	
	}

	@Override
	public void onGameClicked(String gameId) {
		clientFactory.getGameDao().getGameCartridge(gameId, new AsyncCallback<byte[]>(){

			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(byte[] result) {
				view.loadGameCartridge(result);
			}
		});
	}
}
