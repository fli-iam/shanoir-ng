package org.shanoir.uploader.action.init;

import org.shanoir.uploader.gui.ShUpStartupDialog;

public class SelectProfileManualConfigurationState  implements State {

	public void load(StartupStateContext context) {
		ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
		shUpStartupDialog.showSelectProfileForm();
		context.setState(new AuthenticationConfigurationState());
	}

}
