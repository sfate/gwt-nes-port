package com.gochromium.nes.client.model;

public interface Controller {

	public void setA(int a);
	public void setB(int b);
	public void setSelect(int select);
	public void setStart(int start);
	public void setUp(int up);
	public void setDown(int down);
	public void setLeft(int left);
	public void setRight(int right);
	
	public int getA();
	public int getB();
	public int getSelect();
	public int getStart();
	public int getUp();
	public int getDown();
	public int getLeft();
	public int getRight();
	
	public String toJSON();

}
