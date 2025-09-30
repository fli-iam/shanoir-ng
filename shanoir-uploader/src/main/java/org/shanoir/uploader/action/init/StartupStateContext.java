package org.shanoir.uploader.action.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.shanoir.uploader.gui.ShUpStartupDialog;


/**
 * This class is the context class as defined in the "state design pattern".
 * It knows every thing about the current state and is able to trigger an
 * action link to the current state.
 *
 * This class has 2 observers :
 *     - ShanoirStartupController
 *  - ShanoirStartupDialog
 *
 * @author atouboul
 *
 */
@Component
public class StartupStateContext {

    private static final Logger LOG = LoggerFactory.getLogger(StartupStateContext.class);

    private State state;

    private ShUpStartupDialog shUpStartupDialog;

    @Autowired
    private InitialStartupState initialStartupState;

    public void configure() {
        setState(initialStartupState);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        LOG.info("ShanoirUploader startup state changed to:  " + state.toString());
        this.state = state;
    }

    public void nextState() {
        try {
            getState().load(this);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public ShUpStartupDialog getShUpStartupDialog() {
        return shUpStartupDialog;
    }

    public void setShUpStartupDialog(ShUpStartupDialog shUpStartupDialog) {
        this.shUpStartupDialog = shUpStartupDialog;
    }

}
