import { Component, Input, OnInit } from '@angular/core';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { ImagesUrlUtil } from 'src/app/shared/utils/images-url.util';

@Component({
  selector: 'app-pipeline',
  templateUrl: './pipeline.component.html',
  styleUrls: ['./pipeline.component.css']
})
export class PipelineComponent implements OnInit {

  @Input() pipeline:Pipeline;
  readonly ImagesUrlUtil = ImagesUrlUtil;

  constructor() { }

  ngOnInit(): void {
    console.log(this.pipeline);
  }

}
