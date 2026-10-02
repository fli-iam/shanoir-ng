
import {
  Component,
  ElementRef,
  HostListener,
  Inject,
  OnInit,
  Renderer2,
  ViewChild,
  ViewEncapsulation,
  DOCUMENT,
  ChangeDetectionStrategy
} from '@angular/core';
import { SlicePipe, TitleCasePipe } from '@angular/common';

import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { ImagesUrlUtil } from "../shared/utils/images-url.util";
import { StudyType } from "../studies/shared/study-type.enum";
import { StudyLight } from "../studies/shared/study.dto";
import { StudyService } from "../studies/shared/study.service";
import { UserService } from "../users/shared/user.service";
import { DatasetService } from "../datasets/shared/dataset.service";
import * as AppUtils from "../utils/app.utils";
import { isDarkColor } from "../utils/app.utils";

@Component({
    selector: 'app-welcome',
    templateUrl: './welcome.component.html',
    styleUrls: ['./welcome.component.css'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [SlicePipe, TitleCasePipe]
})
export class WelcomeComponent implements OnInit {

    private static readonly JSON_LD_SCRIPT_ID: string = 'shanoir-json-ld';

    public contactMail: string = "mailto:" + AppUtils.SHANOIR_CONTACT_EMAIL;
    public githubLogoUrl: string = ImagesUrlUtil.GITHUB_WHITE_LOGO_PATH;
	public shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
	public publicStudies: StudyLight[] = [];
    public usersCount: number = 0;
    public eventsCount: number = 0;
    public studiesCount: number = 0;
    public datasetAcquisitionsCount: number = 0;
    public subjectsCount: number = 0;
    public storageSize: number = 0;
	public StudyType = StudyType;
	public show: number = 10;

  public welcomeIntroduction: string = AppUtils.FRONTEND_WELCOME_INTRODUCTION;
	@ViewChild('showMore', { static: false }) showMore: ElementRef<HTMLElement>;

	constructor(
		private studyService: StudyService,
        private userService: UserService,
        private datasetService: DatasetService,
        private _renderer2: Renderer2,
        private confirmDialogService: ConfirmDialogService,
        @Inject(DOCUMENT) private _document: Document
	) { }

	ngOnInit(): void {
        this.fetchOverallStats();
    }

    /**
     * Adds a JSON-LD description of the platform and its public studies (Bioschemas DataCatalog /
     * Dataset profiles), read by FAIR evaluators such as FAIR-Checker. Built as an object and
     * serialized with JSON.stringify, so that it is always valid JSON whatever the studies contain.
     */
    addSchemaToDOM(): void {
        const isTerabyte = this.storageSize >= 1000;
        const storageValue = isTerabyte ? (this.storageSize / 1000).toFixed(2) : this.storageSize.toFixed(2);
        const unitCode = isTerabyte ? 'E33' : 'E34';   // E33 = Terabyte, E34 = Gigabyte (codes UN/CEFACT)
        const unitText = isTerabyte ? 'Terabyte' : 'Gigabyte';

        const shanoirUrl: string = window.location.protocol + "//" + window.location.hostname;
        // IRIs of the nodes that only exist in this description (dimensions, metrics, measurements)
        const localIri = (name: string): string => shanoirUrl + '/shanoir-ng/welcome#' + name;
        const fliIri = 'https://www.francelifeimaging.fr';
        const inriaIri = 'https://inria.fr';
        const softwareLicenseIri = 'https://www.gnu.org/licenses/gpl-3.0.en.html';
        // Access policy: public metadata, data access granted by the study admins
        const accessRights = { '@id': 'http://publications.europa.eu/resource/authority/access-right/RESTRICTED' };
        const rights = 'Metadata of public studies are openly available. Access to the data requires a Shanoir account '
            + 'and the approval of the study managers, requested with the "Request an access" button of the study.';
        // license given as absolute IRI, otherwise kept as text
        const licenseValue = (license: string): string | { '@id': string } =>
            /^[a-z][a-z0-9+.-]*:\S+$/i.test(license.trim()) ? { '@id': license.trim() } : license;

        const datasets: Record<string, unknown>[] = (this.publicStudies ?? []).filter(study => study != null).map(study => {
            const studyUrl: string = shanoirUrl + '/shanoir-ng/study/details/' + study.id;
            const dataset: Record<string, unknown> = {
                '@id': studyUrl,
                '@type': ['schema:Dataset', 'dcat:Dataset'],
                'dct:conformsTo': { '@id': 'https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14' },
                'schema:identifier': studyUrl,
                'dct:identifier': studyUrl,
                'schema:url': studyUrl,
                'schema:name': study.name,
                'dct:title': study.name,
                'dct:accessRights': accessRights,
                'dct:rights': rights
            };
            if (study.description) {
                dataset['schema:description'] = study.description;
                dataset['dct:description'] = study.description;
            }
            if (study.license) {
                dataset['schema:license'] = study.license;
                dataset['dct:license'] = licenseValue(study.license);
            }
            const keywords: string[] = (study.studyTags ?? []).filter(tag => tag?.name).map(tag => tag.name);
            if (keywords.length > 0) {
                dataset['schema:keywords'] = keywords;
            }
            return dataset;
        });
        const datasetRefs = datasets.map(dataset => ({ '@id': dataset['@id'] }));

        const titles = [
            { '@value': 'Shanoir - Sharing in vivo imaging resources', '@language': 'en' },
            { '@value': 'Shanoir - Base de données de recherche en imagerie in vivo', '@language': 'fr' }
        ];

        const catalog = {
            '@id': shanoirUrl,
            '@type': ['schema:DataCatalog', 'dcat:Catalog'],
            'dct:conformsTo': { '@id': 'https://bioschemas.org/profiles/DataCatalog/0.3-RELEASE-2019_07_01' },
            'schema:identifier': shanoirUrl,
            'dct:identifier': shanoirUrl,
            'schema:name': 'Shanoir - Sharing in vivo imaging resources',
            'schema:description': 'Shanoir-NG (SHAring NeurOImaging Resources, Next Generation) is a web platform (open-source) for clinical and preclinical research, designed to import, share, archive, search and visualize all kind of medical imaging data (BIDS, MR, CT, PT, EEG, Bruker). Its origin goes back to neuroimaging, but its usage is now open for all kind of organs. It provides a user-friendly, secure web access and offers an intuitive workflow to facilitate the collecting and retrieving of imaging data from multiple sources and a wizzard to make the completion of metadata easy. Shanoir-NG comes along with many features such as pseudonymization of data for all imports, automatic NIfTI conversion and support for multi-centres clinical studies.',
            'schema:url': shanoirUrl,
            'schema:keywords': ['Medical Imaging', 'Neuroimaging', 'Neuroinformatics', 'MRI', 'Research', 'DICOM', 'BIDS', 'Data Sharing'],
            'schema:license': softwareLicenseIri,
            'dct:license': { '@id': softwareLicenseIri },
            'dct:accessRights': accessRights,
            'dct:rights': rights,
            'dct:language': { '@id': 'http://id.loc.gov/vocabulary/iso639-1/en' },
            'dct:title': titles,
            'rdfs:label': titles,
            'schema:provider': [{ '@id': fliIri }, { '@id': inriaIri }],
            'dct:creator': [{ '@id': fliIri }],
            'dct:publisher': [{ '@id': inriaIri }],
            'schema:dataset': datasetRefs,
            'dcat:dataset': datasetRefs
        };

        const organizations = [
            {
                '@id': fliIri,
                '@type': 'schema:Organization',
                'dct:conformsTo': { '@id': 'https://bioschemas.org/profiles/Organization/0.2-DRAFT-2019_07_19' },
                'schema:description': 'France Life Imaging (FLI) is a harmonized imaging network for biomedical research giving access to innovative or even unique imaging equipment systems and to a methodological expertise in all imaging fields to researchers, from public research organisations and industries.',
                'schema:legalName': 'FRANCE LIFE IMAGING',
                'schema:sameAs': fliIri,
                'schema:topic': "Réseau français pour l'imagerie médicale",
                'schema:name': 'France Life Imaging',
                'schema:url': fliIri
            },
            {
                '@id': inriaIri,
                '@type': 'schema:Organization',
                'dct:conformsTo': { '@id': 'https://bioschemas.org/profiles/Organization/0.2-DRAFT-2019_07_19' },
                'schema:description': 'Inria - National Institute for Research in Digital Science and Technology',
                'schema:legalName': 'INSTITUT NATIONAL DE RECHERCHE EN INFORMATIQUE ET EN AUTOMATIQUE (INRIA)',
                'schema:sameAs': 'https://www.wikidata.org/wiki/Q1146208',
                'schema:topic': 'Recherche en informatique',
                'schema:name': 'Inria',
                'schema:url': inriaIri
            }
        ];

        // Quality measurements: one dimension, one metric and one measurement per platform public statistic
        const statistics = [
            { key: 'users', label: 'users', value: this.usersCount,
                dimension: 'Total number of users registered on the platform.',
                metric: 'Count all active users accounts present in Shanoir database.' },
            { key: 'events', label: 'events', value: this.eventsCount,
                dimension: 'Total number of events generated by users on the platform.',
                metric: 'Count all events generated by users on the platform during the last 30 days.' },
            { key: 'datasets', label: 'datasets', value: this.studiesCount,
                dimension: 'Total number of datasets hosted on the platform.',
                metric: 'Count all datasets hosted on the platform under the Shanoir term Studies.' },
            { key: 'public-datasets', label: 'public datasets', value: this.publicStudies?.length ?? 0,
                dimension: 'Total number of public datasets hosted on the platform.',
                metric: 'Count all the publicly accessible datasets among all the datasets hosted on the platform under the Shanoir term Studies.' },
            { key: 'subjects', label: 'subjects', value: this.subjectsCount,
                dimension: 'Total number of subjects belonging to datasets on the platform.',
                metric: 'Count all subjects belonging to datasets and hosted on the platform under the Shanoir term Subjects.' },
            { key: 'images', label: 'images', value: this.datasetAcquisitionsCount,
                dimension: 'Total number of DICOM series belonging to subjects on the platform.',
                metric: 'Count all DICOM series belonging to subjects and hosted on the platform under the Shanoir term Dataset Acquisition.' }
        ];
        const qualityNodes: Record<string, unknown>[] = [];
        for (const statistic of statistics) {
            qualityNodes.push(
                {
                    '@id': localIri(statistic.key + '-dimension'),
                    '@type': 'dqv:Dimension',
                    'skos:prefLabel': 'Number of ' + statistic.label,
                    'skos:definition': statistic.dimension
                },
                {
                    '@id': localIri(statistic.key + '-metric'),
                    '@type': 'dqv:Metric',
                    'dqv:inDimension': { '@id': localIri(statistic.key + '-dimension') },
                    'skos:prefLabel': 'Count of all ' + statistic.label,
                    'skos:definition': statistic.metric
                },
                {
                    '@id': localIri(statistic.key + '-measurement'),
                    '@type': 'dqv:QualityMeasurement',
                    'dqv:computedOn': { '@id': shanoirUrl },
                    'dqv:isMeasurementOf': { '@id': localIri(statistic.key + '-metric') },
                    'dqv:value': { '@value': String(statistic.value ?? 0), '@type': 'xsd:integer' }
                }
            );
        }
        qualityNodes.push(
            {
                '@id': localIri('storage-volume-dimension'),
                '@type': 'dqv:Dimension',
                'skos:prefLabel': 'Data storage volume',
                'skos:definition': 'Total data storage volume used to store all datasets on the platform.'
            },
            {
                '@id': localIri('storage-volume-metric'),
                '@type': 'dqv:Metric',
                'dqv:inDimension': { '@id': localIri('storage-volume-dimension') },
                'skos:prefLabel': 'Total data storage volume',
                'skos:definition': 'Total data storage volume used to store all datasets on the platform.'
            },
            {
                '@id': localIri('storage-volume-measurement'),
                '@type': ['dqv:QualityMeasurement', 'schema:QuantitativeValue'],
                'skos:prefLabel': 'Total data storage volume',
                'dqv:computedOn': { '@id': shanoirUrl },
                'dqv:isMeasurementOf': { '@id': localIri('storage-volume-metric') },
                'schema:value': { '@value': storageValue, '@type': 'xsd:decimal' },
                'schema:unitCode': unitCode,
                'schema:unitText': unitText
            }
        );

        const jsonLd = {
            '@context': {
                'schema': 'https://schema.org/',
                'dcat': 'http://www.w3.org/ns/dcat#',
                'dct': 'http://purl.org/dc/terms/',
                'dqv': 'http://www.w3.org/ns/dqv#',
                'rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
                'skos': 'http://www.w3.org/2004/02/skos/core#',
                'xsd': 'http://www.w3.org/2001/XMLSchema#'
            },
            '@graph': [catalog, ...datasets, ...organizations, ...qualityNodes]
        };

        // replace the description if it was already added (e.g. when coming back to this page)
        this._document.getElementById(WelcomeComponent.JSON_LD_SCRIPT_ID)?.remove();
        const script = this._renderer2.createElement('script');
        script.id = WelcomeComponent.JSON_LD_SCRIPT_ID;
        script.type = 'application/ld+json';
        script.text = JSON.stringify(jsonLd, null, 2);
        this._renderer2.appendChild(this._document.head, script);
    }

    private fetchOverallStats() {
        // get public studies data
        const publicStudiesPromise: Promise<void> = this.fetchPublicStudies();
        // get the latest overall statistics
        this.datasetService.getOverallStatistics().then(stats => {
            this.studiesCount = stats.studiesCount;
            this.subjectsCount = stats.subjectsCount;
            this.datasetAcquisitionsCount = stats.datasetAcquisitionsCount;
            this.storageSize = stats.storageSize;
            // the public studies are part of the JSON-LD description, wait for them too
            Promise.all([this.fetchUsersCount(), this.fetchEventsCount(), publicStudiesPromise])
                .then(() => this.addSchemaToDOM());
        });
    }

    private fetchUsersCount(): Promise<void> {
        //count all users
        return this.userService.countAllUsers().then(count => {
            this.usersCount = count;
        });
    }

    private fetchEventsCount(): Promise<void> {
        // count all users events during last month
        return this.userService.countLastMonthEvents().then(count => {
            this.eventsCount = count;
        });
    }

    get formattedStorageSize(): string {
        if (this.storageSize >= 1000) {
            return (this.storageSize / 1000).toFixed(2) + ' TB';
        }
        return this.storageSize.toFixed(2) + ' GB';
    }

	private fetchPublicStudies(): Promise<void> {
        // get public studies
		return this.studyService.getPublicStudiesData().then(studies => {
			// sort by nbExaminations
			this.publicStudies = studies?.sort((a, b) => {
				// To order by dates :
				// return new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
				return (b.nbExaminations) - (a.nbExaminations);
			})
		});
	}

	increaseShow() {
		this.show += 10;
	}

	login(): void {
		window.location.href = AppUtils.LOGIN_REDIRECT_URL;
	}

	toGithub(): void {
		const url = 'https://github.com/fli-iam/shanoir-ng';
		window.open(url, '_blank');
	}

	toShanoir(): void {
		const url = 'https://project.inria.fr/shanoir/';
		window.open(url, '_blank');
	}

	accessRequest(study: any): void {
        this.confirmDialogService.choose('Do you already have a Shanoir account ?', null, {yes: 'Yes, log in', no: 'No, request an account', cancel: 'Cancel'})
        .then(choice => {
            if (choice == 'yes') {
                window.location.href = window.location.protocol + "//" + window.location.hostname + "/shanoir-ng/access-request/study/" + study.id;
            } else if (choice == 'no') {
                window.location.href = window.location.protocol + "//" + window.location.hostname + "/shanoir-ng/account/study/" + study.id + "/account-request?study=" + study.name + "&function=consumer";
            }
        });
	}

	getFontColor(colorInp: string): boolean {
		return isDarkColor(colorInp);
	}

	@HostListener('window:scroll', ['$event']) onWindowScroll(e) {
		const scroll = e.target['scrollingElement'].scrollTop + window.innerHeight;
		const end = this.showMore?.nativeElement?.offsetTop;
		if (scroll > end && this.publicStudies.length > this.show) this.increaseShow();
	}
}
