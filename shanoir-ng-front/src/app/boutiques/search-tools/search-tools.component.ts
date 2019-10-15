import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';

@Component({
  selector: 'search-tools',
  templateUrl: './search-tools.component.html',
  styleUrls: ['./search-tools.component.css']
})
export class SearchToolsComponent implements OnInit {

  @Output() toolSelected = new EventEmitter<ToolInfo>();

  selectedTool: ToolInfo

  constructor(private toolService:ToolService) { }

  ngOnInit() {
  }

  onToolSelected(toolInfo: ToolInfo) {
  	this.selectedTool = toolInfo;
    this.toolSelected.emit(this.selectedTool);
  }

}
