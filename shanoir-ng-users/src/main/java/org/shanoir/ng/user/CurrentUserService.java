package org.shanoir.ng.user;

public interface CurrentUserService {

    boolean canAccessUser(Long userId);

}
