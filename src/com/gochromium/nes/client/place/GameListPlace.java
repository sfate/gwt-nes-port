package com.gochromium.nes.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class GameListPlace extends Place {

	private final String searchText;
	private final String genreText;
	
	public GameListPlace() {
		this("","");
	}

	public GameListPlace(String searchText, String genreText) {
		this.searchText = searchText;
		this.genreText = genreText;
	}
	
	public String getSearchText() {
		return searchText;
	}
	
	public String getGenreText() {
		return genreText;
	}

	public static class Tokenizer implements PlaceTokenizer<GameListPlace> {

		@Override
		public String getToken(GameListPlace place) {
			return "";
		}

		@Override
		public GameListPlace getPlace(String token) {
			return new GameListPlace();
		}

	}
}
