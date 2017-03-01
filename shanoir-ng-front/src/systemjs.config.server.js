/**
 * System configuration for deployment without installing node_modules
 * Loads umd packages from the web instead
 * Adjust as necessary for your application needs.
 */
(function (global) {
    System.config({
        paths: {
            'npm:': 'https://unpkg.com/' // path serves as alias
        },
        // map tells the System loader where to look for things
        map: {
            app: 'app', // location of transpiled app files
            // angular minimized umd bundles
            '@angular/core': 'npm:@angular/core/bundles/core.umd.min.js',
            '@angular/common': 'npm:@angular/common/bundles/common.umd.min.js',
            '@angular/compiler': 'npm:@angular/compiler/bundles/compiler.umd.min.js',
            '@angular/compiler-cli': 'npm:@angular/compiler/bundles/compiler-cli.umd.min.js',
            '@angular/forms': 'npm:@angular/forms/bundles/forms.umd.min.js',
            '@angular/http': 'npm:@angular/http/bundles/http.umd.min.js',
            '@angular/material': 'npm:@angular/material/bundles/material.umd.js',
            '@angular/platform-browser': 'npm:@angular/platform-browser/bundles/platform-browser.umd.min.js',
            '@angular/platform-browser-dynamic': 'npm:@angular/platform-browser-dynamic/bundles/platform-browser-dynamic.umd.min.js',
            '@angular/platform-server': 'npm:@angular/platform-browser/bundles/platform-server.umd.min.js',
            '@angular/router': 'npm:@angular/router/bundles/router.umd.min.js',
            // other libraries
            'rxjs': 'npm:rxjs@5.0.1',
            'angular-in-memory-web-api': 'npm:angular-in-memory-web-api/bundles/in-memory-web-api.umd.js',
            'mydatepicker': 'npm:mydatepicker/bundles/mydatepicker.umd.js'
        },
        // packages tells the System loader how to load when no filename and/or no extension
        packages: {
            app: {
                main: './main.js',
                defaultExtension: 'js'
            },
            rxjs: {
                defaultExtension: 'js'
            },
            'angular2-in-memory-web-api': {
                main: './index.js',
                defaultExtension: 'js'
            },
            lib: {
                format: 'register',
                defaultExtension: 'js'
            }
        }
    });
})(this);