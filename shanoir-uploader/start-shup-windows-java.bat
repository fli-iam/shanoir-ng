:: Shanoir NG - Import, manage and share neuroimaging data
:: Copyright (C) 2009-2019 Inria - https://www.inria.fr/
:: Contact us on https://project.inria.fr/shanoir/
:: 
:: This program is free software: you can redistribute it and/or modify
:: it under the terms of the GNU General Public License as published by
:: the Free Software Foundation, either version 3 of the License, or
:: (at your option) any later version.
:: 
:: You should have received a copy of the GNU General Public License
:: along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

@ECHO OFF
setlocal enabledelayedexpansion

FOR %%f in (shanoir-uploader-*-jar-with-dependencies.jar) do (
    set "JAR_FILE=%%f"
    goto :found
)

ECHO ERROR: No JAR file found with the pattern shanoir-uploader-*-jar-with-dependencies.jar
EXIT /b 1

:found
FOR /F "tokens=3 delims=-" %%v IN ("!JAR_FILE!") DO (
    set "APP_VERSION=v%%v"
)

IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
	ECHO Starting ShanoirUploader using JAVA_HOME...
	"%JAVA_HOME%\bin\javaw.exe" -Dhttps.protocols=TLSv1.2 -Xms512m -Xmx2g -Xnoclassgc -Dapp.version=!APP_VERSION! -jar "!JAR_FILE!" org.shanoir.uploader.ShanoirUploader  
) ELSE (
	java.exe -version >nul 2>&1
	IF %ERRORLEVEL% NEQ 0 (
		ECHO ERROR : Java is not properly installed or configured on your machine
		pause
	) ELSE (
		ECHO Starting ShanoirUploader without JAVA_HOME...
		javaw -Dhttps.protocols=TLSv1.2 -Xms512m -Xmx2g -Xnoclassgc -Dapp.version=!APP_VERSION! -jar "!JAR_FILE!" org.shanoir.uploader.ShanoirUploader
	)
)
