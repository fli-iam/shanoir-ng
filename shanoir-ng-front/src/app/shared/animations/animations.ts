import { query, style, state, animate, transition, trigger } from '@angular/animations';

export const slideDown = trigger('slideDown', [
    transition(
        ':enter', [
            style({height: '0'}),
            animate('500ms ease-in-out', style({height: '*'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*'}),
            animate('500ms ease-in-out', style({height: '0'}))
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