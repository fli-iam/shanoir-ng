package org.shanoir.uploader.action.init;


import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.service.rest.ServiceConfiguration;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This concrete state class defines the state when the ShanoirUploader :
 *         - Initialize the proxy environment variable if enabled
 *         - tests the connection to remote shanoir server on using the proxy
 *
 * As a result, the context will change either to :
 *         - a manual proxy configuration
 *         - step into the next state in case of success.
 *
 * @author atouboul
 * @author mkain
 *
 */
@Component
public class ProxyConfigurationState implements State {

    private static final Logger logger = LoggerFactory.getLogger(ProxyConfigurationState.class);

    @Autowired
    private AuthenticationConfigurationState authenticationConfigurationState;

    @Autowired
    private ProxyManualConfigurationState proxyManualConfigurationState;

    public void load(StartupStateContext context) {
        String testURL = ServiceConfiguration.getInstance().getTestURL();
        int httpResponseCode = 0;
        try {
            httpResponseCode = ShanoirUploaderServiceClient.testProxy(testURL);
        } catch (Exception e) {
            logger.error("Error during proxy test:", e);
        }
        logger.info("Proxy test returned following code: " + httpResponseCode);
        switch (httpResponseCode) {
            case 200 :
                context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.proxy.success"));
                context.setState(authenticationConfigurationState);
                context.nextState();
                break;
            default:
                context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.proxy.fail"));
                context.setState(proxyManualConfigurationState);
                context.nextState();
                break;
        }
    }

}
