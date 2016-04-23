
@echo.
powershell -command "$output = (new-object net.webclient).uploadstring('https://mcl-xvalen214x.c9users.io/command', 'pack') ; echo $output ; if (-not $output -eq '') {exit 1}"
@if ERRORLEVEL 1 (
    @echo error in packing jar
)
@echo.
@set /P "payload=commit: "

powershell -command "$output = (new-object net.webclient).uploadstring('https://mcl-xvalen214x.c9users.io/push', '%payload%')"
