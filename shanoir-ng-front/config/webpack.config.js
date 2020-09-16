/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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

const METADATA = webpackMerge(commonConfig.metadata, {
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
            'process.env': {
                'ENV': JSON.stringify(METADATA.ENV),
                'NODE_ENV': JSON.stringify(METADATA.ENV),
            }
        })
    ]
});
