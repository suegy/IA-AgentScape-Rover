package rover;

import java.io.Serializable;


public class ScanItem implements Serializable {


	private static final long serialVersionUID = 1L;

	//things that can be sensed on the scanner
	public static final int RESOURCE = 1;
	public static final int ROVER = 2;
	public static final int BASE = 3;

	
	private int itemType;
	
	private double xOffset;
	private double yOffset;
	
	
	public ScanItem(int itemType, double xOffset, double yOffset) {
		this.itemType = itemType;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	public ScanItem(String s) {
		String[] bits = s.split(",");
		
		if(bits[0].equals("resource")) {
			itemType = RESOURCE;
		} else if(bits[0].equals("rover")) {
			itemType = ROVER;
		} else {
			itemType = BASE;
		}
		
		xOffset = Double.parseDouble(bits[1]);
		yOffset = Double.parseDouble(bits[2]);
	}
	
	public int getItemType() {
		return itemType;
	}
	public void setItemType(int itemType) {
		this.itemType = itemType;
	}
	public double getxOffset() {
		return xOffset;
	}
	public void setxOffset(double xOffset) {
		this.xOffset = xOffset;
	}
	public double getyOffset() {
		return yOffset;
	}
	public void setyOffset(double yOffset) {
		this.yOffset = yOffset;
	}
	
	@Override
	public String toString() {
		String type;
		if(itemType == 1) {
			type = "resource";
		} else if(itemType == 2) {
			type = "rover";
		} else {
			type = "base";
		}
		
		return type + "," + xOffset + "," + yOffset;
	}
	
	
}