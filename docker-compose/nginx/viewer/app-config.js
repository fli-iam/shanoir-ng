window.config = {
  routerBasename: '/ohif-viewer/',
  modes: [],
  extensions: [],
  showStudyList: true,
  // below flag is for performance reasons, but it might not work for all servers
  showWarningMessageForCrossOrigin: true,
  strictZSpacingForVolumeViewport: true,
  showCPUFallbackMessage: true,
  showLoadingIndicator: true,
  defaultDataSourceName: 'dicomweb',
  dataSources: [
    {
      namespace: '@ohif/extension-default.dataSourcesModule.dicomweb',
      sourceName: 'dicomweb',
      configuration: {
        friendlyName: 'DCM4CHEE Server',
        name: 'SHANOIR-NG',
        wadoUriRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/pacs',
        qidoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/pacs',
        wadoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/pacs',
        qidoSupportsIncludeField: false,
        imageRendering: 'wadors',
        thumbnailRendering: 'wadors',
		dicomUploadEnabled: true,
		omitQuotationForMultipartRequest: true,
      },
    },
  ],
  httpErrorHandler: (e) => {
    console.log("test"),
    console.warn(e.status), 
	console.warn("test, navigate to https://ohif.org/")
  },
  oidc: [
    {
      authority: 'SHANOIR_URL_SCHEME://SHANOIR_URL_HOST/realms/shanoir-ng',
      client_id: 'ohif-viewer',
      redirect_uri: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/callback',
      response_type: 'code',
      scope: 'openid',
      post_logout_redirect_uri: 'SHANOIR_URL_SCHEME://SHANOIR_URL_HOST',
	  revoke_uri: 'https://accounts.google.com/o/oauth2/revoke?token=',
      automaticSilentRenew: true,
      revokeAccessTokenOnSignout: true,
	  useAuthorizationCodeFlow: true,
    }
  ],
};