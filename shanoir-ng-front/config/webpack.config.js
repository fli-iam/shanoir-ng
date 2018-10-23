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
const ENV = process.env.ENV = process.env.NODE_ENV = 'production';

const SHANOIR_NG_URL_SCHEME = 'http://';
const SHANOIR_NG_URL_HOST = 'shanoir-ng-nginx';

const SHANOIR_NG_URL_BACKEND_API =
	SHANOIR_NG_URL_SCHEME
	+ SHANOIR_NG_URL_HOST
	+ '/shanoir-ng';
	
const SHANOIR_NG_URL_KEYCLOAK =
	SHANOIR_NG_URL_SCHEME
	+ SHANOIR_NG_URL_HOST
	+ '/auth';

const SHANOIR_NG_URL_LOGOUT =
	SHANOIR_NG_URL_SCHEME
	+ SHANOIR_NG_URL_HOST
	+ '/shanoir-ng/index.html';

const METADATA = webpackMerge(commonConfig.metadata, {
    BACKEND_API_USERS_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/users',
    BACKEND_API_STUDIES_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/studies',
	BACKEND_API_DATASET_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/datasets',
	BACKEND_API_IMPORT_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/import',
    KEYCLOAK_BASE_URL: SHANOIR_NG_URL_KEYCLOAK,
    LOGOUT_REDIRECT_URL: SHANOIR_NG_URL_LOGOUT,
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
            'KEYCLOAK_BASE_URL': JSON.stringify(METADATA.KEYCLOAK_BASE_URL),
            'LOGOUT_REDIRECT_URL': JSON.stringify(METADATA.LOGOUT_REDIRECT_URL),
            'process.env': {
                'ENV': JSON.stringify(METADATA.ENV),
                'NODE_ENV': JSON.stringify(METADATA.ENV),
                'BACKEND_API_USERS_MS_URL': JSON.stringify(METADATA.BACKEND_API_USERS_MS_URL),
                'BACKEND_API_STUDIES_MS_URL': JSON.stringify(METADATA.BACKEND_API_STUDIES_MS_URL),
                'BACKEND_API_DATASET_MS_URL': JSON.stringify(METADATA.BACKEND_API_DATASET_MS_URL),
                'BACKEND_API_IMPORT_MS_URL': JSON.stringify(METADATA.BACKEND_API_IMPORT_MS_URL),
                'LOGOUT_REDIRECT_URL': JSON.stringify(METADATA.LOGOUT_REDIRECT_URL),
                'KEYCLOAK_BASE_URL': JSON.stringify(METADATA.KEYCLOAK_BASE_URL),
            }
        })
    ]
});