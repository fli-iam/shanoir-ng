package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Channel {
	@JsonProperty("name")
	private String name;
	@JsonProperty("resolution")
	private float resolution;
	@JsonProperty("referenceUnits")
	private String referenceUnits;
	@JsonProperty("lowCutoff")
	private int lowCutoff = 0;
	@JsonProperty("highCutoff")
	private int highCutoff = 0;
	@JsonProperty("notch")
	private float notch = 0;
	@JsonProperty("x")
	private float x;
	@JsonProperty("y")
	private float y;
	@JsonProperty("z")
	private float z;
	
	// default constructor for jackson purpose.
	public Channel() {
	}
	
	public Channel(String name, float resolution, String referenceUnit) {
		super();
		this.name = name;
		this.resolution = resolution;
		this.referenceUnits = referenceUnit;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getResolution() {
		return resolution;
	}
	public void setResolution(float resolution) {
		this.resolution = resolution;
	}
	public String getReferenceUnits() {
		return referenceUnits;
	}
	public void setReferenceUnits(String referenceUnit) {
		this.referenceUnits = referenceUnit;
	}
	public int getLowCutoff() {
		return lowCutoff;
	}
	public void setLowCutoff(int lowCutoff) {
		this.lowCutoff = lowCutoff;
	}
	public int getHighCutoff() {
		return highCutoff;
	}
	public void setHighCutoff(int highCutoff) {
		this.highCutoff = highCutoff;
	}
	public float getNotch() {
		return notch;
	}
	public void setNotch(float notch) {
		this.notch = notch;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	
}