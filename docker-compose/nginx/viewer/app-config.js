window.config = {
  routerBasename: '/',
  modes: [],
  extensions: [],
  showStudyList: true,
  // below flag is for performance reasons, but it might not work for all servers
  showWarningMessageForCrossOrigin: true,
  strictZSpacingForVolumeViewport: true,
  showCPUFallbackMessage: true,
  defaultDataSourceName: 'dicomweb',
  filterQueryParam: false,
  disableServersCache: false,
  dataSources: [
    {
      namespace: '@ohif/extension-default.dataSourcesModule.dicomweb',
      sourceName: 'dicomweb',
      configuration: {
        friendlyName: 'DCM4CHEE Server',
        name: 'SHANOIR-NG',
        wadoUriRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/shanoir-ng/dcm4chee-arc/aets/DCM4CHEE/wado',
        qidoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/dicomweb/dcm4chee-arc/aets/DCM4CHEE/rs',
        wadoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/dicomweb/dcm4chee-arc/aets/DCM4CHEE/rs',
        qidoSupportsIncludeField: false,
        supportsReject: true,
        imageRendering: 'wadors',
        thumbnailRendering: 'wadors',
        enableStudyLazyLoad: true,
        supportsFuzzyMatching: true,
        supportsWildcard: true,
        omitQuotationForMultipartRequest: true,
      },
    },
  ],
  httpErrorHandler: (e) => {
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
    }
  ],
  defaultDataSourceName: 'dicomweb',
};