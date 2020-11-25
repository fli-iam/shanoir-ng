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
