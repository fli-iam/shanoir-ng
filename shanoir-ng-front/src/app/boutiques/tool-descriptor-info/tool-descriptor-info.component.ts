import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'tool-descriptor-info',
  templateUrl: './tool-descriptor-info.component.html',
  styleUrls: ['./tool-descriptor-info.component.css']
})
export class ToolDescriptorInfoComponent implements OnInit {

  @Input() descriptor: any = null

  constructor() { }

  ngOnInit() {
  }

  getTags() {
    let jsonTags = JSON.stringify(this.descriptor.tags, null, 4);
    return jsonTags.replace(/{|}|"|/g, '').replace(/:/g, ':\n')
    // return jsonTags.replace(/{|}|\[|\]|"|/g, '').replace(/:/g, ':\n')
  }

}
