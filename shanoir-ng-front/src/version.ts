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

const { gitDescribeSync } = require('git-describe');
const { version } = require('../package.json');
const { resolve, relative } = require('path');
const { writeFileSync } = require('fs-extra');

const gitInfo = gitDescribeSync({
    match: 'NG_v[0-9]*',
    longSemver: true
});

gitInfo.version = version;

const file = resolve(__dirname, '..', 'src', 'environments', 'version.ts');
writeFileSync(file,
`// IMPORTANT: THIS FILE IS AUTO GENERATED! DO NOT MANUALLY EDIT OR CHECKIN!
/* tslint:disable */
export const VERSION = ${JSON.stringify(gitInfo, null, 4)};
/* tslint:enable */
`, { encoding: 'utf-8' });

console.log(`Wrote version info ${gitInfo.raw} to ${relative(resolve(__dirname, '..'), file)}`);