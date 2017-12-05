import { Component, Input } from '@angular/core';

import { ImagesUrlUtil } from "../../utils/images-url.util";
import { DatasetModalityType } from "../../enums/dataset-modality-type";

@Component({
  selector: 'list',
  templateUrl: 'list.component.html',
  styleUrls: ['list.component.css']
})
export class ListComponent {
    private zoomInIconPath: string = ImagesUrlUtil.ZOOM_IN_ICON_PATH;
    private zoomOutIconPath: string = ImagesUrlUtil.ZOOM_OUT_ICON_PATH;
    @Input() title: string;
    @Input() tab_open: Boolean;
    @Input() list: any[];
    private link: string;
    private text: string;

    getText(o: any): string {
        switch (this.title) {
            case "Acquisition Equipments List": 
                this.text = o.manufacturerModel.manufacturer.name + " - " + o.manufacturerModel.name + " " 
                    + (o.manufacturerModel.magneticField ? (o.manufacturerModel.magneticField + "T") : "") 
                    + " (" + DatasetModalityType[o.manufacturerModel.datasetModalityType] + ")"
                    + " " + o.serialNumber;
                break;
            case "Investigators List":
                this.text = "investigator name";
                break;
            default:
                this.text = "";
        }
    return this.text;
    }

    getLink(o: any): string {
        switch (this.title) {
            case "Acquisition Equipments List": 
                this.link = "/acquisition-equipment";
                break;
            case "Investigators List":
                this.link = "/investigator";
                break;
            default:
                this.link = "";
        }
    return this.link;
    }
}
