package com.gochromium.nes.client.activity;

import com.gochromium.nes.client.ClientFactory;
import com.gochromium.nes.client.model.Game;
import com.gochromium.nes.client.model.MetaData;
import com.gochromium.nes.client.place.GameListPlace;
import com.gochromium.nes.client.place.GameUploadPlace;
import com.gochromium.nes.client.ui.GameUploadView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GameUploadActivity extends AbstractActivity implements GameUploadView.Presenter {

	private static final String NES_EXTENSION = ".nes";
	private static final String ERROR_LOADING_ROM = "Error Loading ROM";
	private static final String ERROR_INVALID_ROM = "Invalid File type. " +
			"Please upload a valid, unzipped ROM file with a .nes extension";
	private static final String ERROR_STORING_ROM = "Error Storing ROM";
	
	private ClientFactory clientFactory;
	private GameUploadView view;
	@SuppressWarnings("unused")
	private EventBus eventBus;
	

	public GameUploadActivity(GameUploadPlace place, ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		this.view = clientFactory.getGameUploadView();
		this.view.setPresenter(this);
		this.view.clearMessages();
		panel.setWidget(view.asWidget());
		this.eventBus = eventBus;
	}

	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);	
	}

	@Override
	public void onLoadFile(String fileName, byte[] contents) {

		//create default game
		Game game = new Game();
		game.setId(fileName);
		game.setName(fileName);
		
		//make sure it is a valid file type
		if(!assertValidFileName(fileName)) {
			view.addFailureMessage(game, ERROR_INVALID_ROM);
			return;
		}

		//get game meta data (image, name, tags, etc)
		MetaData metaData =
			this.clientFactory.getMetaDataService().findMetaData(fileName);
		
		if(metaData!=null) {
			game.setName(metaData.getName());
			game.setImage(metaData.getImage());
			game.setTags(metaData.getTags());
		}
		
		this.clientFactory.getGameDao().insertGame(
				game, contents, insertGameCallback);
		
	}
	
	boolean assertValidFileName(String fileName) {
		return fileName != null &&
			fileName.toLowerCase().endsWith(NES_EXTENSION);
	}

	@Override
	public void onLoadError(String message) {
		view.addFailureMessage(null, ERROR_LOADING_ROM);
	}
	
	private AsyncCallback<Game> insertGameCallback = new AsyncCallback<Game>(){
		@Override
		public void onFailure(Throwable caught) {
			view.addFailureMessage(null, ERROR_STORING_ROM + ": " + caught.getMessage());
		}

		@Override
		public void onSuccess(Game result) {
			//Print message saying game was successfully inserted
			view.addSuccessMessage(result);
		}
	};
}
