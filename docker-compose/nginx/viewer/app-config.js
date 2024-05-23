window.config = {
  routerBasename: '/',
  extensions: [
    {
      id: 'cornerstone',
      getViewportModule: function() {
        return {
          name: 'cornerstone',
          component: 'cornerstoneViewportComponent',
        };
      },
      getSopClassHandlerModule: function() {
        return [
          {
            name: 'cornerstone',
            sopClassUIDs: [
              '1.2.840.10008.5.1.4.1.1.2', // CT Image Storage
              '1.2.840.10008.5.1.4.1.1.4', // MR Image Storage
            ],
          },
        ];
      },
    },
    {
      id: 'dicom-p10-downloader',
    },
  ],
  showStudyList: true,
  filterQueryParam: false,
  disableServersCache: false,
  studyPrefetcher: {
    enabled: true,
    order: 'closest',
    displaySetCount: 3,
    preventCache: false,
    prefetchDisplaySetsTimeout: 300,
    displayProgress: true,
    includeActiveDisplaySet: true,
  },
  servers: {
    dicomWeb: [
      {
        name: 'SHANOIR-NG',
        wadoUriRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/shanoir-ng/',
        qidoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/dicomweb',
        wadoRoot: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/dicomweb',
        qidoSupportsIncludeField: true,
        imageRendering: 'wadors',
        thumbnailRendering: 'wadors',
        enableStudyLazyLoad: true,
        supportsFuzzyMatching: true,
      },
    ],
  },
  oidc: [
    {
      authority: 'SHANOIR_URL_SCHEME://SHANOIR_URL_HOST/auth/realms/shanoir-ng',
      client_id: 'ohif-viewer',
      redirect_uri: 'SHANOIR_VIEWER_OHIF_URL_SCHEME://SHANOIR_VIEWER_OHIF_URL_HOST/callback',
      response_type: 'code',
      scope: 'openid',
      post_logout_redirect_uri: 'SHANOIR_URL_SCHEME://SHANOIR_URL_HOST'
    }
  ],
  hotkeys: [
    { commandName: 'incrementActiveViewport', label: 'Next Viewport', keys: ['right'] },
    { commandName: 'decrementActiveViewport', label: 'Previous Viewport', keys: ['left'] },
    { commandName: 'rotateViewportCW', label: 'Rotate Right', keys: ['r'] },
    { commandName: 'rotateViewportCCW', label: 'Rotate Left', keys: ['l'] },
    { commandName: 'invertViewport', label: 'Invert', keys: ['i'] },
    { commandName: 'flipViewportVertical', label: 'Flip Horizontally', keys: ['h'] },
    { commandName: 'flipViewportHorizontal', label: 'Flip Vertically', keys: ['v'] },
    { commandName: 'scaleUpViewport', label: 'Zoom In', keys: ['+'] },
    { commandName: 'scaleDownViewport', label: 'Zoom Out', keys: ['-'] },
    { commandName: 'fitViewportToWindow', label: 'Zoom to Fit', keys: ['='] },
    { commandName: 'resetViewport', label: 'Reset', keys: ['space'] },
    { commandName: 'nextImage', label: 'Next Image', keys: ['down'] },
    { commandName: 'previousImage', label: 'Previous Image', keys: ['up'] },
    { commandName: 'previousViewportDisplaySet', label: 'Previous Series', keys: ['pagedown'] },
    { commandName: 'nextViewportDisplaySet', label: 'Next Series', keys: ['pageup'] },
    { commandName: 'setZoomTool', label: 'Zoom', keys: ['z'] },
    { commandName: 'windowLevelPreset1', label: 'W/L Preset 1', keys: ['1'] },
    { commandName: 'windowLevelPreset2', label: 'W/L Preset 2', keys: ['2'] },
    { commandName: 'windowLevelPreset3', label: 'W/L Preset 3', keys: ['3'] },
    { commandName: 'windowLevelPreset4', label: 'W/L Preset 4', keys: ['4'] },
    { commandName: 'windowLevelPreset5', label: 'W/L Preset 5', keys: ['5'] },
    { commandName: 'windowLevelPreset6', label: 'W/L Preset 6', keys: ['6'] },
    { commandName: 'windowLevelPreset7', label: 'W/L Preset 7', keys: ['7'] },
    { commandName: 'windowLevelPreset8', label: 'W/L Preset 8', keys: ['8'] },
    { commandName: 'windowLevelPreset9', label: 'W/L Preset 9', keys: ['9'] },
  ],
  cornerstoneExtensionConfig: {},
  modes: [
    {
      id: 'default',
      displayName: 'Viewer Mode',
      isActive: true,
    }
  ],
  sopClassHandlers: [
    {
      sopClassUID: '1.2.840.10008.5.1.4.1.1.2', // CT Image Storage
      name: 'cornerstone',
    },
    {
      sopClassUID: '1.2.840.10008.5.1.4.1.1.4', // MR Image Storage
      name: 'cornerstone',
    },
  ],
  viewports: [
    {
      namespace: 'cornerstone',
      id: 'cornerstone',
      component: 'cornerstoneViewportComponent',
      defaultOptions: {
        orientation: 'axial',
        viewport: 'active',
      },
    },
  ],
  // Leave maxConcurrentMetadataRequests undefined for no limit, suitable for HTTP/2 enabled servers
  // maxConcurrentMetadataRequests: 5,
};
