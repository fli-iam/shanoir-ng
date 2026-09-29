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
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.UID;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.tool.dcmqrscp.DcmQRSCP;

/**
 * Wraps dcm4che's real Query/Retrieve SCP ({@link DcmQRSCP}) so tests can run
 * it in-process as a stand-in for a real PACS with a configured ShUp AET.
 */
public class TestDicomServer {

    private final DcmQRSCP qrscp;
    
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public TestDicomServer(String aeTitle, int port, File dicomDirStorageRoot,
            Set<String[]> storageSopClassesAndTransferSyntaxes) throws IOException {
        this.qrscp = new DcmQRSCP();

        // Same defaults DcmQRSCP's own CLI uses when no --filepath/--fs-* options are given.
        qrscp.setDicomDirectory(new File(dicomDirStorageRoot, "DICOMDIR"));
        qrscp.setFilePathFormat("DICOM/{0020000D,hash}/{0020000E,hash}/{00080018,hash}");
        qrscp.setRecordFactory(new RecordFactory());

        Device device = qrscp.getDevice();
        Connection conn = device.listConnections().get(0);
        conn.setPort(port);

        ApplicationEntity ae = device.getApplicationEntities().iterator().next();
        ae.setAETitle(aeTitle);
        addTransferCapabilities(ae, storageSopClassesAndTransferSyntaxes);

        openDicomDirViaReflection();

        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);
    }

    private void addTransferCapabilities(ApplicationEntity ae, Set<String[]> storageSopClassesAndTransferSyntaxes) {
        ae.addTransferCapability(new TransferCapability(null,
                UID.Verification, TransferCapability.Role.SCP, UID.ImplicitVRLittleEndian));
        for (String[] cuidTsuid : storageSopClassesAndTransferSyntaxes) {
            ae.addTransferCapability(new TransferCapability(null,
                    cuidTsuid[0], TransferCapability.Role.SCP, cuidTsuid[1])); // receive seeding store
            ae.addTransferCapability(new TransferCapability(null,
                    cuidTsuid[0], TransferCapability.Role.SCU, cuidTsuid[1])); // forward on C-MOVE
        }
        ae.addTransferCapability(new TransferCapability(null,
                UID.StudyRootQueryRetrieveInformationModelFind, TransferCapability.Role.SCP,
                UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian));
        ae.addTransferCapability(new TransferCapability(null,
                UID.StudyRootQueryRetrieveInformationModelMove, TransferCapability.Role.SCP,
                UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian));
    }

    private void openDicomDirViaReflection() throws IOException {
        try {
            Method openDicomDir = DcmQRSCP.class.getDeclaredMethod("openDicomDir");
            openDicomDir.setAccessible(true);
            openDicomDir.invoke(qrscp);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Could not initialize test PACS DICOMDIR", e);
        }
    }

    public void start() throws IOException, GeneralSecurityException {
        qrscp.getDevice().bindConnections();
    }

    public void stop() {
        qrscp.getDevice().unbindConnections();
        executor.shutdown();
        scheduledExecutor.shutdown();
    }
    
    public void addRemoteConnection(String aet, String host, int port) {
        Connection remote = new Connection();
        remote.setHostname(host);
        remote.setPort(port);
        qrscp.addRemoteConnection(aet, remote);
    }

}
