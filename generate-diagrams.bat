@echo off
echo ========================================
echo   Generate Diagrams from PlantUML
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java JDK 8 or higher
    pause
    exit /b 1
)

REM Download PlantUML if not exists
if not exist "plantuml.jar" (
    echo [INFO] Downloading PlantUML...
    curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml-1.2024.0.jar
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to download PlantUML
        pause
        exit /b 1
    )
    echo [SUCCESS] PlantUML downloaded
    echo.
)

REM Generate Architecture Diagram
echo [INFO] Generating System Architecture Diagram...
java -jar plantuml.jar -tpng ARCHITECTURE_DIAGRAM.puml
if %errorlevel% equ 0 (
    echo [SUCCESS] architecture_diagram.png created
) else (
    echo [ERROR] Failed to generate architecture diagram
)
echo.

REM Generate Database ERD
echo [INFO] Generating Database ERD...
java -jar plantuml.jar -tpng DATABASE_ERD.puml
if %errorlevel% equ 0 (
    echo [SUCCESS] database_erd.png created
) else (
    echo [ERROR] Failed to generate database ERD
)
echo.

REM Generate SVG versions (high quality)
echo [INFO] Generating SVG versions (high quality)...
java -jar plantuml.jar -tsvg ARCHITECTURE_DIAGRAM.puml
java -jar plantuml.jar -tsvg DATABASE_ERD.puml
echo [SUCCESS] SVG files created
echo.

echo ========================================
echo   Generation Complete!
echo ========================================
echo.
echo Files created:
echo   - ARCHITECTURE_DIAGRAM.png
echo   - DATABASE_ERD.png
echo   - ARCHITECTURE_DIAGRAM.svg
echo   - DATABASE_ERD.svg
echo.
pause
