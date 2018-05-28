import { Component, OnInit, SimpleChange, Input } from "@angular/core";

declare var papaya: any;
declare var papayaContainers: any[];

@Component({
    selector: "papaya",
    templateUrl: "papaya.component.html",
    styleUrls: ["papaya.component.css"]
})
export class PapayaComponent implements OnInit {
    @Input() params: Object[];
    @Input() autoLoading: boolean = false;
    private loaded: boolean = false;

    constructor() {
            this.params = [];
            this.params["kioskMode"] = true;
            this.params["expandable"] = true;
            this.params["allowScroll"] = false;
            this.params["radiological"] = true;
            this.params["showRuler"] = true;
    }

    ngOnInit() {
        papayaContainers = [];
        papaya.Container.startPapaya();
    }

    ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
        for (let propName in changes) {
            if (!changes[propName].isFirstChange()) {
                if (propName == "params") {
                    if (this.autoLoading) {
                        this.load();
                    } else {
                        this.loaded = false;
                    }
                }
            }
        }
    }

    private load() {
        papaya.Container.resetViewer(0, this.params);
        this.loaded = true;
    }
}