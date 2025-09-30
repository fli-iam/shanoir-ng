package org.shanoir.ng.user.service;

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.model.vip.CountryCode;
import org.shanoir.ng.user.model.vip.VIPUser;
import org.shanoir.ng.user.model.vip.VIPUserLevel;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author Alae ES-SAKI
 */
@Component
@ConditionalOnProperty(name = "vip.enabled", havingValue = "true")
public class VIPUserServiceImpl implements VIPUserService {
    private static final String INRIA_ADMIN_GENERATED = "inria_admin_generated";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(VIPUserServiceImpl.class);

    @Value("${vip.uri}")
    private String vipUri;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;



    @Override
    public User createVIPAccountRequest(final User user) throws SecurityException, MicroServiceCommunicationException, PasswordPolicyException {
        AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();

        /* Password generation */
        final String newPassword = PasswordUtils.generatePassword();
        if (!PasswordUtils.checkPasswordPolicy(newPassword)) {
            throw new PasswordPolicyException();
        }

        // vip user info;
        String[] accountType = new String[] {"Support"};
        String comments = "";
        VIPUserLevel userLevel = VIPUserLevel.Beginner;
        CountryCode countryCode = CountryCode.fr;
        String institution = user.getAccountRequestInfo() == null ? INRIA_ADMIN_GENERATED : user.getAccountRequestInfo().getInstitution();

        VIPUser vipUser = new VIPUser(user.getFirstName(), user.getLastName(), user.getEmail(), institution, newPassword, userLevel, countryCode, comments, accountType);

        // prepare entity.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenResponse.getToken());
        HttpEntity entity = new HttpEntity<>(vipUser, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(this.vipUri, HttpMethod.POST, entity, Void.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOG.error("Could not communicate with VIP instance to create user. Http response: ", response.getStatusCode());
            }
            return user;
        } catch (Exception e) {
            // Do not fail when an error occures
            LOG.error("Could not communicate with VIP instance to create user", e);
            return user;
        }
    }
}
