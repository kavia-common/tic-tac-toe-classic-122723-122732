@echo off
REM Delegates Gradle commands to the Android app's wrapper
setlocal
set SCRIPT_DIR=%~dp0
pushd "%SCRIPT_DIR%\tic_tac_toe_android_app"
call gradlew.bat %*
popd
endlocal
