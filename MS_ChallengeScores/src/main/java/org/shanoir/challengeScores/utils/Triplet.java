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
