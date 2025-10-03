/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

window.config = { 
	routerBasename: '/', 
	extensions: [], 
	modes: [],
	experimentalStudyBrowserSort: true,
	showStudyList: true,
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
				omitQuotationForMultipartRequest: false, 
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
