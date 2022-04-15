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

package org.shanoir.uploader.action.init;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.ShUpStartupDialog;


/**
 * This class is the context class as defined in the "state design pattern".
 * It knows every thing about the current state and is able to trigger an 
 * action link to the current state.
 * 
 * This class has 2 observers :
 * 	- ShanoirStartupController
 *  - ShanoirStartupDialog
 *  
 * @author atouboul
 *  
 */
public class StartupStateContext {
	
	private static Logger logger = Logger.getLogger(StartupStateContext.class);

	private State state;
	
	private ShUpStartupDialog shUpStartupDialog;

	public StartupStateContext() {
		setState(new InitialStartupState());
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		logger.info("ShanoirUploader startup state changed to:  " + state.toString());
		this.state = state;
	}
	
	public void nextState(){
		try {
			getState().load(this);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public ShUpStartupDialog getShUpStartupDialog() {
		return shUpStartupDialog;
	}
	
	public void setShUpStartupDialog(ShUpStartupDialog shUpStartupDialog) {
		this.shUpStartupDialog = shUpStartupDialog;
	}

}
