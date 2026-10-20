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
	// Give up after ~60 s so a renamed/removed OHIF global is reported instead of failing silently.
	const MAX_POLL_ATTEMPTS = 240;
	// Upper bound on the export preload: queued pool requests can be dropped without
	// settling (e.g. on mode exit), so never wait on them indefinitely.
	const PRELOAD_TIMEOUT_MS = 120000;

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
		const exportPreparePromises = new Map();

		function getExportSegmentations(segmentationId) {
			const segs = cornerstoneTools.segmentation.state.getSegmentations();
			if (!segmentationId) {
				return segs;
			}
			return segs.filter(seg => seg.segmentationId === segmentationId);
		}

		function getExportLoadConcurrency() {
			const cfg = window.config?.maxNumRequests;
			const raw = cfg?.prefetch ?? cfg?.interaction ?? 6;
			const n = Number(raw);
			return Number.isFinite(n) && n > 0 ? n : 6;
		}

		function collectReferencedSourceImageIds(segmentationId) {
			const { cache } = window.cornerstone;
			const refs = new Set();

			for (const seg of getExportSegmentations(segmentationId)) {
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

		async function preloadReferencedSourceImages(imageLoader, refs) {
			const ids = [...refs];
			if (!ids.length) {
				return;
			}

			const pool = cornerstone.imageLoadPoolManager;
			const concurrency = getExportLoadConcurrency();

			const loadOne = ref =>
				imageLoader.loadAndCacheImage(ref).catch(err => {
					console.warn('[shanoir] segmentation export: failed to load source image', ref, err);
				});

			if (pool?.addRequest) {
				await Promise.allSettled(
					ids.map(
						ref =>
							new Promise((resolve, reject) => {
								const run = () => loadOne(ref).then(resolve, reject);
								try {
									// 'interaction', not 'prefetch': stackPrefetch clears the prefetch
									// stack on every slice change, dropping queued requests unsettled.
									pool.addRequest(run, 'interaction', { imageId: ref }, 0);
								} catch (err) {
									run().then(resolve, reject);
								}
							})
					)
				);
				return;
			}

			for (let i = 0; i < ids.length; i += concurrency) {
				const batch = ids.slice(i, i + concurrency);
				await Promise.allSettled(batch.map(loadOne));
			}
		}

		async function ensureSourceImagesLoaded(segmentationId) {
			const { cache, imageLoader } = window.cornerstone;
			// Only fetch what is not cached yet, so re-entrant or repeated exports are cheap.
			const missing = [...collectReferencedSourceImageIds(segmentationId)].filter(
				ref => !cache.getImage(ref)
			);
			if (!missing.length) {
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

			let preloadTimeoutTimer;
			try {
				// Load every referenced source frame (OHIF generateSegmentation maps all
				// labelmap slices to cache.getImage(referencedImageId) synchronously).
				// On timeout, fall through to the cache check below.
				await Promise.race([
					preloadReferencedSourceImages(imageLoader, missing),
					new Promise(resolve => {
						preloadTimeoutTimer = setTimeout(() => {
							console.warn('[shanoir] segmentation export: source image preload timed out');
							resolve();
						}, PRELOAD_TIMEOUT_MS);
					}),
				]);
			} finally {
				clearTimeout(slowNotifyTimer);
				clearTimeout(preloadTimeoutTimer);
			}

			const stillMissing = missing.filter(ref => !cache.getImage(ref));
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
				missing.length,
				'referenced source image(s)'
			);
			return true;
		}

		function prepareExport(segmentationId) {
			const key = segmentationId ?? '';
			if (!exportPreparePromises.has(key)) {
				const promise = ensureSourceImagesLoaded(segmentationId).finally(() => {
					exportPreparePromises.delete(key);
				});
				exportPreparePromises.set(key, promise);
			}
			return exportPreparePromises.get(key);
		}

		function findUnregisteredVoxelValues(segmentationId) {
			const { cache } = window.cornerstone;

			for (const seg of getExportSegmentations(segmentationId)) {
				const labelmapIds = seg?.representationData?.Labelmap?.imageIds;
				if (!labelmapIds) {
					continue;
				}
				const ohifSeg = services.segmentationService.getSegmentation(seg.segmentationId);
				if (!ohifSeg) {
					// Unknown to OHIF: no segment registry to compare against, so treating every
					// voxel as unregistered would wrongly block the export.
					continue;
				}
				const registered = new Set(Object.keys(ohifSeg.segments || {}).map(Number));
				for (const id of labelmapIds) {
					const lm = cache.getImage(id);
					if (!lm) {
						continue;
					}
					const px = lm.getPixelData();
					for (let i = 0; i < px.length; i++) {
						const v = px[i];
						if (v && !registered.has(v)) {
							return [v];
						}
					}
				}
			}

			return [];
		}

		async function isExportAllowed(segmentationId) {
			const badValues = findUnregisteredVoxelValues(segmentationId);
			if (badValues.length) {
				services.uiNotificationService?.show?.({
					title: 'Cannot export segmentation',
					message:
						'Accept the AI result before exporting: press Enter and wait until the page responds, or press Esc to reject. Unregistered label values: ' +
						badValues.join(', '),
					type: 'warning',
					duration: 15000,
				});
				return false;
			}

			// Always prepare: a shared in-flight counter would let a concurrent export of
			// another segmentation skip its preload.
			return prepareExport(segmentationId);
		}

		async function runExportCommand(commandName, options, ...rest) {
			let allowed;
			try {
				allowed = await isExportAllowed(options?.segmentationId);
			} catch (err) {
				// Fail open: a bug in the shim must never block an export.
				console.warn('[shanoir] segmentation export shim failed, running original command:', err);
				allowed = true;
			}
			if (!allowed) {
				return;
			}

			return originalRunCommand(commandName, options, ...rest);
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

	let pollAttempts = 0;

	function poll() {
		if (tryInstall()) {
			return;
		}
		if (++pollAttempts >= MAX_POLL_ATTEMPTS) {
			console.warn(
				'[shanoir] OHIF segmentation export shim NOT installed: OHIF globals not found after ~60 s (OHIF version changed?)'
			);
			return;
		}
		setTimeout(poll, POLL_MS);
	}

	poll();
})();
