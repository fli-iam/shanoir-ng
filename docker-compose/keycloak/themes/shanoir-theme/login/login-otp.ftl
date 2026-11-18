<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-otp-login-form" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}"
            method="post">
            <#if otpLogin.userOtpCredentials?size gt 1>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputWrapperClass!}">
                        <div class="shanoir-otp-list" role="radiogroup">
                            <#list otpLogin.userOtpCredentials as otpCredential>
                                <input id="kc-otp-credential-${otpCredential?index}" class="shanoir-otp-radio" type="radio" name="selectedCredentialId" value="${otpCredential.id}" <#if otpCredential.id == otpLogin.selectedCredentialId>checked="checked"</#if>>
                                <label for="kc-otp-credential-${otpCredential?index}" class="shanoir-otp-tile">
                                    <span class="shanoir-otp-tile-check" aria-hidden="true"></span>
                                    <span class="shanoir-otp-tile-title"><#if otpCredential.userLabel?has_content>${otpCredential.userLabel}<#else>${msg("otpDeviceUnnamed", otpCredential?index + 1)}</#if></span>
                                </label>
                            </#list>
                        </div>
                    </div>
                </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <#if messagesPerField.existsError('totp')>
                    <span id="input-error" class="alert ${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('totp'))?no_esc}
                    </span>
                </#if>
                <div class="${properties.kcInputWrapperClass!}">
                    <input id="otp" name="otp" autocomplete="one-time-code" type="text" class="${properties.kcInputClass!}"
                            placeholder="${msg("loginOtpOneTime")}"
                            autofocus aria-invalid="<#if messagesPerField.existsError('totp')>true</#if>"
                            dir="ltr" />
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!} ">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    <a href="${properties.requestWelcome!}">
						<button type="button">${msg("doCancel")}</button>
					</a>
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit">${msg("doSubmit")}</button>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>