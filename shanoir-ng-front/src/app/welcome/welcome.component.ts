import {Component, ElementRef, HostListener, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import * as AppUtils from "../utils/app.utils";
import {ImagesUrlUtil} from "../shared/utils/images-url.util";
import {StudyService} from "../studies/shared/study.service";
import {PublicStudyData} from "../studies/shared/study.dto";
import {StudyType} from "../studies/shared/study-type.enum";
import {isDarkColor} from "../utils/app.utils";

@Component({
	selector: 'app-welcome',
	templateUrl: './welcome.component.html',
	styleUrls: ['./welcome.component.css'],
	encapsulation: ViewEncapsulation.None
})
export class WelcomeComponent implements OnInit {

	public githubLogoUrl: string = ImagesUrlUtil.GITHUB_WHITE_LOGO_PATH;
	public shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
	public email: string = "mailto:developers_shanoir-request@inria.fr";
	public studies: PublicStudyData[] = [];
	public StudyType = StudyType;
	public show: number = 10;
	@ViewChild('showMore', { static: false }) showMore: ElementRef<HTMLElement>;

	constructor(
		private studyService: StudyService
	) { }

	ngOnInit(): void {
		this.fetchStudies();
	}

	private fetchStudies() {
		this.studyService.getPublicStudiesData().then(studies => {
			// sort by nbExaminations
			this.studies = studies.sort((a, b) => {
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
		window.location.href = window.location.protocol + "//" + window.location.hostname + "/shanoir-ng/account/study/" + study.id + "/account-request";
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
