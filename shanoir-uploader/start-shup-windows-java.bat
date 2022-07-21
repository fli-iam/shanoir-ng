@ECHO OFF

IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
	ECHO Starting ShanoirUploader using JAVA_HOME...
	"%JAVA_HOME%\bin\javaw.exe" -Dhttps.protocols=TLSv1.2 -Xms128m -Xmx512m -Xnoclassgc -jar shanoir-uploader-7.0.1-jar-with-dependencies.jar org.shanoir.uploader.ShanoirUploader  
) ELSE (
	java.exe -version >nul 2>&1
	IF %ERRORLEVEL% NEQ 0 (
		ECHO ERROR : Java is not properly installed or configured on your machine
		pause
	) ELSE (
		ECHO Starting ShanoirUploader without JAVA_HOME...
		javaw -Dhttps.protocols=TLSv1.2 -Xms128m -Xmx512m -Xnoclassgc -jar shanoir-uploader-7.0.1-jar-with-dependencies.jar org.shanoir.uploader.ShanoirUploader
	)
)
