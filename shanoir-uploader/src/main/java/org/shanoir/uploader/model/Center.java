package org.shanoir.uploader.model;

import java.util.List;

public class Center implements Comparable<Center> {

	private Long id;

	private String name;

	private List<Investigator> investigators;

	public Center() {
	}
	
	public Center(Long id, String name) {
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

	public List<Investigator> getInvestigatorList() {
		return investigators;
	}

	public void setInvestigatorList(List<Investigator> investigatorList) {
		this.investigators = investigatorList;
	}

	public String toString() {
		return this.getName();
	}

	public int compareTo(Center o) {
		return Long.compare(this.getId(), o.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Center other = (Center) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
