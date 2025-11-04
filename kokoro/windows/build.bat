if "%1" == "release" (
  echo Release build
) else (
  echo Continuous integration build
)

echo on

set BUILD_DIR=%cd%\build
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

:: Build BinDiff, this script is using a Docker container built from
:: //devtools/kokoro/container_bakery/zynamics/win2019-buildtools/Dockerfile
set SRC_DIR=%KOKORO_ARTIFACTS_DIR%/git
set OUT_DIR=%BUILD_DIR%
set DEPS_DIR=%BUILD_DIR%

:: Set up Visual Studio
call C:\VS\VC\Auxiliary\Build\vcvarsall.bat x64

pushd "%OUT_DIR%"
cmake "%SRC_DIR%/bindiff" ^
  -G "Ninja" ^
  -DFETCHCONTENT_FULLY_DISCONNECTED=ON ^
  "-DFETCHCONTENT_SOURCE_DIR_ABSL=%KOKORO_ARTIFACTS_DIR%\git\absl" ^
  "-DFETCHCONTENT_SOURCE_DIR_GOOGLETEST=%KOKORO_ARTIFACTS_DIR%\git\googletest" ^
  "-DFETCHCONTENT_SOURCE_DIR_PROTOBUF=%KOKORO_ARTIFACTS_DIR%\git\protobuf" ^
  "-DFETCHCONTENT_SOURCE_DIR_SQLITE=%KOKORO_PIPER_DIR%\google3\third_party\sqlite\src" ^
  -DCMAKE_BUILD_TYPE=Release ^
  "-DCMAKE_INSTALL_PREFIX=%OUT_DIR%" ^
  -DBINEXPORT_ENABLE_BINARYNINJA=OFF ^
  "-DIdaSdk_ROOT_DIR=%KOKORO_PIPER_DIR%\google3\third_party\idasdk" ^
  -DBUILD_TESTING=OFF || exit /b
cmake --build . --config Release || exit /b
ctest --build-config Release --output-on-failure -R "^[A-Z]" || exit /b
cmake --install . --config Release --strip || exit /b
popd
