const webpack = require('webpack');
const webpackMerge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const OpenBrowserPlugin = require('open-browser-webpack-plugin');
const commonConfig = require('./webpack.common.js');
const helpers = require('./helpers');

/**
 * Webpack Constants
 */
const ENV = process.env.ENV = process.env.NODE_ENV = 'development';
const METADATA = webpackMerge(commonConfig.metadata, {
    host: 'localhost',
    BACKEND_API_USERS_MS_URL: 'http://localhost:9901',
    BACKEND_API_STUDIES_MS_URL: 'http://localhost:9902',
    BACKEND_API_IMPORT_MS_URL: 'http://localhost:9903',
    BACKEND_API_DATASET_MS_URL: 'http://localhost:9906',
    BACKEND_API_PRECLINICAL_MS_URL: 'http://localhost:9909',
    KEYCLOAK_BASE_URL: 'http://localhost/auth',
    LOGOUT_REDIRECT_URL: 'http://localhost:8080/shanoir-ng/index.html',
    port: 8080,

    ENV: ENV,
});

var path = require("path");

module.exports = webpackMerge(commonConfig, {
    devtool: 'cheap-module-eval-source-map',

    output: {
        path: helpers.root('dist'),
        publicPath: '/shanoir-ng',
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
        new OpenBrowserPlugin({ url: 'http://localhost:8080/shanoir-ng' }),

        new HtmlWebpackPlugin({
            filename: 'index.html',
            template: 'src/index-dev.html'
        }),

        new ExtractTextPlugin('[name].css'),

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
            'BACKEND_API_IMPORT_MS_URL': JSON.stringify(METADATA.BACKEND_API_IMPORT_MS_URL),
            'BACKEND_API_DATASET_MS_URL': JSON.stringify(METADATA.BACKEND_API_DATASET_MS_URL),
            'BACKEND_API_PRECLINICAL_MS_URL': JSON.stringify(METADATA.BACKEND_API_PRECLINICAL_MS_URL),
            'KEYCLOAK_BASE_URL': JSON.stringify(METADATA.KEYCLOAK_BASE_URL),
            'LOGOUT_REDIRECT_URL': JSON.stringify(METADATA.LOGOUT_REDIRECT_URL),
            'process.env': {
                'ENV': JSON.stringify(METADATA.ENV),
                'NODE_ENV': JSON.stringify(METADATA.ENV),
                'BACKEND_API_USERS_MS_URL': JSON.stringify(METADATA.BACKEND_API_USERS_MS_URL),
                'BACKEND_API_STUDIES_MS_URL': JSON.stringify(METADATA.BACKEND_API_STUDIES_MS_URL),
                'BACKEND_API_IMPORT_MS_URL': JSON.stringify(METADATA.BACKEND_API_IMPORT_MS_URL),
                'BACKEND_API_DATASET_MS_URL': JSON.stringify(METADATA.BACKEND_API_DATASET_MS_URL),
                'BACKEND_API_PRECLINICAL_MS_URL': JSON.stringify(METADATA.BACKEND_API_PRECLINICAL_MS_URL),
                'LOGOUT_REDIRECT_URL': JSON.stringify(METADATA.LOGOUT_REDIRECT_URL),
                'KEYCLOAK_BASE_URL': JSON.stringify(METADATA.KEYCLOAK_BASE_URL),
            }
        })
    ],

    devServer: {
        historyApiFallback: true,
        stats: 'minimal'
    }
});