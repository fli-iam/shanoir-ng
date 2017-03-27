package org.shanoir.ng.shared.model;

import java.io.Serializable;

public interface Identifiable<T extends Serializable> {
    T getId();
}
