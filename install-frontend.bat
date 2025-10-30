@echo off
echo Installing frontend dependencies...
cd /d "C:\Users\Quang\Desktop\Đồ án TN\Do-an-tot-nghiep\src\frontend"
echo Current directory: %CD%
echo.
echo Checking Node.js version:
node --version
echo.
echo Checking npm version:
npm --version
echo.
echo Installing packages...
npm install
echo.
echo Installation completed!
echo.
echo To start the development server, run:
echo npm run dev
echo.
pause
