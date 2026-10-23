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

package org.shanoir.ng.importer.service;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * The ZIP import knows a whole series when it creates the acquisition: it counts
 * the slices and sums the file sizes once. A STOW-RS import receives the series
 * one instance at a time, so the same two figures must grow with every accepted
 * instance, otherwise the acquisition keeps "Number of slices = 0" and the
 * study list keeps "Storage volume = 0 Bytes" whatever was sent.
 *
 * Both are single UPDATE statements that add to the stored value. Reading the
 * value, adding in Java and saving would lose increments when two instances of
 * one series are imported at the same time, and nothing in the importer
 * serialises them.
 *
 * Call inside the import transaction: a rolled-back import takes its increment
 * with it, so the figures only ever count instances that were stored.
 */
@Component
public class StowImportCounters {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * One more instance in the series: protocol.numberOfSlices += 1.
     *
     * MR, CT and XA protocols start at the number of instances of the series
     * known at creation, which is 0 for STOW-RS. A PET protocol starts from the
     * DICOM tag (0054,0081) when the sender provides it, which an increment
     * would then count twice, so PET is left alone.
     *
     * @return whether a protocol row was updated
     */
    @Transactional
    public boolean addInstanceToAcquisition(DatasetAcquisition acquisition) {
        if (acquisition instanceof MrDatasetAcquisition mr && mr.getMrProtocol() != null) {
            return incrementSlices("MrProtocol", mr.getMrProtocol().getId());
        }
        if (acquisition instanceof CtDatasetAcquisition ct && ct.getCtProtocol() != null) {
            return incrementSlices("CtProtocol", ct.getCtProtocol().getId());
        }
        if (acquisition instanceof XaDatasetAcquisition xa && xa.getXaProtocol() != null) {
            return incrementSlices("XaProtocol", xa.getXaProtocol().getId());
        }
        return false;
    }

    /**
     * expression.size += bytes, treating an unset size as 0.
     *
     * The study list sums the sizes that are not null, so an expression that
     * never received a size is invisible there.
     *
     * @return whether the expression row was updated
     */
    @Transactional
    public boolean addBytesToExpression(DatasetExpression expression, long bytes) {
        if (expression == null || expression.getId() == null || bytes <= 0) {
            return false;
        }
        return entityManager
                .createQuery("UPDATE DatasetExpression e SET e.size = COALESCE(e.size, 0) + :bytes WHERE e.id = :id")
                .setParameter("bytes", bytes)
                .setParameter("id", expression.getId())
                .executeUpdate() > 0;
    }

    private boolean incrementSlices(String protocolEntity, Long protocolId) {
        if (protocolId == null) {
            return false;
        }
        // The entity name comes from the fixed set above, never from input.
        return entityManager
                .createQuery("UPDATE " + protocolEntity
                        + " p SET p.numberOfSlices = COALESCE(p.numberOfSlices, 0) + 1 WHERE p.id = :id")
                .setParameter("id", protocolId)
                .executeUpdate() > 0;
    }

}
