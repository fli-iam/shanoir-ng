import { Component } from '@angular/core';
import { TreeNodeComponent } from '../../shared/components/tree/tree.node.component';

@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
})

export class StudyTreeComponent {

    public subjects: Object;

    constructor() {
       this.subjects = [
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