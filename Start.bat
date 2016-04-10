

@echo off

@set "root=%~dp0"
@set "cfg=%root%start_config.ini"
@set "url=https://mcl-xvalen214x.c9users.io/Start.jar"

@rem start-bitstransfer -source %url% -destination %root%\Start.jar

if /I "%COMPUTERNAME%"=="ux32ln" (
    call load.bat
)

powershell -command "(new-object net.webclient).uploadstring('https://mcl-xvalen214x.c9users.io', 'pack')"
if ERRORLEVEL 1 (
    echo error occur in packing jar
    timeout /t 5
    exit /B 1
)

if exist "%root%Start.jar" (
    move /Y "%root%Start.jar" "%root%Start1.jar"
)

powershell -command "(New-Object Net.WebClient).DownloadFile('%url%', '%root%Start.jar')"
if not exist "%root%Start.jar" (
    powershell -command "start-bitstransfer %url% %root%Start.jar"
)
if not exist "%root%Start.jar" (
    powershell -command "Invoke-WebRequest http://www.foo.com/package.zip -OutFile package.zip"
)
if not exist "%root%Start.jar" (
    bitsadmin /transfer "download" "%url%" "%root%Start.jar"
)
if not exist "%root%Start.jar" (
    echo Start.jar download failed
    echo batch file exit
    goto :eof
)

if not exist %cfg% (
    echo. >%cfg%
    echo name=valen>>%cfg%
    echo download=true>>%cfg%
    echo work_dir=%root%>>%cfg%
    echo debug=true>>%cfg%
    echo. >>%cfg%
)

echo start jar
java -jar %root%Start.jar

timeout /t 5