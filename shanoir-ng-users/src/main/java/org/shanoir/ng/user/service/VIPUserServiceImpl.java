package org.shanoir.ng.user.service;

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.model.vip.CountryCode;
import org.shanoir.ng.user.model.vip.VIPUser;
import org.shanoir.ng.user.model.vip.VIPUserLevel;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.utils.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Alae ES-SAKI
 */
@Component
public class VIPUserServiceImpl implements VIPUserService{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(VIPUserServiceImpl.class);

    @Value("${vip.uri}")
    private String vip_uri;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;



    @Override
    public User createVIPAccountRequest(final User user) throws EntityNotFoundException, SecurityException, MicroServiceCommunicationException, PasswordPolicyException {

        // Verify if the user is created in SHANOIR DB
        final boolean userExist = userRepository.existsById(user.getId());
        if (!userExist) {
            LOG.error("User with id {} not found", user.getId());
            throw new EntityNotFoundException(User.class, user.getId());
        }

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
        String institution = user.getAccountRequestInfo() == null ? "inria_admin_generated" : user.getAccountRequestInfo().getInstitution();

        VIPUser vipUser = new VIPUser(user.getFirstName(), user.getLastName(), user.getEmail(), institution, newPassword, userLevel, countryCode, comments, accountType);

        // prepare entity.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenResponse.getToken());
        HttpEntity entity = new HttpEntity(vipUser, headers);

        try {
            ResponseEntity<Void> executionResponseEntity = restTemplate.exchange(this.vip_uri, HttpMethod.POST, entity, Void.class);
            return user;
        }catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            LOG.error("error while saving vip user with status : {} ,and message : {}", e.getStatusCode(), e.getMessage());
            throw new MicroServiceCommunicationException("Error while communicating with VIP with status : "+e.getStatusCode()+ ",and message : "+e.getMessage());
        } catch (RestClientException e) {
            // in case of an error with no response payload
            LOG.error("there is no response payload while saving vip user");
            throw new MicroServiceCommunicationException("Error while communicating with VIP with no response payload while saving vip user");
        }


    }
}
