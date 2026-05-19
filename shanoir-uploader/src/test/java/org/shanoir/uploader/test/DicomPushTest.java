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

package org.shanoir.uploader.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.util.TagUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.shanoir.uploader.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test makes a c-store into a running ShUp instance
 * to test the feature of DICOM push.
 */
public class DicomPushTest {

    private static final Logger LOG = LoggerFactory.getLogger(DicomPushTest.class);

    private static Properties dicomServerProperties = new Properties();

    private static String remoteHost;

    private static String remotePort;

    private static String calledAET;

    private static final String CALLING_AET = "testDicomPush";

    @BeforeAll
    public static void setup() {
        PropertiesUtil.initPropertiesFromResourcePath(dicomServerProperties, ShUpConfig.DICOM_SERVER_PROPERTIES);
        remoteHost = dicomServerProperties.getProperty(ConfigBean.LOCAL_DICOM_SERVER_HOST);
        remotePort = dicomServerProperties.getProperty(ConfigBean.LOCAL_DICOM_SERVER_PORT);
        calledAET = dicomServerProperties.getProperty(ConfigBean.LOCAL_DICOM_SERVER_AET_CALLING);
    }

    @Test
    public void testDicomPush() {
        try {
            URL resource = getClass().getClassLoader().getResource("acr_phantom_t1");
            if (resource != null) {
                File phantomDir = new File(resource.toURI());
                if (phantomDir.isDirectory()) {
                    sendDicomFilesToShUp(phantomDir);
                }
            }
        } catch (Exception e) {
            LOG.error("Error in testDicomPush", e);
        }
    }

    private void sendDicomFilesToShUp(File phantomDir)
            throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        Device device = new Device(this.getClass().getName());
        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);
        ApplicationEntity ae = new ApplicationEntity(CALLING_AET);
        Connection calledConn = new Connection(this.getClass().getName(), remoteHost, Integer.valueOf(remotePort));
        device.addConnection(calledConn);
        device.addApplicationEntity(ae);
        ae.addConnection(calledConn);
        AAssociateRQ rq = new AAssociateRQ();
        rq.setCalledAET(calledAET);
        rq.setCallingAET(CALLING_AET);
        preparePresentationContext(phantomDir, rq);
        Association association = ae.connect(calledConn, rq);
        DimseRSPHandler handler = new DimseRSPHandler(association.nextMessageID()) {
            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(Tag.Status, -1);
                if (status != Status.Success) {
                    LOG.error("C-STORE failed for: with status: "
                            + TagUtils.shortToHexString(status));
                }
            }
        };
        sendDicomFiles(phantomDir, association, handler);
        association.release();
    }

    private void sendDicomFiles(File phantomDir, Association association, DimseRSPHandler handler)
            throws IOException, InterruptedException {
        for (File file : phantomDir.listFiles()) {
            try (DicomInputStream dis = new DicomInputStream(file)) {
                Attributes fmi = dis.readFileMetaInformation();
                Attributes data = dis.readDataset();
                String sopClassUID = fmi.getString(Tag.MediaStorageSOPClassUID);
                String sopInstanceUID = fmi.getString(Tag.MediaStorageSOPInstanceUID);
                String ts = fmi.getString(Tag.TransferSyntaxUID);
                association.cstore(
                        sopClassUID,
                        sopInstanceUID,
                        0,
                        new DataWriterAdapter(data),
                        ts,
                        handler);
            }
        }
    }

    private void preparePresentationContext(File phantomDir, AAssociateRQ rq) throws IOException {
        for (File file : phantomDir.listFiles()) {
            try (DicomInputStream dis = new DicomInputStream(file)) {
                Attributes attrs = dis.readDataset();
                String sopClassUID = attrs.getString(Tag.SOPClassUID);
                String ts = dis.getTransferSyntax();
                rq.addPresentationContext(new PresentationContext(
                        rq.getNumberOfPresentationContexts() * 2 + 1,
                        sopClassUID,
                        ts));
            }
        }
    }

}
