package org.shanoir.uploader.action.init;

/**
 * This class defines the interface State.
 * 
 * The concretes states implementing classes are :
 *  {@link InitialStartupState}
 *  {@link ProxyConfigurationState}
 *  {@link ProxyManualConfigurationState}
 * 	{@link AuthenticationConfigurationState}
 *  {@link AuthenticationManualConfigurationState}
 *  {@link WSDLConfigurationState}
 *  {@link PacsConfigurationState}
 *  {@link PacsManualConfigurationState}
 *  {@link ServerUnreachableState}
 *  {@link ReadyState}
 * 
 *  Any question on this implementation : please refer to the design pattern State.
 * 
 */
public interface State {

	public void load(StartupStateContext context) throws Exception;

}