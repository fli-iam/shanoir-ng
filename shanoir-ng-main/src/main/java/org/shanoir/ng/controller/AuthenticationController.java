package org.shanoir.ng.controller;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user authentication.
 * 
 * @author msimon
 *
 */
@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	@ResponseBody
	public UserDTO authenticate(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        return authenticationService.authenticate(loginDTO, response);
	}
	
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public void logout(){
        authenticationService.logout();
    }
    
}
