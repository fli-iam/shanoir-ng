import { Component } from '@angular/core';
import { SmallAppComponent } from './smallApp/smallApp.component'

@Component({
    selector: 'app-store',
    templateUrl: 'app/appStore/appStore.component.html',
    styleUrls: ['app/appStore/appStore.component.css'],
})

export class AppStoreComponent {

    private apps: Object = [
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
        {id: 1, label: "Lesion Detection Pipeline", author: "INRIA", logo: "./app/images/MSSEG logo.png", review: {rate: 0.8, reviewers: 11}},
    ]

    constructor() {
    }
       
}