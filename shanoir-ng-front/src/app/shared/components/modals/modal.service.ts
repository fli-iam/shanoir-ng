import { ComponentFactoryResolver, Injectable, Type, ViewContainerRef } from '@angular/core';
import { ModalsComponent } from './modals.component';
import { Mode } from '../entity/entity.component.abstract';

@Injectable()
export class ModalService {

    public rootViewCRef: ViewContainerRef;

    constructor(
        private componentFactoryResolver: ComponentFactoryResolver) {}

    public open(componentClass: Type<any>, mode: Mode): Promise<any> { // TODO : componentClass: EntityComponent<any>
        console.log('open');
        let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
        
        let componentRef = this.rootViewCRef.createComponent(componentFactory);
        componentRef.instance.mode = mode;
        
        let modalCompFactory = this.componentFactoryResolver.resolveComponentFactory(ModalsComponent);
        
        let modalCompRef = this.rootViewCRef.createComponent(
            modalCompFactory,
            0,
            undefined,
            [[componentRef.location.nativeElement]]
        );

        return Promise.resolve();
    }

}