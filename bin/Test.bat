

@echo off

@set "root=%~dp0"
@set "url=https://mcl-xvalen214x.c9users.io/Test.class"

@rem start-bitstransfer -source %url% -destination %root%\Test.class

if /I "%COMPUTERNAME%"=="ux32ln" (
    call load.bat
)

powershell -command "$output = (new-object net.webclient).uploadstring('https://mcl-xvalen214x.c9users.io/command', 'test') ; echo $output ; if (-not $output -eq '') {exit 1}"
if ERRORLEVEL 1 (
    echo error occur in compiling Test.java
    timeout /t 5
    exit /B 1
)

if exist "%root%Test.class" (
    move /Y "%root%Test.class" "%root%Start1.jar"
)

powershell -command "(New-Object Net.WebClient).DownloadFile('%url%', '%root%Test.class')"
if not exist "%root%Test.class" (
    powershell -command "start-bitstransfer %url% %root%Test.class"
)
if not exist "%root%Test.class" (
    powershell -command "Invoke-WebRequest http://www.foo.com/package.zip -OutFile package.zip"
)
if not exist "%root%Test.class" (
    bitsadmin /transfer "download" "%url%" "%root%Test.class"
)
if not exist "%root%Test.class" (
    echo Test.class download failed
    echo batch file exit
    goto :eof
)

echo start Test
java -cp "%root%"" Test
