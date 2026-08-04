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

package org.shanoir.uploader.test.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.tool.storescu.StoreSCU;

/**
 * Seeds a running {@link TestDicomServer} with a folder of DICOM files via a
 * real C-STORE, using dcm4che's embeddable {@link StoreSCU} API directly
 * (no CLI parsing / no risk of {@code System.exit} on a bad argument).
 */
public class TestDicomServerSeeder {

    public static void seed(File dicomSourceDir, Set<String[]> sopClassesAndTransferSyntaxes,
            String callingAet, String pacsAet, String pacsHost, int pacsPort) throws Exception {
        Device device = new Device("test-dicom-server-seeder");
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(callingAet);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        StoreSCU storeSCU = new StoreSCU(ae);
        storeSCU.setAttributes(new Attributes());

        Connection remote = storeSCU.getRemoteConnection();
        remote.setHostname(pacsHost);
        remote.setPort(pacsPort);

        AAssociateRQ rq = storeSCU.getAAssociateRQ();
        rq.setCallingAET(callingAet);
        rq.setCalledAET(pacsAet);

        for (String[] cuidTsuid : sopClassesAndTransferSyntaxes) {
            storeSCU.addOfferedStorageSOPClass(cuidTsuid[0], cuidTsuid[1]); // exact match only, no alternatives
        }

        List<String> files = new ArrayList<>();
        files.add(dicomSourceDir.getAbsolutePath());

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        java.util.concurrent.ScheduledExecutorService scheduledExecutor =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);
        try {
            storeSCU.scanFiles(files);
            storeSCU.open();
            storeSCU.sendFiles();
        } finally {
            storeSCU.close();
            executor.shutdown();
            scheduledExecutor.shutdown();
        }
    }

    /** Walks the folder once to find every distinct SOP Class UID present, so we can
     *  offer matching presentation contexts instead of hard coding e.g. MR Image Storage. */
    public static Set<String[]> scanSopClassesAndTransferSyntaxes(File dir) throws IOException {
        Set<String[]> found = new LinkedHashSet<>();
        Set<String> seen = new LinkedHashSet<>();
        File[] files = dir.listFiles();
        if (files == null) return found;
        for (File f : files) {
            if (f.isDirectory()) {
                found.addAll(scanSopClassesAndTransferSyntaxes(f));
                continue;
            }
            try (DicomInputStream in = new DicomInputStream(f)) {
                in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
                Attributes ds = in.readDataset(Tag.SOPInstanceUID + 1);
                String cuid = ds.getString(Tag.SOPClassUID);
                String tsuid = in.getTransferSyntax(); // the file's ACTUAL encoding
                String key = cuid + "|" + tsuid;
                if (cuid != null && tsuid != null && seen.add(key)) {
                    found.add(new String[]{cuid, tsuid});
                }
            } catch (Exception notDicom) {
                // skip DICOMDIR and non-DICOM files
            }
        }
        return found;
    }

}
