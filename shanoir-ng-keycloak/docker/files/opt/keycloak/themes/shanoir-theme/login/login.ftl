<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section, displayMessage>
    <#if section = "title">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))}
    <#elseif section = "form">
		<div class="login-main"> 
			<#if realm.password>
				<form id="kc-form-login" action="${url.loginAction}" method="post">
					<div class="header command-zone">Connect to Shanoir ${properties.instance}</div>
					<fieldset>
						<ol>
							<li>
								<#if usernameEditDisabled??>
									<input id="username" name="username" value="${(login.username!'')?html}" type="text" disabled />
								<#else>
									<input id="username" name="username" value="${(login.username!'')?html}" type="text" autofocus autocomplete="off" 
									placeholder ="<#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>"/>
								</#if>
							</li>
							<li>
								<input id="password" name="password" type="password" autocomplete="off" placeholder="${msg("password")}"/>
							</li>
						</ol>
					</fieldset>

					<div  class="footer command-zone">
						<div id="kc-form-options">
							<#if realm.rememberMe && !usernameEditDisabled??>
								<div class="checkbox">
									<label>
										<#if login.rememberMe??>
											<input id="rememberMe" name="rememberMe" type="checkbox" tabindex="3" checked> ${msg("rememberMe")}
										<#else>
											<input id="rememberMe" name="rememberMe" type="checkbox" tabindex="3"> ${msg("rememberMe")}
										</#if>
									</label>
								</div>
							</#if>
							<div>
								<#if realm.resetPasswordAllowed>
									<span><a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
								</#if>
							</div>
						</div>
	
						<div>
							<#if displayMessage?has_content && displayMessage && message?has_content>
								<div class="error">
									<div class="alert alert-${message.type}">
										<#if message.type = 'success'></#if>
										<#if message.type = 'warning'></#if>
										<#if message.type = 'error'></#if>
										<#if message.type = 'info'></#if>
										<span class="kc-feedback-text">${message.summary}</span>
									</div>
								</div>
							</#if>
							<div id="kc-form-buttons">						
								<div>
									<button name="login" id="kc-login" type="submit">${msg("doLogIn")}</button>
								</div>
								<div>
									<a href="${properties.requestAccount!}">Create an account</a>
								</div>
							</div>
						</div>
					</div>
				</form>
			</#if>
		</div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !usernameEditDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>

        <#if realm.password && social.providers??>
            <div id="kc-social-providers">
                <ul>
                    <#list social.providers as p>
                        <li><a href="${p.loginUrl}" id="zocial-${p.alias}" class="zocial ${p.providerId}"> <span class="text">${p.displayName}</span></a></li>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
