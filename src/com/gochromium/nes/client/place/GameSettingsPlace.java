package com.gochromium.nes.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class GameSettingsPlace extends Place {

	private String token = null;
	
	public GameSettingsPlace() {
		this("");
	}

	public GameSettingsPlace(String token) {
		this.token = token;
	}

	public static class Tokenizer implements PlaceTokenizer<GameSettingsPlace> {

		@Override
		public String getToken(GameSettingsPlace place) {
			return place.token;
		}

		@Override
		public GameSettingsPlace getPlace(String token) {
			return new GameSettingsPlace(token);
		}

	}
}
