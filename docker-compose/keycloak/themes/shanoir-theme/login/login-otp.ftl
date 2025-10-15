<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp') displayInfo=true displayWide=false>
    <form id="kc-otp-login-form" action="${url.loginAction}" method="post">
        <div class="form-group">
            <label for="otp">${msg("loginOtpOneTime")}</label>
            <input type="text" id="otp" name="otp" autocomplete="off" autofocus />
        </div>

        <div class="form-actions">
            <input type="submit" value="${msg("doLogIn")}" />
        </div>
    </form>
</@layout.registrationLayout>
