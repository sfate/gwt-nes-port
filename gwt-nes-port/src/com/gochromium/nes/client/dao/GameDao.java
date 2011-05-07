package com.gochromium.nes.client.dao;

import java.util.List;

import com.gochromium.nes.client.model.Game;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GameDao {

	public void getGameList(AsyncCallback<List<Game>> callback);
	
	public void insertGame(Game game, byte[] cartridge, AsyncCallback<Game> callback);
	
	public void deleteGame(String name, AsyncCallback<String> callback);

	public void getGameCartridge(String name, AsyncCallback<byte[]> callback);
}
