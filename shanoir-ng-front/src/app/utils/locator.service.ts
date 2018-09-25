import {Injector, ViewContainerRef} from "@angular/core";

export class ServiceLocator {
    static injector: Injector;
    static rootViewContainerRef: ViewContainerRef;
}