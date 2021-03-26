@ECHO OFF

IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
	ECHO Starting ShanoirUploader application
    ECHO %*
	"%JAVA_HOME%\bin\javaw.exe" -jar shanoir-uploader-7.0.1-jar-with-dependencies.jar org.shanoir.downloader.ShanoirDownloader %*
) ELSE (
	java.exe -version >nul 2>&1
	IF %ERRORLEVEL% NEQ 0 (
		echo ERROR : Java is not properly installed or configured on your machine
		pause
	) ELSE (
		javaw -jar shanoir-uploader-7.0.1-jar-with-dependencies.jar org.shanoir.downloader.ShanoirDownloader %*
	)
)
