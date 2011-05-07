package com.gochromium.nes.client.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.gochromium.nes.client.model.Game;
import com.gochromium.nes.client.model.GameJso;
import com.gochromium.nes.client.util.Base64Util;
import com.google.code.gwt.database.client.Database;
import com.google.code.gwt.database.client.SQLError;
import com.google.code.gwt.database.client.SQLResultSet;
import com.google.code.gwt.database.client.SQLTransaction;
import com.google.code.gwt.database.client.StatementCallback;
import com.google.code.gwt.database.client.TransactionCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;

@Deprecated
public class GameDaoWebDatabaseImpl implements GameDao {

	static Database DB;
	static final String DB_NAME = "NES Database";
	static final String DB_VERSION = "1.0";
	static final int DB_SIZE = 3145728;

	
	static {
		DB = Database.openDatabase(DB_NAME, DB_VERSION, DB_NAME, DB_SIZE); //3 MB

		DB.transaction(new TransactionCallback() {
		    public void onTransactionStart(SQLTransaction tx) {
		        tx.executeSql("CREATE TABLE IF NOT EXISTS Game (key, name, image, tags, cartridge)", null);
		    }
		    public void onTransactionFailure(SQLError error) {
		        // handle error...
		    }
		    public void onTransactionSuccess() {
		        // Proceed when successfully committed...
		    }
		});
	}
	
	
	@Override
	public void getGameList(final AsyncCallback<List<Game>> callback) {
		DB.transaction(new TransactionCallback(){

			@Override
			public void onTransactionStart(SQLTransaction tx) {
				tx.executeSql("select key,name,image,tags from Game", new Object[]{}, new StatementCallback<GameJso>(){

					@Override
					public void onSuccess(SQLTransaction transaction,
							SQLResultSet<GameJso> resultSet) {
						int itemCount = resultSet.getRows().getLength();
						
						List<Game> gameList = new ArrayList<Game>();
						for(int i=0;i<itemCount;i++) {
							GameJso gameJso = resultSet.getRows().getItem(i);
							Game game = new Game();
							game.setId(gameJso.getKey());
							game.setName(gameJso.getName());
							game.setTags(gameJso.getTags());
							game.setImage(gameJso.getImage());
							gameList.add(game);
						}
						
						Collections.sort(gameList, new Comparator<Game>(){
							@Override
							public int compare(Game arg0, Game arg1) {
								return arg0.getName().compareTo(arg1.getName());
							}
						});
						
						callback.onSuccess(gameList);
					}

					@Override
					public boolean onFailure(SQLTransaction transaction,
							SQLError error) {
						callback.onFailure(new RuntimeException(error.getMessage()));
						return false;
					}


				});
			}

			@Override
			public void onTransactionSuccess() {
				
			}

			@Override
			public void onTransactionFailure(SQLError error) {
				callback.onFailure(new RuntimeException(error.getMessage()));
			}
		});
	}

	@Override
	public void insertGame(final Game game, final byte[] cartridge,
			final AsyncCallback<Game> callback) {
		
		DB.transaction(new TransactionCallback() {
		    public void onTransactionStart(SQLTransaction tx) {
//		        tx.executeSql("INSERT INTO Game (key, value) VALUES (?,?)", new Object[] {game.getId(), Base64Util.encodeLines(cartridge)});
		    	tx.executeSql("INSERT INTO Game (key, name, image, tags, cartridge) VALUES (?,?,?,?,?)", 
		        		new Object[]{ game.getId(),game.getName(),game.getImage(),game.getTags(),Base64Util.encodeLines(cartridge) });
		    }
		    public void onTransactionFailure(SQLError error) {
		        callback.onFailure(new RuntimeException(error.getMessage()));
		    }
		    public void onTransactionSuccess() {
		        callback.onSuccess(game);
		    }
		});
	}

	@Override
	public void deleteGame(String name, AsyncCallback<String> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getGameCartridge(final String name, final AsyncCallback<byte[]> callback) {
		DB.transaction(new TransactionCallback(){

			@Override
			public void onTransactionStart(SQLTransaction tx) {
				tx.executeSql("select cartridge from Game where key = ?", new Object[]{name}, new StatementCallback<GameJso>(){

					@Override
					public void onSuccess(SQLTransaction transaction,
							SQLResultSet<GameJso> resultSet) {
						
						//one result should be returned
						if(resultSet.getRows().getLength()!=1) {
							callback.onFailure(new RuntimeException("Cartridge Not Found"));
						}
						
						GameJso gameJso = resultSet.getRows().getItem(0);
						byte[] cartridge = Base64Util.decodeLines(gameJso.getCartridge());
						callback.onSuccess(cartridge);
					}

					@Override
					public boolean onFailure(SQLTransaction transaction,
							SQLError error) {
						callback.onFailure(new RuntimeException(error.getMessage()));
						return false;
					}


				});
			}

			@Override
			public void onTransactionSuccess() {
				
			}

			@Override
			public void onTransactionFailure(SQLError error) {
				callback.onFailure(new RuntimeException(error.getMessage()));
			}
		});
	}



}
