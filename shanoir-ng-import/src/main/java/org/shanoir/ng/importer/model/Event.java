package org.shanoir.ng.importer.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {
	// <type>,[<description>],
	// <position>,<points>,<channel number>,[<date>]
    @JsonProperty("type")
	private String type;
    @JsonProperty("description")
	private String description;
    @JsonProperty("position")
	private String position;
    @JsonProperty("points")
	private int points;
    @JsonProperty("channelNumber")
	private int channelNumber;
    @JsonProperty("date")
	private Date date;
    
    // default constructor for jackson purpose
    public Event() {
    }
	
	public Event(String type, String description, String position, int points, int channelNumber, Date date) {
		super();
		this.type = type;
		this.description = description;
		this.position = position;
		this.points = points;
		this.channelNumber = channelNumber;
		this.date = date;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public int getChannelNumber() {
		return channelNumber;
	}
	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}