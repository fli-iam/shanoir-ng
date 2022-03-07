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