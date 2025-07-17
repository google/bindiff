if "%1" == "release" (
  echo Release build
) else (
  echo Continuous integration build
)

setlocal enableextensions

set BINDIFF_RELEASE=8
set BINEXPORT_RELEASE=12

set BUILD_DIR=%cd%\build
set GOOGLE3_DIR=%cd%\..\..\..
set THIRD_PARTY_DIR=%cd%\..\..

set BINDIFF_PKG_DIR=packaging\msi\SourceDir
set APP_DIR=%BINDIFF_PKG_DIR%\ProgramFiles\BinDiff
for %%I in ("%BUILD_DIR%\wix" ^
            "%APP_DIR%\bin" ^
            "%APP_DIR%\Extra\Config" ^
            "%APP_DIR%\Extra\Ghidra" ^
            "%APP_DIR%\Plugins\IDA Pro") do if not exist "%%I" mkdir "%%I"

:: Set tool paths
set SIGNTOOL="%ProgramFiles(x86)%\Windows kits\10\bin\x86\signtool.exe"
set JAVA_HOME=%BUILD_DIR%\zulu16.28.11-ca-jdk16.0.0-win_x64
set HEAT="%BUILD_DIR%\wix\heat.exe"
set CANDLE="%BUILD_DIR%\wix\candle.exe"
set LIGHT="%BUILD_DIR%\wix\light.exe"

:: Copy pre-built dependencies.
unzip -q "%KOKORO_GFILE_DIR%\zulu16.28.11-ca-jdk16.0.0-win_x64.zip" ^
  -d "%BUILD_DIR%" || exit /b
unzip -q "%KOKORO_GFILE_DIR%\wix311-binaries.zip" ^
  -d "%BUILD_DIR%\wix" || exit /b

:: Code-sign release artifacts
if "%1" neq "release" ^
  call :codesign "%KOKORO_GFILE_DIR%\*.exe" "%KOKORO_GFILE_DIR%\*.dll"

:: Copy latest release artifacts.
for %%I in (bindiff.exe ^
            bindiff_config_setup.exe ^
            bindiff.jar ^
            binexport2dump.exe) do copy /Y ^
  "%KOKORO_GFILE_DIR%\%%I" ^
  "%APP_DIR%\bin\" || exit /b
:: Omitting "\" in the "-d" argument below is intentional
unzip -q ^
  "%KOKORO_GFILE_DIR%\ghidra_BinExport.zip" ^
  -d "%APP_DIR%\Extra\Ghidra" || exit /b
for %%I in ("bindiff%BINDIFF_RELEASE%_ida.dll" ^
            "bindiff%BINDIFF_RELEASE%_ida64.dll" ^
            "binexport%BINEXPORT_RELEASE%_ida.dll" ^
            "binexport%BINEXPORT_RELEASE%_ida64.dll") do copy /Y ^
  "%KOKORO_GFILE_DIR%\%%I" ^
  "%APP_DIR%\Plugins\IDA Pro\" || exit /b
copy /Y ^
  bindiff_config.proto ^
  "%APP_DIR%\Extra\Config\" || exit /b
copy /Y ^
  bindiff.json ^
  "%BINDIFF_PKG_DIR%\CommonAppData\BinDiff\" || exit /b

:: Build bundle JRE
:: To gather the dependencies for the GUI jar file:
::   %JAVA_HOME%\bin\jdeps.exe --print-module-deps bindiff.jar
"%JAVA_HOME%\bin\jlink.exe" ^
  --module-path "%JAVA_HOME%\jmods" ^
  --no-header-files ^
  --compress=2 ^
  --strip-debug ^
  --add-modules java.base,java.desktop,java.prefs,java.scripting,java.sql,jdk.unsupported,jdk.xml.dom ^
  --output "%APP_DIR%\jre" || exit /b

:: Generate include file for bundled JRE and BinExport for Ghidra
%HEAT% dir "%APP_DIR%\jre" ^
  -nologo -ke -ag -cg BinDiff_Jre -dr INSTALLDIR -sw5150 -indent 2 ^
  -out packaging\msi\Jre.wxs || exit /b
%HEAT% dir "%APP_DIR%\Extra\Ghidra" ^
  -nologo -ke -ag -cg BinDiff_Extra_Ghidra -dr BinDiff_Extra -sw5150 -indent 2 ^
  -out packaging\msi\Extra_Ghidra.wxs || exit /b

:: Build MSI package
%CANDLE% -nologo ^
  -arch x64 ^
  -dProjectDir=packaging\msi\ ^
  -ext WixFirewallExtension ^
  -ext WixUtilExtension ^
  -o "%BUILD_DIR%\\" ^
  packaging\msi\Extra_Ghidra.wxs ^
  packaging\msi\Jre.wxs ^
  packaging\msi\Setup.wxs || exit /b
%LIGHT% -nologo ^
  -ext WixFirewallExtension ^
  -ext WixUtilExtension ^
  -ext WixUIExtension ^
  -cultures:en-us ^
  -sice:ICE03 -sice:ICE48 -sice:ICE50 -sice:ICE82 ^
  -o "%BUILD_DIR%\bindiff%BINDIFF_RELEASE%.msi" ^
  -b "%APP_DIR%\jre" ^
  -b "%APP_DIR%\Extra\Ghidra" ^
  "%BUILD_DIR%\Extra_Ghidra.wixobj" ^
  "%BUILD_DIR%\Jre.wixobj" ^
  "%BUILD_DIR%\Setup.wixobj" || exit /b

:: Release build, code sign the artifacts
@REM TODO(cblichmann): Re-enable code signing using a separate job.
@REM if "%1" neq "release" call :codesign "%BUILD_DIR%\*.msi"
@REM
@REM exit /b
@REM
@REM :: Code-signs the specified artifacts and verifies them
@REM :: %* artifacts to sign
@REM :codesign
@REM
@REM set ARTIFACTS=%*
@REM
@REM ksigntool sign GOOGLE_EXTERNAL /v /debug /t http://timestamp.digicert.com ^
@REM   %ARTIFACTS% || exit /b
@REM %SIGNTOOL% verify /pa /all %ARTIFACTS% || exit /b

exit /b
