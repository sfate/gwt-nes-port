package com.gochromium.nes.client.model;

public class Game {

	private String id;
	private String name;
	private String image;
	private String searchableName;
	private String tags;

	public Game() {
		
	}

	public Game(String id, String name, String image) {
		super();
		this.id = id;
		this.name = name;
		this.image = image;
		this.searchableName = (name!=null)?name.trim().toUpperCase():"";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.searchableName = (name!=null)?name.trim().toUpperCase():"";
	}

	public String getImage() {
		return image;
	}

	public String getSearchableName() {
		return searchableName;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
}
