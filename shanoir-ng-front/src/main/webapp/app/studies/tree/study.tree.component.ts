import { Component } from '@angular/core';
import { TreeNodeComponent } from './shared/tree/tree.node.component';

@Component({
    selector: 'study-tree',
    moduleId: module.id,
    templateUrl: 'study.tree.component.html',
    styleUrls: ['../../shared/css/common.css', 'study.tree.component.css'],
})

export class StudyTreeComponent {

    private patients;

    constructor() {
       this.patients = [
           {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, {
                name: "01016SACH",
                examinations: [
                    {
                        name: "NEURO^ENCEPHALE 32",
                        date: "17/01/2014",
                        acquisitions : [
                            {
                                name: "T1 MPRAGE (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE"
                                    }
                                ]
                            }, {
                                name: "AX T2 2 ECHOS (Mr)",
                                datasets: [
                                    {
                                        name: "AX T2 2 ECHOS"
                                    }
                                ]
                            }, {
                                name: "3D FLAIR FS (Mr)",
                                datasets: [
                                    {
                                        name: "3D FLAIR FS"
                                    }
                                ]
                            }, {
                                name: "T1 MPRAGE GADO (Mr)",
                                datasets: [
                                    {
                                        name: "T1 MPRAGE GADO"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }, 
       ];
    }
}