package org.shanoir.uploader.action.init;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.ShUpStartupDialog;

public class AuthenticationManualConfigurationState implements State {

	public void load(StartupStateContext context) {
		ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
		shUpStartupDialog.updateStartupText("\n"+ ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.login.fail"));
		shUpStartupDialog.showLoginForm();
		context.setState(new AuthenticationConfigurationState());
	}
	
}
