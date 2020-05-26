@ECHO OFF

IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
	ECHO Starting ShanoirUploader application
	"%JAVA_HOME%\bin\javaw.exe" -jar shanoir-uploader-6.0.3-jar-with-dependencies.jar -Xms128m -Xmx512m -Xnoclassgc org.shanoir.uploader.ShanoirUploader  
) ELSE (
	java.exe -version >nul 2>&1
	IF %ERRORLEVEL% NEQ 0 (
		echo ERROR : Java is not properly installed or configured on your machine
		pause
	) ELSE (
		javaw -jar shanoir-uploader-6.0.3-jar-with-dependencies.jar -Xms128m -Xmx512m -Xnoclassgc org.shanoir.uploader.ShanoirUploader
	)
)
