import { Component, OnInit, Output, EventEmitter } from '@angular/core';


@Component({
    selector: 'import-bruker',
    templateUrl: 'importBruker.component.html',
    styleUrls: ['importBruker.component.css'],
    animations: []
})

export class ImportBrukerComponent implements OnInit {

	importBrukerFileEnabled: boolean = true;
	importBrukerFileOpen: boolean = true;
	
	modalityEnabled: boolean = false;
	modalityOpen: boolean = false;
	
	selectSeriesEnabled: boolean = false;
	selectSeriesOpen: boolean = false;
	
	ngOnInit(): void {
	}
	
	onUploadBrukerFile(event) {
		//this.importBrukerFileEnabled = false;
		//this.importBrukerFileOpen = true;
		this.modalityEnabled = true;
		this.modalityOpen = true;
		this.selectSeriesEnabled = true;
		this.selectSeriesOpen = true;
	}
	
	onSelectModality(event){
		this.importBrukerFileEnabled = false;
		this.importBrukerFileOpen = true;
		this.modalityEnabled = false;
		this.modalityOpen = true;
		this.selectSeriesEnabled = true;
		this.selectSeriesOpen = true;
	}
	
	onSelectSeries(event){
		this.importBrukerFileEnabled = false;
		this.importBrukerFileOpen = false;
		this.modalityEnabled = false;
		this.modalityOpen = false;
		this.selectSeriesEnabled = false;
		this.selectSeriesOpen = true;
	}
}