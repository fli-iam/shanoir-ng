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

package org.shanoir.challengeScores.utils;

/**
 * @author jlouis
 */
public class Triplet<T, U, V> {

	private T a;
	private U b;
	private V c;

	/**
	 * Constructor
	 *
	 * @param a
	 * @param b
	 * @param c
	 */
	public Triplet(T a, U b, V c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public int hashCode() {
		return (a == null ? 0 : a.hashCode())
			 ^ (b == null ? 0 : b.hashCode())
			 ^ (c == null ? 0 : c.hashCode());
	};

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Triplet<?, ?, ?>)) {
			return false;
		} else {
			Triplet triplet = (Triplet) obj;
			return this.a.equals(triplet.getA())
					&& this.b.equals(triplet.getB())
					&& this.c.equals(triplet.getC());
		}
	};

	/**
	 * @return the a
	 */
	public T getA() {
		return a;
	}

	/**
	 * @param a the a to set
	 */
	public void setA(T a) {
		this.a = a;
	}

	/**
	 * @return the b
	 */
	public U getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(U b) {
		this.b = b;
	}

	/**
	 * @return the c
	 */
	public V getC() {
		return c;
	}

	/**
	 * @param c the c to set
	 */
	public void setC(V c) {
		this.c = c;
	}
}
