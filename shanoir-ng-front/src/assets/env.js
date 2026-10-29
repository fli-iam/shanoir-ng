(function(window) {
  window["env"] = window["env"] || {};
  // Environment variables
    window["env"]["keycloakUrl"] = "SHANOIR_KEYCLOAK_URL";
    window["env"]["dpoMail"] = "SHANOIR_DPO_EMAIL";
    window["env"]["contactMail"] = "SHANOIR_CONTACT_EMAIL";
    window["env"]["ohifUrl"] = "SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST";
    window["env"]["videpeUrl"] = "SHANOIR_VIEWER_VIDEPE_URL_SCHEME://SHANOIR_VIEWER_VIDEPE_URL_HOST";
    window["env"]["vipUrl"] = "VIP_URL_SCHEME://VIP_URL_HOST";
})(this);
