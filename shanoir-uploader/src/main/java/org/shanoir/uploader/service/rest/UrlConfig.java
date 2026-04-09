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

package org.shanoir.uploader.service.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* http url config. Used to define urls in order to access shanoir-ng rest services using resource file.
*
* @author atouboulic
*
*/

public class UrlConfig {

    /**
    * Logger
    */
    private static final Logger LOG = LoggerFactory.getLogger(UrlConfig.class);

    private String urlStudy;

    private String urlStudyCard;

    private String urlGetSubject;

    private String urlGetSubjectByIdentifier;

    private String urlPostSubject;

    private String urlStudyWithStudyCard;


    public String getUrlStudyWithStudyCard() {
        return urlStudyWithStudyCard;
    }

    public void setUrlStudyWithStudyCard(String urlStudyWithStudyCard) {
        this.urlStudyWithStudyCard = urlStudyWithStudyCard;
    }

    public String getUrlStudy() {
        return urlStudy;
    }

    public void setUrlStudy(String urlStudy) {
        this.urlStudy = urlStudy;
    }

    public String getUrlStudyCard() {
        return urlStudyCard;
    }

    public void setUrlStudyCard(String urlStudyCard) {
        this.urlStudyCard = urlStudyCard;
    }

    public String getUrlGetSubject() {
        return urlGetSubject;
    }

    public void setUrlGetSubject(String urlGetSubject) {
        this.urlGetSubject = urlGetSubject;
    }

    public String getUrlPostSubject() {
        return urlPostSubject;
    }

    public void setUrlPostSubject(String urlPostSubject) {
        this.urlPostSubject = urlPostSubject;
    }

    public String getUrlGetSubjectByIdentifier() {
        return urlGetSubjectByIdentifier;
    }

    public void setUrlGetSubjectByIdentifier(String urlGetSubjectByIdentifier) {
        this.urlGetSubjectByIdentifier = urlGetSubjectByIdentifier;
    }
}
