package com.gochromium.nes.client.model;


public class MetaData {

	String name;
	String image;
	String pattern;
	String tags;
	
	public MetaData(String name) {
		this.name = name;
	}

	public MetaData(String name, String pattern, String image) {
		this(name, pattern, image, null);
	}

	public MetaData(String name, String pattern, String image, String tags) {
		this.name = name;
		this.image = image;
		this.pattern = (pattern==null)?null:pattern.toLowerCase();
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	
}
