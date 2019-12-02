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
    return JSON.stringify(this.descriptor.tags);
  }

}
