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

package org.shanoir.ng.storage;

import io.awspring.cloud.autoconfigure.s3.S3ClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.services.s3.LegacyMd5Plugin;

/**
 * Since AWS SDK 2.30, operations requiring a checksum (e.g. DeleteObjects)
 * send a CRC32 checksum instead of Content-MD5. S3-compatible stores such as
 * S3 Ninja, Ceph or older MinIO reject these requests ("The Content-MD5 you
 * specified did not match what we received"). Restore the legacy MD5 behavior
 * and only compute checksums when the operation requires them.
 */
@Configuration
public class S3ClientConfiguration {

    @Bean
    public S3ClientCustomizer legacyMd5S3ClientCustomizer() {
        return builder -> builder
                .addPlugin(LegacyMd5Plugin.create())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);
    }

}
