import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { BreadcrumbsService } from '@app/breadcrumbs/breadcrumbs.service';
import { Pipeline } from '@app/vip/models/pipeline';
import { PipelineGroup, PipelineVersion } from '@app/vip/models/pipeline-group';

import { ExecutionDataService } from '../execution.data-service';

import { PipelineService } from "./pipeline/pipeline.service";
import { PipelineComponent } from './pipeline/pipeline.component';

@Component({
    selector: 'app-pipelines',
    templateUrl: './pipelines.component.html',
    styleUrls: ['./pipelines.component.css'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [PipelineComponent]
})
export class PipelinesComponent implements OnInit {

    pipelines: Pipeline[];
    /** The pipelines gathered by name, one tile per name with a version picker. */
    pipelineGroups: PipelineGroup[];
    selectedPipeline: Pipeline;
    /** Identifier of the clicked pipeline, so that the tile is highlighted while its description loads. */
    selectedIdentifier: string;
    descriptionLoading: boolean;

    constructor(private breadcrumbsService: BreadcrumbsService, 
            private pipelineService: PipelineService, 
            private router: Router, 
            private processingService: ExecutionDataService) {
        this.pipelines = [];
        this.pipelineGroups = [];
        this.descriptionLoading = false;

        this.breadcrumbsService.currentStepAsMilestone();
        this.breadcrumbsService.nameStep('1. Processing');
    }

    ngOnInit(): void {
        this.pipelineService.listPipelines().then(
            (pipelines: Pipeline[]) => {
                this.pipelines = pipelines;
                this.pipelineGroups = this.groupByName(pipelines);
            }
        )
    }

    /**
     * VIP lists every version as its own entry, which makes the tile list several times longer
     * than the number of actual pipelines. Gather them by name, latest version first.
     */
    private groupByName(pipelines: Pipeline[]): PipelineGroup[] {
        const groups: Map<string, PipelineGroup> = new Map();
        pipelines.forEach(pipeline => {
            // The identifier is built as "<name>/<version>", fall back on it when VIP omits either field.
            const [identifierName, identifierVersion] = (pipeline.identifier ?? '').split('/');
            const name: string = pipeline.name ?? identifierName;
            const version: PipelineVersion = { label: pipeline.version ?? identifierVersion, pipeline: pipeline };
            const group: PipelineGroup = groups.get(name);
            if (group) {
                group.versions.push(version);
            } else {
                groups.set(name, { name: name, versions: [version] });
            }
        });
        const grouped: PipelineGroup[] = Array.from(groups.values());
        grouped.forEach(group => group.versions.sort((a, b) => PipelinesComponent.compareVersions(b.label, a.label)));
        return grouped;
    }

    /**
     * Compares dotted versions segment by segment ("0.9" < "0.10"), falling back on a
     * string comparison for any segment that is not a number.
     */
    private static compareVersions(a: string, b: string): number {
        const segmentsA: string[] = (a ?? '').split('.');
        const segmentsB: string[] = (b ?? '').split('.');
        for (let i = 0; i < Math.max(segmentsA.length, segmentsB.length); i++) {
            const segmentA: string = segmentsA[i] ?? '';
            const segmentB: string = segmentsB[i] ?? '';
            const numberA: number = Number(segmentA);
            const numberB: number = Number(segmentB);
            if (segmentA === '' || segmentB === '' || isNaN(numberA) || isNaN(numberB)) {
                const comparison: number = segmentA.localeCompare(segmentB);
                if (comparison !== 0) return comparison;
            } else if (numberA !== numberB) {
                return numberA - numberB;
            }
        }
        return 0;
    }

    selectPipeline(pipeline: Pipeline) {
        this.selectedIdentifier = pipeline.identifier;
        this.descriptionLoading = true;
        this.pipelineService.getPipeline(pipeline.identifier).then(
            (pipeline: Pipeline) => {
                this.descriptionLoading = false;
                this.selectedPipeline = pipeline;
            },
            (error) => {
                console.error(error);
            }
        )
    }

    isSelectedDatasets(): boolean {
        return this.processingService.selectedDatasets && this.processingService.selectedDatasets.size > 0;
    }

    choosePipeLine() {
        this.processingService.setPipeline(this.selectedPipeline);
        // let filesParam = 0;
        // // Here we are going to calculate the number of possible executions in parallel
        // this.selectedPipeline.parameters?.forEach(parameter => {
        //     if (parameter.type == 'File') {
        //         filesParam += 1;
        //     }
        // })
        this.router.navigate(['execution']);
    }

    navigateToSolr(): void {
        this.router.navigate(['/solr-search']);
    }
}