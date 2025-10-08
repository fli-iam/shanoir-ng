window.config = { 
	routerBasename: null,
	extensions: [], 
	modes: [],
	experimentalStudyBrowserSort: true,
	showStudyList: false,
	dataSources: [ 
		{ 
			namespace: '@ohif/extension-default.dataSourcesModule.dicomweb', 
			sourceName: 'dicomweb', 
			configuration: { 
				friendlyName: 'SHANOIR-NG',
				name: 'SHANOIR-NG', 
				wadoUriRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/shanoir-ng/',
				qidoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/dicomweb',
				wadoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/dicomweb',
				qidoSupportsIncludeField: true, 
				supportsReject: true, 
				imageRendering: 'wadors', 
				thumbnailRendering: 'wadors', 
				enableStudyLazyLoad: true, 
				supportsFuzzyMatching: true, 
				supportsWildcard: true, 
				omitQuotationForMultipartRequest: false 
			}, 
		}, 
	], 
	defaultDataSourceName: 'dicomweb', 
	oidc: [
  	  {
    	// ~ REQUIRED
    	// Authorization Server URL
    	authority: 'SHANOIR_URL_SCHEME://SHANOIR_URL_HOST/auth/realms/shanoir-ng',
    	client_id: 'ohif-viewer',
    	redirect_uri: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/callback', // `OHIFStandaloneViewer.js`
    	// "Authorization Code Flow"
		// Resource: https://medium.com/@darutk/diagrams-of-all-the-openid-connect-flows-6968e3990660
		response_type: 'code',
		scope: 'openid', // email profile openid
		// ~ OPTIONAL
		post_logout_redirect_uri: 'SHANOIR_URL_SCHEME://SHANOIR_URL_HOST'
    }
  ]
};
