package org.shanoir.ng.shared.exception;

public class CheckedIllegalClassException extends Exception {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 8069872579377254819L;

    /**
     * <p>Instantiates with the expected type, and actual object.</p>
     *
     * @param expected  the expected type
     * @param actual  the actual object
     * @since 2.1
     */
    public CheckedIllegalClassException(Class expected, Object actual) {
        super(
            "Expected: "
                + safeGetClassName(expected)
                + ", actual: "
                + (actual == null ? "null" : actual.getClass().getName()));
    }

    /**
     * <p>Instantiates with the expected and actual types.</p>
     *
     * @param expected  the expected type
     * @param actual  the actual type
     */
    public CheckedIllegalClassException(Class expected, Class actual) {
        super(
            "Expected: "
                + safeGetClassName(expected)
                + ", actual: "
                + safeGetClassName(actual));
    }

    /**
     * <p>Instantiates with the specified message.</p>
     *
     * @param message  the exception message
     */
    public CheckedIllegalClassException(String message) {
        super(message);
    }

    /**
     * <p>Returns the class name or <code>null</code> if the class is
     * <code>null</code>.</p>
     *
     * @param cls  a <code>Class</code>
     * @return the name of <code>cls</code>, or <code>null</code> if if <code>cls</code> is <code>null</code>.
     */
    private static final String safeGetClassName(Class cls) {
        return cls == null ? null : cls.getName();
    }

}
