package org.shanoir.ng.controller.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.shanoir.ng.configuration.security.SecurityConfiguration;
import org.shanoir.ng.configuration.security.jwt.extractor.TokenExtractor;
import org.shanoir.ng.configuration.security.jwt.token.AccessJwtToken;
import org.shanoir.ng.configuration.security.jwt.token.JwtSettings;
import org.shanoir.ng.configuration.security.jwt.token.JwtTokenFactory;
import org.shanoir.ng.configuration.security.jwt.token.RefreshToken;
import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.exception.InvalidJwtTokenException;
import org.shanoir.ng.exception.RestServiceException;
import org.shanoir.ng.exception.ShanoirAuthenticationException;
import org.shanoir.ng.exception.error.ErrorModel;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user authentication.
 *
 * @author msimon
 *
 */
@RestController
public class AuthenticationController {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtSettings jwtSettings;

    @Autowired
    private TokenExtractor tokenExtractor;
    
	@Autowired
    private JwtTokenFactory tokenFactory;
    
	@Autowired
	private UserService userService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	@ResponseBody
	public UserDTO authenticate(@RequestBody final LoginDTO loginDTO, final HttpServletResponse response)
			throws RestServiceException {
		final UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(loginDTO.getLogin(), loginDTO.getPassword());
        Authentication authentication = null;
        try {
        	authentication = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
        	LOG.error("Error while authenticating", e);
        	int errorCode = ErrorModelCode.BAD_CREDENTIALS;
        	if (e instanceof ShanoirAuthenticationException) {
        		errorCode = ((ShanoirAuthenticationException) e).getErrorCode();
        	}
        	throw new RestServiceException(new ErrorModel(401, "" + errorCode, null));
        }
		
        final UserContext userContext = (UserContext) authentication.getPrincipal();
        final List<String> authorities = new ArrayList<String>();
		if (userContext.getAuthorities() != null) {
			for (GrantedAuthority authority : userContext.getAuthorities()) {
				authorities.add(authority.getAuthority());
			}
		}
        
		final UserDTO userDTO = new UserDTO();
        userDTO.setAuthorities(authorities);
        userDTO.setRefreshToken(tokenFactory.createRefreshToken(userContext).getToken());
        userDTO.setToken(tokenFactory.createAccessJwtToken(userContext).getToken());
        userDTO.setTokenExpirationTime(jwtSettings.getTokenExpirationTime());
        userDTO.setUsername(userContext.getUsername());
        
        return userDTO;
	}

	@RequestMapping(value = "/authenticate/token", method = RequestMethod.POST)
	@ResponseBody
	public UserDTO refreshToken(final HttpServletRequest request, final HttpServletResponse response)
			throws RestServiceException {
		final String tokenPayload = null;//tokenExtractor.extract(request.getHeader(SecurityConfiguration.JWT_TOKEN_HEADER_PARAM));
        
		final AccessJwtToken rawToken = new AccessJwtToken(tokenPayload);
		final RefreshToken refreshToken = RefreshToken.create(rawToken, jwtSettings.getTokenSigningKey()).orElseThrow(() -> new InvalidJwtTokenException());

		final String subject = refreshToken.getSubject();
		final User user = userService.findByUsername(subject).orElseThrow(() -> new UsernameNotFoundException("User not found: " + subject));

		final UserContext userContext = new UserContext();
        userContext.setUsername(user.getUsername());
        userContext.setAuthorities((List<GrantedAuthority>) user.getAuthorities());

        final UserDTO userDTO = new UserDTO();
        userDTO.setRefreshToken(tokenFactory.createRefreshToken(userContext).getToken());
        userDTO.setToken(tokenFactory.createAccessJwtToken(userContext).getToken());
        userDTO.setTokenExpirationTime(jwtSettings.getTokenExpirationTime());
        
        return userDTO;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(final HttpSession session) {
		session.invalidate();
	}

}
