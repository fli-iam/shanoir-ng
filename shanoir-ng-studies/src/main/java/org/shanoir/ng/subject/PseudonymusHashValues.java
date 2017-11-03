package org.shanoir.ng.subject;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@Entity
@Table(name="pseudonymus_hash_values")
@JsonPropertyOrder({ "_links", "id", "birthNameHash1", "birthNameHash2", "birthNameHash3","lastNameHash1","lastNameHash2","lastNameHash3","firstNameHash1","firstNameHash2","firstNameHash3","birthDateHash", "subject" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class PseudonymusHashValues  extends HalEntity implements Serializable {
	
	/**
	 * UID
	 */
	private static final long serialVersionUID = 3108595543203629662L;

	/** Subject. */
	@OneToOne
	@JsonIgnore
	@JoinColumn(name = "id", referencedColumnName = "id", unique = true, nullable = false, updatable = true)
	private Subject subject;
	
	private String birthNameHash1;

	private String birthNameHash2;

	private String birthNameHash3;

	private String lastNameHash1;

	private String lastNameHash2;

	private String lastNameHash3;

	private String firstNameHash1;

	private String firstNameHash2;

	private String firstNameHash3;

	private String birthDateHash;
	

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final PseudonymusHashValues other) {
		if (lastNameHash1 != null && other.lastNameHash1 != null) {
			return lastNameHash1.compareTo(other.lastNameHash1);
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PseudonymusHashValues other = (PseudonymusHashValues) obj;
		if (birthNameHash1 == null) {
			if (other.birthNameHash1 != null) {
				return false;
			}
		} else if (!birthNameHash1.equals(other.birthNameHash1)) {
			return false;
		}
		if (birthNameHash2 == null) {
			if (other.birthNameHash2 != null) {
				return false;
			}
		} else if (!birthNameHash2.equals(other.birthNameHash2)) {
			return false;
		}
		if (birthNameHash3 == null) {
			if (other.birthNameHash3 != null) {
				return false;
			}
		} else if (!birthNameHash3.equals(other.birthNameHash3)) {
			return false;
		}
		if (lastNameHash1 == null) {
			if (other.lastNameHash1 != null) {
				return false;
			}
		} else if (!lastNameHash1.equals(other.lastNameHash1)) {
			return false;
		}
		if (lastNameHash2 == null) {
			if (other.lastNameHash2 != null) {
				return false;
			}
		} else if (!lastNameHash2.equals(other.lastNameHash2)) {
			return false;
		}
		if (lastNameHash3 == null) {
			if (other.lastNameHash3 != null) {
				return false;
			}
		} else if (!lastNameHash3.equals(other.lastNameHash3)) {
			return false;
		}
		if (firstNameHash1 == null) {
			if (other.firstNameHash1 != null) {
				return false;
			}
		} else if (!firstNameHash1.equals(other.firstNameHash1)) {
			return false;
		}
		if (firstNameHash2 == null) {
			if (other.firstNameHash2 != null) {
				return false;
			}
		} else if (!firstNameHash2.equals(other.firstNameHash2)) {
			return false;
		}
		if (firstNameHash3 == null) {
			if (other.firstNameHash3 != null) {
				return false;
			}
		} else if (!firstNameHash3.equals(other.firstNameHash3)) {
			return false;
		}
		if (birthDateHash == null) {
			if (other.birthDateHash != null) {
				return false;
			}
		} else if (!birthDateHash.equals(other.birthDateHash)) {
			return false;
		}		
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((birthNameHash1 == null) ? 0 : birthNameHash1.hashCode());
		result = prime * result + ((birthNameHash2 == null) ? 0 : birthNameHash2.hashCode());
		result = prime * result + ((birthNameHash3 == null) ? 0 : birthNameHash3.hashCode());
		result = prime * result + ((lastNameHash1 == null) ? 0 : lastNameHash1.hashCode());
		result = prime * result + ((lastNameHash2 == null) ? 0 : lastNameHash2.hashCode());
		result = prime * result + ((lastNameHash3 == null) ? 0 : lastNameHash3.hashCode());
		result = prime * result + ((firstNameHash1 == null) ? 0 : firstNameHash1.hashCode());
		result = prime * result + ((firstNameHash2 == null) ? 0 : firstNameHash2.hashCode());
		result = prime * result + ((firstNameHash3 == null) ? 0 : firstNameHash3.hashCode());
		result = prime * result + ((birthDateHash == null) ? 0 : birthDateHash.hashCode());
		return result;
	}
	
	
	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
	public Long getId() {
		return super.getId();
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getBirthNameHash1() {
		return birthNameHash1;
	}

	public void setBirthNameHash1(String birthNameHash1) {
		this.birthNameHash1 = birthNameHash1;
	}

	public String getBirthNameHash2() {
		return birthNameHash2;
	}

	public void setBirthNameHash2(String birthNameHash2) {
		this.birthNameHash2 = birthNameHash2;
	}

	public String getBirthNameHash3() {
		return birthNameHash3;
	}

	public void setBirthNameHash3(String birthNameHash3) {
		this.birthNameHash3 = birthNameHash3;
	}

	public String getLastNameHash1() {
		return lastNameHash1;
	}

	public void setLastNameHash1(String lastNameHash1) {
		this.lastNameHash1 = lastNameHash1;
	}

	public String getLastNameHash2() {
		return lastNameHash2;
	}

	public void setLastNameHash2(String lastNameHash2) {
		this.lastNameHash2 = lastNameHash2;
	}

	public String getLastNameHash3() {
		return lastNameHash3;
	}

	public void setLastNameHash3(String lastNameHash3) {
		this.lastNameHash3 = lastNameHash3;
	}

	public String getFirstNameHash1() {
		return firstNameHash1;
	}

	public void setFirstNameHash1(String firstNameHash1) {
		this.firstNameHash1 = firstNameHash1;
	}

	public String getFirstNameHash2() {
		return firstNameHash2;
	}

	public void setFirstNameHash2(String firstNameHash2) {
		this.firstNameHash2 = firstNameHash2;
	}

	public String getFirstNameHash3() {
		return firstNameHash3;
	}

	public void setFirstNameHash3(String firstNameHash3) {
		this.firstNameHash3 = firstNameHash3;
	}

	public String getBirthDateHash() {
		return birthDateHash;
	}

	public void setBirthDateHash(String birthDateHash) {
		this.birthDateHash = birthDateHash;
	}

}
