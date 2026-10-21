<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-otp-reset-form" class="${properties.kcFormClass!}" action="${url.loginAction}"
              method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcInputWrapperClass!}">
                    <p id="kc-otp-reset-form-description">${msg("otp-reset-description")}</p>

                    <div class="shanoir-otp-list" role="radiogroup">
                        <#list configuredOtpCredentials.userOtpCredentials as otpCredential>
                            <input id="kc-otp-credential-${otpCredential?index}" class="shanoir-otp-radio" type="radio" name="selectedCredentialId" value="${otpCredential.id}" <#if otpCredential.id == configuredOtpCredentials.selectedCredentialId>checked="checked"</#if>>
                            <label for="kc-otp-credential-${otpCredential?index}" class="shanoir-otp-tile">
                                <span class="shanoir-otp-tile-check" aria-hidden="true"></span>
                                <span class="shanoir-otp-tile-title"><#if otpCredential.userLabel?has_content>${otpCredential.userLabel}<#else>${msg("otpDeviceUnnamed", otpCredential?index + 1)}</#if></span>
                            </label>
                        </#list>
                    </div>

                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                            <input id="kc-otp-reset-form-submit" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
