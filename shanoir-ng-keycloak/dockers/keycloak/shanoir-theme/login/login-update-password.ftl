<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))}
    <#elseif section = "form">
        <div class="login-main"> 
            <form id="kc-passwd-update-form" action="${url.loginAction}" method="post">
                <div class="header command-zone">${msg("updatePasswordTitle")}</div>
                <fieldset>
                    <ol>
                        <li class="instructions">
                            ${msg("newPasswordInstruction")}
                        </li>
                        <li>
                            <input type="password" id="password-new" name="password-new" autofocus autocomplete="off" placeholder="${msg("password")}" />
                        </li>
                        <li>
                            <input type="password" id="password-confirm" name="password-confirm" class="${properties.kcInputClass!}" autocomplete="off" placeholder="${msg("passwordConfirm")}"/>
                        </li>
                    </ol>
                    <ol>
                        <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                            <div class="${properties.kcFormOptionsWrapperClass!}">
                            </div>
                        </div>
                    </ol>
                </fieldset>

                <div  class="footer command-zone">
                    <div>
                        <div id="kc-form-buttons">						
                            <div>
                                <button>${msg("doSubmit")}</button>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
		</div>
    </#if>
</@layout.registrationLayout>