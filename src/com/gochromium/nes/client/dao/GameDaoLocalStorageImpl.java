package com.gochromium.nes.client.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.gochromium.nes.client.html5.LocalStorage;
import com.gochromium.nes.client.model.Game;
import com.gochromium.nes.client.model.GameJso;
import com.gochromium.nes.client.util.Base64Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GameDaoLocalStorageImpl implements GameDao {

	@Override
	public void getGameList(AsyncCallback<List<Game>> callback) {
		List<Game> games = new ArrayList<Game>();
		

		try {
			
			int length = LocalStorage.length();
			
			for(int i=0;i<length;i++){
				String key = LocalStorage.key(i);
				if(!key.startsWith("SETTINGS_"))
					games.add(getGame(key));
			}
		
			Collections.sort(games, new Comparator<Game>(){
				@Override
				public int compare(Game arg0, Game arg1) {
					return arg0.getName().compareTo(arg1.getName());
				}
			});
			
			callback.onSuccess(games);
			
		}catch(Exception ex) {
			callback.onFailure(ex);
		}
	}

	@Override
	public void insertGame(Game game, byte[] cartridge, AsyncCallback<Game> callback) {
		
		GameJso gameDTO = GameJso.create();

		gameDTO.setName(game.getName());
		gameDTO.setImage(game.getImage());
		gameDTO.setTags(game.getTags());
		gameDTO.setCartridge( Base64Util.encodeLines(cartridge) );

		try {
			LocalStorage.setItem(game.getName(),gameDTO.toJSON());
			callback.onSuccess(game);
		}catch(IOException ex) {
			callback.onFailure(ex);
		}
	}
	
	@Override
	public void deleteGame(String name, AsyncCallback<String> callback) {
		try {
			LocalStorage.removeItem(name);
			callback.onSuccess(name);
		} catch (IOException ex) {
			callback.onFailure(ex);
		}
	}
		
	public void getGameCartridge(String name, AsyncCallback<byte[]> callback) {
		try {
			String jsonString = LocalStorage.getItem(name);
			GameJso gameDTO = GameJso.fromJSON(jsonString);
	
			byte[] gameBytes = Base64Util.decodeLines(gameDTO.getCartridge());
			callback.onSuccess(gameBytes);
			
		} catch(IOException ex) {
			callback.onFailure(ex);
		}
	}

	protected Game getGame(String name) {
		
		try {
			String jsonString = LocalStorage.getItem(name);
			
			GameJso gameDTO = GameJso.fromJSON(jsonString);
			
			
			
			Game game = new Game();
			game.setImage(gameDTO.getImage());
			game.setId(gameDTO.getName());
			game.setName(gameDTO.getName());
			game.setTags(gameDTO.getTags());
			

	
			return game;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
