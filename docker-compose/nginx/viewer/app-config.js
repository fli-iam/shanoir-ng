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

// Shim Node-only globals that leak into a lazily loaded OHIF 3.12 browser
// chunk (ReferenceError: __filename is not defined -> black screen on the first
// load). Defining them on the global object lets the bare references resolved.
// Safe to remove if the upstream ohif/app image stops referencing them.
window.__filename = window.__filename || '';
window.__dirname = window.__dirname || '/';

window.config = {
	routerBasename: null,
	extensions: [],
	modes: [],
	experimentalStudyBrowserSort: true,
	showStudyList: false,
	customizationService: [
		{
			// Show only the examination the viewer was opened with:
			'studyBrowser.studyMode': {
				$set: 'primary',
			},
		},
	],
	maxNumRequests: {
		interaction: SHANOIR_VIEWER_OHIF_INTERACTION_NUM_REQUESTS,
		thumbnail: SHANOIR_VIEWER_OHIF_THUMBNAIL_NUM_REQUESTS,
		prefetch: SHANOIR_VIEWER_OHIF_PREFETCH_NUM_REQUESTS,
	},
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
	onnxConfig: {
		wasmPaths: '/ort/',
	},
	oidc: [
  	  {
    	// ~ REQUIRED
    	// Authorization Server URL
    	authority: 'SHANOIR_KEYCLOAK_URL/realms/shanoir-ng',
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

// Interim OHIF 3.12.5 segmentation export guards (scenarios A/B in
// docs/runbooks/OHIF-segmentation-export-failures.md). Remove when upstream OHIF
// preloads referenced source images and blocks export with a pending AI preview.
(function installShanoirSegExportShim() {
	const EXPORT_COMMANDS = new Set(['storeSegmentation', 'downloadSegmentation']);
	const POLL_MS = 250;

	function tryInstall() {
		if (window.__shanoirSegExportShimInstalled) {
			return true;
		}

		const commandsManager = window.commandsManager;
		const cornerstone = window.cornerstone;
		const cornerstoneTools = window.cornerstoneTools;
		const services = window.services;

		if (
			!commandsManager?.runCommand ||
			!cornerstone?.cache ||
			!cornerstone?.imageLoader ||
			!cornerstoneTools?.segmentation?.state?.getSegmentations ||
			!services?.segmentationService
		) {
			return false;
		}

		const originalRunCommand = commandsManager.runCommand.bind(commandsManager);
		let guardDepth = 0;
		let exportPreparePromise = null;

		function collectReferencedSourceImageIds() {
			const { cache } = window.cornerstone;
			const segs = cornerstoneTools.segmentation.state.getSegmentations();
			const refs = new Set();

			for (const seg of segs) {
				const labelmapIds = seg?.representationData?.Labelmap?.imageIds;
				if (!labelmapIds) {
					continue;
				}
				for (const id of labelmapIds) {
					const lm = cache.getImage(id);
					if (lm?.referencedImageId) {
						refs.add(lm.referencedImageId);
					}
				}
			}

			return refs;
		}

		async function ensureSourceImagesLoaded() {
			const { cache, imageLoader } = window.cornerstone;
			const refs = collectReferencedSourceImageIds();
			if (!refs.size) {
				return true;
			}

			const ui = services.uiNotificationService;
			let slowNotifyTimer = setTimeout(() => {
				ui?.show?.({
					title: 'Loading source images',
					message:
						'Loading the full image series before export. This may take a moment.',
					type: 'info',
					duration: 8000,
				});
			}, 1000);

			try {
				// Load every referenced source frame (OHIF generateSegmentation maps all
				// labelmap slices to cache.getImage(referencedImageId) synchronously).
				await Promise.all([...refs].map(ref => imageLoader.loadAndCacheImage(ref)));
			} finally {
				clearTimeout(slowNotifyTimer);
			}

			const stillMissing = [...refs].filter(ref => !cache.getImage(ref));
			if (stillMissing.length) {
				console.warn(
					'[shanoir] segmentation export: source images still not in cache after preload:',
					stillMissing.length
				);
				ui?.show?.({
					title: 'Cannot export segmentation',
					message:
						stillMissing.length +
						' source image(s) are not loaded yet. Scroll through the full series, wait for images to load, then try again.',
					type: 'error',
					duration: 15000,
				});
				return false;
			}

			console.info(
				'[shanoir] segmentation export: preloaded',
				refs.size,
				'referenced source image(s)'
			);
			return true;
		}

		function prepareExport() {
			if (!exportPreparePromise) {
				exportPreparePromise = ensureSourceImagesLoaded().finally(() => {
					exportPreparePromise = null;
				});
			}
			return exportPreparePromise;
		}

		function findUnregisteredVoxelValues() {
			const { cache } = window.cornerstone;
			const segs = cornerstoneTools.segmentation.state.getSegmentations();
			const unregistered = new Set();

			for (const seg of segs) {
				const labelmapIds = seg?.representationData?.Labelmap?.imageIds;
				if (!labelmapIds) {
					continue;
				}
				const registered = new Set(
					Object.keys(
						services.segmentationService.getSegmentation(seg.segmentationId).segments || {}
					).map(Number)
				);
				const voxelValues = new Set();
				for (const id of labelmapIds) {
					const lm = cache.getImage(id);
					if (!lm) {
						continue;
					}
					const px = lm.getPixelData();
					for (let i = 0; i < px.length; i++) {
						if (px[i]) {
							voxelValues.add(px[i]);
						}
					}
				}
				for (const v of voxelValues) {
					if (!registered.has(v)) {
						unregistered.add(v);
					}
				}
			}

			return [...unregistered];
		}

		async function runExportCommand(commandName, options, ...rest) {
			const badValues = findUnregisteredVoxelValues();
			if (badValues.length) {
				services.uiNotificationService?.show?.({
					title: 'Cannot export segmentation',
					message:
						'Accept the AI result before exporting: press Enter and wait until the page responds (about 30 seconds), or press Esc to reject. Unregistered label values: ' +
						badValues.join(', '),
					type: 'warning',
					duration: 15000,
				});
				return;
			}

			if (guardDepth === 0) {
				const ready = await prepareExport();
				if (!ready) {
					return;
				}
			}

			guardDepth++;
			try {
				const result = originalRunCommand(commandName, options, ...rest);
				return result instanceof Promise ? await result : result;
			} finally {
				guardDepth--;
			}
		}

		commandsManager.runCommand = function shanoirWrappedRunCommand(commandName, options, ...rest) {
			if (!EXPORT_COMMANDS.has(commandName)) {
				return originalRunCommand(commandName, options, ...rest);
			}
			return runExportCommand(commandName, options, ...rest);
		};

		window.__shanoirSegExportShimInstalled = true;
		console.info('[shanoir] OHIF segmentation export shim installed');
		return true;
	}

	function poll() {
		if (tryInstall()) {
			return;
		}
		setTimeout(poll, POLL_MS);
	}

	poll();
})();
