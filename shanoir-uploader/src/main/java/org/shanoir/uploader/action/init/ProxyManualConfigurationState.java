package org.shanoir.uploader.action.init;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.ShUpStartupDialog;

/**
 * This class initializes the ShUpStartupDialog for the first time as required here at first.
 * It already sets the state back to ProxyConfigurationState to test the config given after
 * and continue.
 * 
 * @author mkain
 *
 */
public class ProxyManualConfigurationState implements State {
	
	private static Logger logger = Logger.getLogger(ProxyManualConfigurationState.class);
	
	public void load(StartupStateContext context) {
		ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
		shUpStartupDialog.showProxyForm();
		// set the next state here, what means that in the ProxyPanelActionListener
		// only nextState will have to be called without knowing what is the next state
		context.setState(new ProxyConfigurationState());
	}

}
