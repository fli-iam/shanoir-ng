package org.shanoir.uploader.action.init;

import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectProfileManualConfigurationState  implements State {

    @Autowired
    private AuthenticationConfigurationState authenticationConfigurationState;

    public void load(StartupStateContext context) {
        ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
        shUpStartupDialog.showSelectProfileForm();
        context.setState(authenticationConfigurationState);
    }

}
