//package org.shanoir.ng.user.security;
//
//import java.util.Map;
//
//import org.keycloak.KeycloakPrincipal;
//import org.keycloak.representations.AccessToken;
//import org.shanoir.ng.shared.exception.TokenNotFoundException;
//import org.shanoir.ng.user.model.User;
//import org.shanoir.ng.user.service.UserService;
//import org.shanoir.ng.utils.KeycloakUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserFieldsSecurityService {
//	
//	@Autowired
//	private UserService userService;
//		
//    public boolean isMe(Long id) {
//    	if (id == null) throw new IllegalArgumentException("id cannot be null");
//    	else if (isAnonymousConnected()) return false;
//    	else return id.equals(getConnectedUserId());
//    }
//    
//}