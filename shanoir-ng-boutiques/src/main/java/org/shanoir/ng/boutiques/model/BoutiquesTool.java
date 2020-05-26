package org.shanoir.ng.boutiques.model;

public class BoutiquesTool {

    private final String id;
    private final String name;
    private final String description;
    private final int nDownloads;

    public BoutiquesTool(String id, String name, String description, int nDownloads) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.nDownloads = nDownloads;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getNDownloads() {
        return nDownloads;
    }
}
