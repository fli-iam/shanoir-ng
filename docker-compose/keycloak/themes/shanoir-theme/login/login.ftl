<#-- Shanoir custom login page -->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password displayWide=true>
    <#if realm.password>
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div class="form-group">
                <label for="username">${msg("usernameOrEmail")}</label>
                <input type="text" id="username" name="username" value="${username}" autofocus autocomplete="off" />
            </div>

            <div class="form-group">
                <label for="password">${msg("password")}</label>
                <input type="password" id="password" name="password" autocomplete="off" />
            </div>

            <div class="form-actions">
                <input type="submit" value="${msg("doLogIn")}" />
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
