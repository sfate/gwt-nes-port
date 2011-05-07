package com.gochromium.nes.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class GameUploadPlace extends Place {

	private String token = null;
	
	public GameUploadPlace() {
		this("");
	}

	public GameUploadPlace(String token) {
		this.token = token;
	}

	public static class Tokenizer implements PlaceTokenizer<GameUploadPlace> {

		@Override
		public String getToken(GameUploadPlace place) {
			return place.token;
		}

		@Override
		public GameUploadPlace getPlace(String token) {
			return new GameUploadPlace(token);
		}

	}
}
