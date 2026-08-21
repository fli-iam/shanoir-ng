/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
