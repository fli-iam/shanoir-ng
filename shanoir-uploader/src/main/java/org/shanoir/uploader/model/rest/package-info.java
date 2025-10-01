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

/**
 * New package with model classes for Shanoir-NG, that uses REST only.
 * Version 7.0 of ShUp, that will run only with Shanoir-NG, will then
 * delete the two packages:
 * - org.shanoir.uploader.model
 * - org.shanoir.uploader.model.dto
 * The package org.shanoir.uploader.model.rest remains for the future.
 * Very unnecessary copy operations are part of ImportDialogOpener,
 * that is only used for Shanoir-old and the Soap web service interface.
 *
 * ImportDialogOpenerNG uses only this package for its data exchange objects.
 * In version 7.0 the ImportDialogOpenerNG will be renamed to ImportDialogOpener
 * and the old Opener will be deleted, as it is only used for the Soap exchange.
 *
 * @author mkain
 *
 */
package org.shanoir.uploader.model.rest;