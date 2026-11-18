// VIDEPE runtime configuration for Shanoir (replaces VIDEPE's default app-config.js)
window.videpeConfig = {
  shanoir: {
    authority: 'SHANOIR_KEYCLOAK_URL/realms/shanoir-ng', // Keycloak realm
    clientId: 'videpe-viewer', // public client from step 5
    apiBase: '/shanoir-ng/datasets', // datasets API, proxied on the viewer host (step 4)
  },
};