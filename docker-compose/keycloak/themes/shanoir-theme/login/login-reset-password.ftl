<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))}
    <#elseif section = "form">
        <div class="login-main"> 
            <form id="kc-reset-password-form" action="${url.loginAction}" method="post">
                <div class="header command-zone">${msg("emailForgotTitle")}</div>
                <fieldset>
                    <ol>
                        <li class="instructions">
                            ${msg("emailInstruction")}
                        </li>
                        <li>
                            <input type="text" id="username" name="username" class="${properties.kcInputClass!}" autofocus placeholder="<#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>"/>
                        </li>
                    </ol>
                </fieldset>

                <div  class="footer command-zone">
                    <div>
                        <div id="kc-form-buttons">						
                            <div>
                                <button>${msg("doSubmit")}</button>
                            </div>
                            <div>
                                <a href="${url.loginUrl}">${msg("backToLogin")}</a>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
		</div>
    </#if>
</@layout.registrationLayout>
