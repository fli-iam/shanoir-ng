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

import { DOCUMENT } from "@angular/common";
import {
    Component,
    ElementRef,
    HostListener,
    Inject,
    OnInit,
    Renderer2,
    ViewChild,
    ViewEncapsulation
} from '@angular/core';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { ImagesUrlUtil } from "../shared/utils/images-url.util";
import { StudyType } from "../studies/shared/study-type.enum";
import { StudyLight } from "../studies/shared/study.dto";
import { StudyService } from "../studies/shared/study.service";
import * as AppUtils from "../utils/app.utils";
import { isDarkColor } from "../utils/app.utils";

@Component({
    selector: 'app-welcome',
    templateUrl: './welcome.component.html',
    styleUrls: ['./welcome.component.css'],
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class WelcomeComponent implements OnInit {

	public githubLogoUrl: string = ImagesUrlUtil.GITHUB_WHITE_LOGO_PATH;
	public shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
	public email: string = "mailto:developers_shanoir-request@inria.fr";
	public studies: StudyLight[] = [];
	public StudyType = StudyType;
	public show: number = 10;
	@ViewChild('showMore', { static: false }) showMore: ElementRef<HTMLElement>;

	constructor(
		private studyService: StudyService,
        private _renderer2: Renderer2,
        private confirmDialogService: ConfirmDialogService,
        @Inject(DOCUMENT) private _document: Document
	) { }

	ngOnInit(): void {
        this.fetchStudies();
    }

    addSchemaToDOM(): void {
        let script = this._renderer2.createElement('script');
        script.type = `application/ld+json`;

        let datasetStr: string = "";
        let shanoirUrl: string = window.location.protocol + "//" + window.location.hostname;

        this.studies?.forEach( study => {

            // keywords handling
            let keywords: string = "";
            study.studyTags.forEach( tag => {
                if (tag != null) {
                    keywords += "\"" + tag.name + "\"";
                }
                if (tag.id != study.studyTags[study.studyTags.length - 1].id) {
                    keywords += ", ";
                }
            })

            // datasets handling
            if (study != null) {
                datasetStr += `
                    {
                        "@context": "https://schema.org",
                        "@type": "Dataset",
                        "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
                        "url": "` + shanoirUrl + `/shanoir-ng/study/details/` + study.id + `",
                        "identifier": "` + shanoirUrl + `/shanoir-ng/study/details/` + study.id + `",
                        "license": "` + study.license + `",
                        "name": "` + study.name + `",
                        "description": "` + study.description + `",
                        "keywords": [
                            ` + keywords + `
                        ]
                    }`
            }
            if (study != this.studies[this.studies.length - 1]) {
                datasetStr += ",";
            }
        })

        // schema.org DataCatalog + Datasets
        script.text = `
        {
            "@context": "http://schema.org",
            "@id": "` + shanoirUrl + `",
            "@type": "DataCatalog",
            "dct:conformsTo": "https://bioschemas.org/profiles/DataCatalog/0.3-RELEASE-2019_07_01",
            "description": "Shanoir-NG (SHAring NeurOImaging Resources, Next Generation) is a web platform (open-source) for clinical and preclinical research, designed to import, share, archive, search and visualize all kind of medical imaging data (BIDS, MR, CT, PT, EEG, Bruker). Its origin goes back to neuroimaging, but its usage is now open for all kind of organs. It provides a user-friendly, secure web access and offers an intuitive workflow to facilitate the collecting and retrieving of imaging data from multiple sources and a wizzard to make the completion of metadata easy. Shanoir-NG comes along with many features such as pseudonymization of data for all imports, automatic NIfTI conversion and support for multi-centres clinical studies.",
            "keywords": [
                "Medical Imaging",
                "Neuroimaging",
                "Neuroinformatics",
                "MRI",
                "Research"
            ],
            "license": "https://www.gnu.org/licenses/gpl-3.0.en.html",
            "name": "Shanoir",
            "provider": [
                {
                    "@context": "http://schema.org",
                    "@type": "Organization",
                    "dct:conformsTo": "https://bioschemas.org/profiles/Organization/0.2-DRAFT-2019_07_19",
                    "description": "France Life Imaging (FLI) is a harmonized imaging network for biomedical research giving access to innovative or even unique imaging equipment systems and to a methodological expertise in all imaging fields to researchers, from public research organisations and industries.",
                    "legalName": "FRANCE LIFE IMAGING",
                    "sameAs": "https://www.francelifeimaging.fr",
                    "topic": "Réseau français pour l'imagerie médicale",
                    "name": "France Life Imaging",
                    "url": "https://www.francelifeimaging.fr"
                },
                {
                    "@context": "http://schema.org",
                    "@type": "Organization",
                    "dct:conformsTo": "https://bioschemas.org/profiles/Organization/0.2-DRAFT-2019_07_19",
                    "description": "Inria - National Institute for Research in Digital Science and Technology",
                    "legalName": "INSTITUT NATIONAL DE RECHERCHE EN INFORMATIQUE ET EN AUTOMATIQUE (INRIA)",
                    "sameAs": "https://www.wikidata.org/wiki/Q1146208",
                    "topic": "Recherche en informatique",
                    "name": "Inria",
                    "url": "https://inria.fr"
                }
            ],
            "dataset": [`
                + datasetStr + `
            ],

            "url": "` + shanoirUrl + `"
        }`;

        this._renderer2.appendChild(this._document.head, script);
	}

	private fetchStudies() {
		this.studyService.getPublicStudiesData().then(studies => {
			// sort by nbExaminations
			this.studies = studies?.sort((a, b) => {
				// To order by dates :
				// return new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
				return (b.nbExaminations) - (a.nbExaminations);
			})
            this.addSchemaToDOM();
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
		let scroll = e.target['scrollingElement'].scrollTop + window.innerHeight;
		let end = this.showMore?.nativeElement?.offsetTop;
		if (scroll > end && this.studies.length > this.show) this.increaseShow();
	}
}
