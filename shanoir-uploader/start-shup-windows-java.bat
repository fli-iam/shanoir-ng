@ECHO OFF
setlocal enabledelayedexpansion

FOR %%f in (shanoir-uploader-*-jar-with-dependencies.jar) do (
    set "JAR_FILE=%%f"
    goto :found
)

ECHO ERROR: No JAR file found with the pattern shanoir-uploader-*-jar-with-dependencies.jar
EXIT /b 1

:found
IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
	ECHO Starting ShanoirUploader using JAVA_HOME...
	"%JAVA_HOME%\bin\javaw.exe" -Dhttps.protocols=TLSv1.2 -Xms512m -Xmx2g -Xnoclassgc -jar "%JAR_FILE%" org.shanoir.uploader.ShanoirUploader  
) ELSE (
	java.exe -version >nul 2>&1
	IF %ERRORLEVEL% NEQ 0 (
		ECHO ERROR : Java is not properly installed or configured on your machine
		pause
	) ELSE (
		ECHO Starting ShanoirUploader without JAVA_HOME...
		javaw -Dhttps.protocols=TLSv1.2 -Xms512m -Xmx2g -Xnoclassgc -jar "%JAR_FILE%" org.shanoir.uploader.ShanoirUploader
	)
)
