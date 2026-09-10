@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo  JeecgBoot Weather Platform - Dev Launcher
echo ============================================

REM ---------- Frontend package manager ----------
REM Corepack may keep an incomplete pnpm version in its cache. Prefer the
REM complete pnpm 11 CLI already installed on this machine.
set "PNPM_CLI=%LOCALAPPDATA%\node\corepack\v1\pnpm\11.16.0\bin\pnpm.cjs"

REM ---------- 1. Make sure MySQL / Redis services are running ----------
for %%S in (MySQL Redis) do (
  sc query %%S | findstr /C:"RUNNING" >nul 2>&1
  if errorlevel 1 (
    echo [service] %%S is not running, starting...
    net start %%S >nul 2>&1
  ) else (
    echo [service] %%S is running
  )
)

REM ---------- 2. Check backend port 8880 ----------
netstat -ano | findstr /C:":8880 " | findstr /C:"LISTENING" >nul 2>&1
if errorlevel 1 (
  if not exist "%~dp0jeecg-boot\jeecg-module-system\jeecg-system-start\target\jeecg-system-start-3.9.5.jar" (
    echo [backend] jar not found, building project first...
    pushd "%~dp0jeecg-boot"
    call mvn -f pom.xml -pl jeecg-module-system/jeecg-system-start -am package -Pdev -DskipTests
    popd
  )
  if exist "%~dp0jeecg-boot\jeecg-module-system\jeecg-system-start\target\jeecg-system-start-3.9.5.jar" (
    echo [backend] starting at http://localhost:8880/jeecg-boot
    start "JeecgBoot Backend" /D "%~dp0jeecg-boot\jeecg-module-system\jeecg-system-start\target" cmd /k "java -jar jeecg-system-start-3.9.5.jar"
  ) else (
    echo [backend] build failed, please check the Maven log above.
  )
) else (
  echo [backend] port 8880 is already running
)

REM ---------- 3. Install frontend dependencies if needed ----------
if not exist "%~dp0jeecgboot-vue3\node_modules" (
  echo [frontend] installing dependencies for the first time...
  pushd "%~dp0jeecgboot-vue3"
  if exist "%PNPM_CLI%" (
    call node "%PNPM_CLI%" install
  ) else (
    call pnpm install
  )
  popd
)

REM ---------- 4. Check frontend port 3111 ----------
netstat -ano | findstr /C:":3111 " | findstr /C:"LISTENING" >nul 2>&1
if errorlevel 1 (
  echo [frontend] starting at http://localhost:3111
  if exist "%PNPM_CLI%" (
    start "JeecgBoot Frontend" /D "%~dp0jeecgboot-vue3" cmd /k ""node" "%PNPM_CLI%" dev"
  ) else (
    start "JeecgBoot Frontend" /D "%~dp0jeecgboot-vue3" cmd /k "pnpm dev"
  )
) else (
  echo [frontend] port 3111 is already running
)

echo.
echo Done. Open the following URLs after services are ready:
echo   Frontend: http://localhost:3111
echo   Backend : http://localhost:8880/jeecg-boot
echo.
echo Close the Backend/Frontend windows to stop each service.
endlocal
