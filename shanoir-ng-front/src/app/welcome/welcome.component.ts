
import {
  Component,
  ElementRef,
  HostListener,
  Inject,
  OnInit,
  Renderer2,
  ViewChild,
  ViewEncapsulation,
  DOCUMENT
} from '@angular/core';

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
  standalone: false
})
export class WelcomeComponent implements OnInit {

  public githubLogoUrl: string = ImagesUrlUtil.GITHUB_WHITE_LOGO_PATH;
  public shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
  public email: string = "mailto:developers_shanoir-request@inria.fr";
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

  addSchemaToDOM(): void {
    const script = this._renderer2.createElement('script');
    script.type = `application/ld+json`;

    let datasetStr: string = "";
    const shanoirUrl: string = window.location.protocol + "//" + window.location.hostname;

    this.publicStudies?.forEach(study => {

      // keywords handling
      let keywords: string = "";
      study.studyTags.forEach(tag => {
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
                        "@id": "` + study.name + `",
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
      if (study != this.publicStudies[this.publicStudies.length - 1]) {
        datasetStr += ",";
      }
    })

    // schema.org DataCatalog + Datasets
    script.text = `
        {
  "@context": {
    "schema": "https://schema.org/",
    "dcat": "http://www.w3.org/ns/dcat#",
    "dct": "http://purl.org/dc/terms/",
    "dcterms": "http://purl.org/dc/terms/",
    "dqv": "http://www.w3.org/ns/dqv#",
    "prov": "http://www.w3.org/ns/prov#",
    "skos": "http://www.w3.org/2004/02/skos/core#",
    "xsd": "http://www.w3.org/2001/XMLSchema#",
    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    "ex": "http://example.org/"
  },
  "@graph": [
    {
      "@id": "https://www.francelifeimaging.fr",
      "@type": "schema:Organization",
      "dct:conformsTo": "https://bioschemas.org/profiles/Organization/0.2-DRAFT-2019_07_19",
      "schema:description": "France Life Imaging (FLI) is a harmonized imaging network for biomedical research giving access to innovative or even unique imaging equipment systems and to a methodological expertise in all imaging fields to researchers, from public research organisations and industries.",
      "schema:legalName": "FRANCE LIFE IMAGING",
      "schema:sameAs": "https://www.francelifeimaging.fr",
      "schema:name": "France Life Imaging",
      "schema:url": "https://www.francelifeimaging.fr"
    },
    {
      "@id": "https://inria.fr",
      "@type": "schema:Organization",
      "dct:conformsTo": "https://bioschemas.org/profiles/Organization/0.2-DRAFT-2019_07_19",
      "schema:description": "Inria - National Institute for Research in Digital Science and Technology",
      "schema:legalName": "Institut National de Recherche en Informatique et en Automatique (INRIA)",
      "schema:sameAs": "https://www.wikidata.org/wiki/Q1146208",
      "schema:name": "Inria",
      "schema:url": "https://inria.fr"
    },
    {
      "@id": "https://shanoir.irisa.fr",
      "@type": [
        "schema:DataCatalog",
        "dcat:Catalog"
      ],
      "dct:conformsTo": "https://bioschemas.org/profiles/DataCatalog/0.3-RELEASE-2019_07_01",
      "schema:name": "Shanoir - Sharing in vivo imaging resources",
      "schema:description": "Shanoir-NG (SHAring NeurOImaging Resources, Next Generation) is a web platform (open-source) for clinical and preclinical research, designed to import, share, archive, search and visualize all kind of medical imaging data (BIDS, MR, CT, PT, EEG, Bruker). Its origin goes back to neuroimaging, but its usage is now open for all kind of organs. It provides a user-friendly, secure web access and offers an intuitive workflow to facilitate the collecting and retrieving of imaging data from multiple sources and a wizzard to make the completion of metadata easy. Shanoir-NG comes along with many features such as pseudonymization of data for all imports, automatic NIfTI conversion and support for multi-centres clinical studies.",
      "schema:url": "https://shanoir.irisa.fr",
      "schema:keywords": [
        "Medical Imaging",
        "Neuroimaging",
        "Neuroinformatics",
        "MRI",
        "Research",
        "DICOM",
        "BIDS",
        "Data Sharing"
      ],
      "schema:license": "https://www.gnu.org/licenses/gpl-3.0.en.html",
      "dct:language": {
        "@id": "http://id.loc.gov/vocabulary/iso639-1/en"
      },
      "dcterms:title": [
        {
          "@value": "Shanoir - Sharing in vivo imaging resources",
          "@language": "en"
        },
        {
          "@value": "Shanoir - Base de données de recherche en imagerie in vivo",
          "@language": "fr"
        }
      ],
      "rdfs:label": [
        {
          "@value": "Shanoir - Sharing in vivo imaging resources",
          "@language": "en"
        },
        {
          "@value": "Shanoir - Base de données de recherche en imagerie in vivo",
          "@language": "fr"
        }
      ],
      "schema:provider": [
        { "@id": "https://www.francelifeimaging.fr" },
        { "@id": "https://inria.fr" }
      ],
      "dct:creator": [
        { "@id": "https://www.francelifeimaging.fr" }
      ],
      "dct:publisher": [
        { "@id": "https://inria.fr" }
      ],
      "schema:dataset": [
        { "@id": "Sharing_SynesthesiaStructuralData" },
        { "@id": "CGen-KG-ICA" },
        { "@id": "ReVis-MD" },
        { "@id": "Sharing_SynesthesiaFunctionalData" },
        { "@id": "MSSEG-2" },
        { "@id": "MSSEG 2016" },
        { "@id": "MS-Multi-Spine" }
      ],
      "dcat:dataset": [
        { "@id": "Sharing_SynesthesiaStructuralData" },
        { "@id": "CGen-KG-ICA" },
        { "@id": "ReVis-MD" },
        { "@id": "Sharing_SynesthesiaFunctionalData" },
        { "@id": "MSSEG-2" },
        { "@id": "MSSEG 2016" },
        { "@id": "MS-Multi-Spine" }
      ],
      "dqv:hasQualityMeasurement": [
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/users/users/count" },
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/users/events/count" },
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/datasets" },
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/studies/studies/public/data" },
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/subjects" },
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/images" },
        { "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/storage" }
      ]
    },
    {
      "@id": "Sharing_SynesthesiaStructuralData",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/134",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/134",
      "schema:name": "Sharing_SynesthesiaStructuralData",
      "schema:keywords": []
    },
    {
      "@id": "CGen-KG-ICA",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/278",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/278",
      "schema:name": "CGen-KG-ICA",
      "schema:description": "Collection of simulated imaging data of intracranial berry aneurysms.",
      "schema:keywords": ["Simulated Data", "Interoperability", "Intracranial Aneurysm"]
    },
    {
      "@id": "ReVis-MD",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/269",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/269",
      "schema:name": "ReVis-MD",
      "schema:keywords": []
    },
    {
      "@id": "Sharing_SynesthesiaFunctionalData",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/140",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/140",
      "schema:name": "Sharing_SynesthesiaFunctionalData",
      "schema:keywords": []
    },
    {
      "@id": "MSSEG-2",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/208",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/208",
      "schema:name": "MSSEG-2",
      "schema:description": "MS new lesions segmentation challenge of MICCAI 2021",
      "schema:keywords": ["100 subjects", "MRI brain images", "NIfTI", "Multiple sclerosis"]
    },
    {
      "@id": "MSSEG 2016",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/209",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/209",
      "schema:name": "MSSEG 2016",
      "schema:description": "MS lesions segmentation challenge of MICCAI 2016",
      "schema:keywords": ["60 subjects", "MRI brain images", "NIfTI", "Multiple sclerosis"]
    },
    {
      "@id": "MS-Multi-Spine",
      "@type": ["schema:Dataset", "dcat:Dataset"],
      "dct:conformsTo": "https://bioschemas.org/profiles/Dataset/0.3-RELEASE-2019_06_14",
      "schema:url": "https://shanoir.irisa.fr/shanoir-ng/study/details/259",
      "dct:identifier": "https://shanoir.irisa.fr/shanoir-ng/study/details/259",
      "schema:name": "MS-Multi-Spine",
      "schema:description": "MS lesion segmentation challenge on spinal cord",
      "schema:keywords": ["Spinal cord", "Multiple sclerosis (MS)", "Lesion segmentation", "200 subjects"]
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/users/users/count",
      "@type": "dqv:QualityMeasurement",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Users" },
      "dqv:inMetric": { "@id": "Users Count" },
      "dqv:value": { "@value": "0", "@type": "xsd:integer" }
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/users/events/count",
      "@type": "dqv:QualityMeasurement",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Events" },
      "dqv:inMetric": { "@id": "Events Count" },
      "dqv:value": { "@value": "0", "@type": "xsd:integer" }
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/datasets",
      "@type": "dqv:QualityMeasurement",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Datasets" },
      "dqv:inMetric": { "@id": "Datasets Count" },
      "dqv:value": { "@value": "278", "@type": "xsd:integer" }
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/studies/studies/public/data",
      "@type": "dqv:QualityMeasurement",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Public Datasets" },
      "dqv:inMetric": { "@id": "Public Datasets Count" },
      "dqv:value": { "@value": "7", "@type": "xsd:integer" }
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/subjects",
      "@type": "dqv:QualityMeasurement",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Subjects" },
      "dqv:inMetric": { "@id": "Subjects Count" },
      "dqv:value": { "@value": "25449", "@type": "xsd:integer" }
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/images",
      "@type": "dqv:QualityMeasurement",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Images" },
      "dqv:inMetric": { "@id": "Images Count" },
      "dqv:value": { "@value": "350322", "@type": "xsd:integer" }
    },
    {
      "@id": "https://shanoir.irisa.fr/shanoir-ng/datasets/datasets/overallStatistics/storage",
      "@type": "schema:QuantitativeValue",
      "skos:prefLabel": "Total data storage volume",
      "dqv:computedOn": { "@id": "https://shanoir.irisa.fr" },
      "dqv:isMeasurementOf": { "@id": "Storage Volume" },
      "schema:value": { "@value": "18609", "@type": "xsd:decimal" },
      "schema:unitCode": "E34",
      "schema:unitText": "Gigabyte"
    },
    {
      "@id": "Users",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Number of users",
      "skos:definition": "Total number of users registered on the platform."
    },
    {
      "@id": "Events",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Number of events",
      "skos:definition": "Total number of events generated by users on the platform."
    },
    {
      "@id": "Datasets",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Number of datasets",
      "skos:definition": "Total number of datasets hosted on the platform."
    },
    {
      "@id": "Public Datasets",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Number of public datasets",
      "skos:definition": "Total number of public datasets hosted on the platform."
    },
    {
      "@id": "Subjects",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Number of subjects",
      "skos:definition": "Total number of subjects belonging to datasets on the platform."
    },
    {
      "@id": "Images",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Number of images",
      "skos:definition": "Total number of DICOM series belonging to subjects on the platform."
    },
    {
      "@id": "Storage Volume",
      "@type": "dqv:Dimension",
      "skos:prefLabel": "Data storage volume",
      "skos:definition": "Total data storage volume used to store all datasets on the platform."
    },
    {
      "@id": "Users Count",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Users" },
      "skos:prefLabel": "Count of all users",
      "skos:definition": "Count all active users accounts present in Shanoir database."
    },
    {
      "@id": "Events Count",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Events" },
      "skos:prefLabel": "Count of all events",
      "skos:definition": "Count all events generated by users on the platform during the last 30 days."
    },
    {
      "@id": "Datasets Count",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Datasets" },
      "skos:prefLabel": "Count of all datasets",
      "skos:definition": "Count all datasets hosted on the platform under the Shanoir term Studies."
    },
    {
      "@id": "Public Datasets Count",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Public Datasets" },
      "skos:prefLabel": "Count of all public datasets",
      "skos:definition": "Count all the publicly accessible datasets among all the datasets hosted on the platform."
    },
    {
      "@id": "Subjects Count",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Subjects" },
      "skos:prefLabel": "Count of all subjects",
      "skos:definition": "Count all subjects belonging to datasets and hosted on the platform."
    },
    {
      "@id": "Images Count",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Images" },
      "skos:prefLabel": "Count of all images",
      "skos:definition": "Count all DICOM series belonging to subjects and hosted on the platform."
    },
    {
      "@id": "Storage Volume Metric",
      "@type": "dqv:Metric",
      "dqv:inDimension": { "@id": "Storage Volume" },
      "skos:prefLabel": "Total data storage volume",
      "skos:definition": "Total data storage volume used to store all datasets on the platform."
    }
  ]
}`;

    this._renderer2.appendChild(this._document.head, script);
  }

  private fetchUsersCount() {
    //count all users
    this.userService.countAllUsers().then(count => {
      this.usersCount = count;
    });
  }

  private fetchEventsCount() {
    // count all users events during last month
    this.userService.countLastMonthEvents().then(count => {
      this.eventsCount = count;
    });
  }

  private fetchOverallStats() {
    // get public studies data
    this.fetchPublicStudies();
    // get the latest overall statistics
    this.datasetService.getOverallStatistics().then(stats => {
      this.studiesCount = stats.studiesCount;
      this.subjectsCount = stats.subjectsCount;
      this.datasetAcquisitionsCount = stats.datasetAcquisitionsCount;
      this.storageSize = stats.storageSize;
      this.fetchUsersCount();
      this.fetchEventsCount();
      this.addSchemaToDOM();
    });

  }

  private fetchPublicStudies() {
    // get public studies
    this.studyService.getPublicStudiesData().then(studies => {
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
    this.confirmDialogService.choose('Do you already have a Shanoir account ?', null, { yes: 'Yes, log in', no: 'No, request an account', cancel: 'Cancel' })
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