package com.gochromium.nes.client;

import com.gochromium.nes.client.place.GameListPlace;
import com.gochromium.nes.client.place.GameSettingsPlace;
import com.gochromium.nes.client.place.GameUploadPlace;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({ 
	GameListPlace.Tokenizer.class,
	GameSettingsPlace.Tokenizer.class,
	GameUploadPlace.Tokenizer.class})
public interface ApplicationPlaceHistoryMapper extends PlaceHistoryMapper {

}
