package org.shanoir.uploader.action.init;

/**
 * This concrete state class defines the state when the shanoir uploader tests
 * the connection to the PACS after having failed with previous configuration.
 * 
 * This state is doing the same as the AuthenticationManualConfigurationState.class 
 * (except that the view will display dedicated GUI for entering new pacs configuration).
 *  NOTE : THIS IS NOT IMPLEMENTED YET.
 *
 * As a result, the context will change either to :
 * 		- a Manual Pacs Configuration in case of failure
 * 		- step to the READY state in case of success.
 * 
 * NOTE : Currently this new state is always since GUI implementation is not done.
 * 
 * @author atouboul
 *
 */
public class PacsManualConfigurationState implements State {

	public void load(StartupStateContext context) {
		context.setState(new ReadyState());
		context.nextState();
	}

}