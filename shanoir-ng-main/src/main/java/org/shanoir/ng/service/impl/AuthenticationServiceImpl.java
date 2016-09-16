package org.shanoir.ng.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Authentication service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	@Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

	@Override
	public UserDTO authenticate(final LoginDTO loginDTO, final HttpServletResponse response) {
		final UsernamePasswordAuthenticationToken authenticationToken = 
				new UsernamePasswordAuthenticationToken(loginDTO.getLogin(), loginDTO.getPassword());
        Authentication authentication = null;
        try {
        	authentication = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
        	LOG.error("Error while authenticating", e);
        	return null;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Retrieve security user after authentication
        final User securityUser = (User) userDetailsService.loadUserByUsername(loginDTO.getLogin());

        //Parse Granted authorities to a list of string authorities
        final List<String> authorities = new ArrayList<>();
        for(GrantedAuthority authority : securityUser.getAuthorities()){
            authorities.add(authority.getAuthority());
        }

        //Get Hmac signed token
//        Map<String,String> customClaims = new HashMap<>();
//        customClaims.put(HmacSigner.ENCODING_CLAIM_PROPERTY, HmacUtils.HMAC_SHA_256);

        //Generate a random secret
//        String secret = HmacSigner.generateSecret();

//        HmacToken hmacToken = HmacSigner.getSignedToken(secret,String.valueOf(securityUser.getId()), HmacSecurityFilter.JWT_TTL,customClaims);

//        for(UserDTO userDTO : MockUsers.getUsers()){
//            if(userDTO.getId().equals(securityUser.getId())){
//                userDTO.setSecretKey(secret);
//            }
//        }

        //Set all tokens in http response headers
//        response.setHeader(HmacUtils.X_TOKEN_ACCESS, hmacToken.getJwt());
//        response.setHeader(HmacUtils.X_SECRET, hmacToken.getSecret());
//        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, HmacUtils.HMAC_SHA_256);

        final UserDTO userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setLogin(securityUser.getUsername());
        userDTO.setAuthorities(authorities);
        
        return userDTO;
	}

    @Override
    public void logout() {
    	SecurityContextHolder.clearContext();
    }

}
