package org.shanoir.uploader.model.rest;

public class Investigator implements Comparable<Investigator>{
	
	private Long id;
	
	private String name;

	public Investigator(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int compareTo(Investigator o) {
		return Long.compare(this.getId(), o.getId());
	}
	
	public String toString() {
		return this.getName();
	}

}
