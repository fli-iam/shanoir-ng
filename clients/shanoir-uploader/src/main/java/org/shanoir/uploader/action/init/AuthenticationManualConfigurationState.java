package org.shanoir.uploader.action.init;

import org.shanoir.uploader.gui.ShUpStartupDialog;

public class AuthenticationManualConfigurationState implements State {

	public void load(StartupStateContext context) {
		ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
		shUpStartupDialog.showLoginForm();
		context.setState(new AuthenticationConfigurationState());
	}
	
}
