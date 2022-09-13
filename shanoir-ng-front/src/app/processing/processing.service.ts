import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Pipeline } from '../carmin/models/pipeline';

@Injectable({
  providedIn: 'root'
})
export class ProcessingService {

  //subjects
  private selectedDatasetsSubject: BehaviorSubject<Set<number>>;
  private selectedPipelineSubject: BehaviorSubject<Pipeline>;

  //observables
  public selectedDatasets: Observable<Set<number>>;
  public selectedPipeline: Observable<Pipeline>;

  constructor() { 
    this.selectedDatasetsSubject = new BehaviorSubject<Set<number>>(new Set());
    this.selectedPipelineSubject = new BehaviorSubject<Pipeline>(null);

    this.selectedDatasets = this.selectedDatasetsSubject.asObservable();
    this.selectedPipeline = this.selectedPipelineSubject.asObservable();
  }


  public clearSelectedPipeline():void{
    this.selectedPipelineSubject.next(null);
  }

  public clearDatasets(): void{
    this.selectedDatasetsSubject.next(new Set());
  }

  public setDatasets(datasetsIds : Set<number>){
    this.selectedDatasetsSubject.next(datasetsIds);
  }

  public setPipeline(pipeline: Pipeline){
    this.selectedPipelineSubject.next(pipeline);
  }

  public get selectedDatasetsValue(): Set<number>{
    return this.selectedDatasetsSubject.value;
  }

  public get selectedPipelineValue(): Pipeline{
    return this.selectedPipelineSubject.value;
  }

  public isDatasetsSubjectValid():boolean{
    let selectedDatasets: Set<number>;
    this.selectedDatasets.subscribe(
      (datasets: Set<number>)=>{
        selectedDatasets = datasets;
      }
    );

    if(selectedDatasets == null || selectedDatasets.size == 0){
      return false;
    }
    return true;
  }

  public isAnyPipelineSelected(): boolean{
    let selectedPipeline: Pipeline;
    this.selectedPipeline.subscribe(
      (pipeline: Pipeline)=>{
        selectedPipeline = pipeline;
      }
    );

    if(selectedPipeline == null) return false;
    return true;
  }
}
