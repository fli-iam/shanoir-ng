import { query, style, state, animate, transition, trigger } from '@angular/animations';

export const slideDown = trigger('slideDown', [
    transition(
        ':enter', [
            style({height: '0', 'padding-bottom': '0'}),
            animate('500ms ease-in-out', style({height: '*', 'padding-bottom': '*'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*', 'padding-bottom': '*'}),
            animate('500ms ease-in-out', style({height: '0', 'padding-bottom': '0'}))
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