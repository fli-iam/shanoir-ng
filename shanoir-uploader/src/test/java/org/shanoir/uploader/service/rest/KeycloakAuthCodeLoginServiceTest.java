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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.OtpDevice;

class KeycloakAuthCodeLoginServiceTest {

    /** OTP page as rendered by shanoir-theme/login/login-otp.ftl for two devices. */
    private static final String TWO_DEVICES_PAGE = """
        <form id="kc-otp-login-form" class="form" action="https://shanoir/login-actions/authenticate?session_code=a&amp;tab_id=b" method="post">
            <div class="shanoir-otp-list" role="radiogroup">
                <input id="kc-otp-credential-0" class="shanoir-otp-radio" type="radio" name="selectedCredentialId" value="11111111-aaaa" checked="checked">
                <label for="kc-otp-credential-0" class="shanoir-otp-tile">
                    <span class="shanoir-otp-tile-check" aria-hidden="true"></span>
                    <span class="shanoir-otp-tile-title">Work phone</span>
                </label>
                <input id="kc-otp-credential-1" class="shanoir-otp-radio" type="radio" name="selectedCredentialId" value="22222222-bbbb" >
                <label for="kc-otp-credential-1" class="shanoir-otp-tile">
                    <span class="shanoir-otp-tile-check" aria-hidden="true"></span>
                    <span class="shanoir-otp-tile-title">Marie&#39;s tablet</span>
                </label>
            </div>
            <input id="otp" name="otp" autocomplete="one-time-code" type="text" />
            <input type="hidden" id="id-hidden-input" name="credentialId" />
        </form>
        """;

    /** OTP page for a single device: the template renders no device list. */
    private static final String ONE_DEVICE_PAGE = """
        <form id="kc-otp-login-form" class="form" action="https://shanoir/login-actions/authenticate" method="post">
            <input id="otp" name="otp" autocomplete="one-time-code" type="text" />
            <input type="hidden" id="id-hidden-input" name="credentialId" />
        </form>
        """;

    @Test
    void extractOtpDevicesListsEveryDeviceWithItsLabelAndDefault() {
        List<OtpDevice> devices = KeycloakAuthCodeLoginService.extractOtpDevices(TWO_DEVICES_PAGE);

        assertEquals(List.of(
            new OtpDevice("11111111-aaaa", "Work phone", true),
            new OtpDevice("22222222-bbbb", "Marie's tablet", false)
        ), devices);
    }

    @Test
    void extractOtpDevicesIsEmptyForSingleDevice() {
        assertTrue(KeycloakAuthCodeLoginService.extractOtpDevices(ONE_DEVICE_PAGE).isEmpty());
    }

}
