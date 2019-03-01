import { animate, query, style, transition, trigger } from '@angular/animations';

export const slideDown = trigger('slideDown', [
    transition(
        ':enter', [
            style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}),
            animate('500ms ease-in-out', style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}),
            animate('500ms ease-in-out', style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}))
        ]
    )
]);

export const menuAnimDur = 100;
export const menuSlideDown = trigger('menuSlideDown', [
    transition(
        ':enter', [
            style({ height: 0 }),
            animate(menuAnimDur + 'ms ease-in-out', style({ height: '*', 'padding-bottom': '*' }))
        ]
    ),
    transition(
        ':leave', [
            style({ height: '*' }),
            animate(menuAnimDur + 'ms ease-in-out', style({ height: 0, 'padding-bottom': '0' }))
        ]
    )
]);

export const menuSlideRight = trigger('menuSlideRight', [
    transition(
        ':enter', [
            style({width: 0}),
            animate(menuAnimDur+'ms ease-in-out', style({width: '*'}))
        ]
    ),
    transition(
        ':leave', [
            style({width: '*'}),
            animate(menuAnimDur+'ms ease-in-out', style({width: 0}))
        ]
    )
]);
    

export const preventInitialChildAnimations = trigger('preventInitialChildAnimations', [
    transition(
        ':enter', [
            query(':enter', [], {optional: true})
        ]
    )
]);