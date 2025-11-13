/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Component, Input } from '@angular/core';

import { TaskState } from 'src/app/async-tasks/task.model';

import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';

import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'challenge-block',
    templateUrl: 'challenge-block.component.html',
    styleUrls: ['challenge-block.component.css'],
    imports: [LoadingBarComponent, RouterLink]
})

export class ChallengeBlockComponent {

    @Input() challengeStudy: Study;
    protected downloadState: TaskState = new TaskState();

    constructor(
            private studyService: StudyService) {
    }

    downloadFile(filePath: string) {
        this.studyService.downloadProtocolFile(filePath, this.challengeStudy.id, this.downloadState);
    }

}