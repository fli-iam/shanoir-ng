const webpack = require('webpack');
const webpackMerge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const commonConfig = require('./webpack.common.js');
const helpers = require('./helpers');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')

/**
 * Webpack Constants
 */
const ENV = process.env.ENV = process.env.NODE_ENV = 'development';
const BACKEND_API_ROOT_URL = 'http://shanoir-ng-nginx/shanoir-ng';
const METADATA = webpackMerge(commonConfig.metadata, {
    host: 'shanoir-ng-nginx',
    BACKEND_API_USERS_MS_URL: BACKEND_API_ROOT_URL + '/users',
    BACKEND_API_STUDIES_MS_URL: BACKEND_API_ROOT_URL + '/studies',
	BACKEND_API_DATASET_MS_URL: BACKEND_API_ROOT_URL + '/datasets',
	BACKEND_API_IMPORT_MS_URL: BACKEND_API_ROOT_URL + '/import',
    BACKEND_API_PRECLINICAL_MS_URL: '/preclinical',
    KEYCLOAK_BASE_URL: 'http://shanoir-ng-nginx/auth',
    LOGOUT_REDIRECT_URL: 'http://shanoir-ng-nginx/shanoir-ng/index.html',
    port: 8080,
    ENV: ENV,
});

module.exports = webpackMerge(commonConfig, {
    devtool: 'source-map',

    output: {
        path: helpers.root('dist'),
        publicPath: '/shanoir-ng/',
        filename: '[name].js',
        chunkFilename: '[id].chunk.js'
    },

    module: {
        rules: [
            {
                test: /\.ts$/,
                loaders: ['awesome-typescript-loader', 'angular2-template-loader']
            }
        ]
    },

    plugins: [
        new HtmlWebpackPlugin({
            filename: 'index.html',
            template: 'src/index.html'
        }),

        new webpack.NoEmitOnErrorsPlugin(),
        new UglifyJsPlugin({
            "sourceMap": false,
            "uglifyOptions": {
                "compress": { "warnings": false },
                "mangle": true,
                "output": { "comments": false }
            }
        }),
        new ExtractTextPlugin('[name].css'),
        new webpack.DefinePlugin({
            'process.env': {
                'ENV': JSON.stringify(ENV)
            }
        }),
        new webpack.LoaderOptionsPlugin({
            htmlLoader: {
                minimize: false // workaround for ng2
            }
        }),

        /**
         * Plugin: DefinePlugin
         * Description: Define free variables.
         * Useful for having development builds with debug logging or adding global constants.
         *
         * Environment helpers
         *
         * See: https://webpack.github.io/docs/list-of-plugins.html#defineplugin
         */
        new webpack.DefinePlugin({
            'ENV': JSON.stringify(METADATA.ENV),
            'BACKEND_API_USERS_MS_URL': JSON.stringify(METADATA.BACKEND_API_USERS_MS_URL),
            'BACKEND_API_STUDIES_MS_URL': JSON.stringify(METADATA.BACKEND_API_STUDIES_MS_URL),
            'BACKEND_API_DATASET_MS_URL': JSON.stringify(METADATA.BACKEND_API_DATASET_MS_URL),
            'BACKEND_API_IMPORT_MS_URL': JSON.stringify(METADATA.BACKEND_API_IMPORT_MS_URL),
            'BACKEND_API_PRECLINICAL_MS_URL': JSON.stringify(METADATA.BACKEND_API_PRECLINICAL_MS_URL),
            'KEYCLOAK_BASE_URL': JSON.stringify(METADATA.KEYCLOAK_BASE_URL),
            'LOGOUT_REDIRECT_URL': JSON.stringify(METADATA.LOGOUT_REDIRECT_URL),
            'process.env': {
                'ENV': JSON.stringify(METADATA.ENV),
                'NODE_ENV': JSON.stringify(METADATA.ENV),
                'BACKEND_API_USERS_MS_URL': JSON.stringify(METADATA.BACKEND_API_USERS_MS_URL),
                'BACKEND_API_STUDIES_MS_URL': JSON.stringify(METADATA.BACKEND_API_STUDIES_MS_URL),
                'BACKEND_API_DATASET_MS_URL': JSON.stringify(METADATA.BACKEND_API_DATASET_MS_URL),
                'BACKEND_API_IMPORT_MS_URL': JSON.stringify(METADATA.BACKEND_API_IMPORT_MS_URL),
                'BACKEND_API_PRECLINICAL_MS_URL': JSON.stringify(METADATA.BACKEND_API_PRECLINICAL_MS_URL),
                'LOGOUT_REDIRECT_URL': JSON.stringify(METADATA.LOGOUT_REDIRECT_URL),
                'KEYCLOAK_BASE_URL': JSON.stringify(METADATA.KEYCLOAK_BASE_URL),
            }
        })
    ]
});