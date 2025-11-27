@echo off
echo Running migration to add PENDING_PAYMENT status...
echo.

mysql -u root web2 < migration_add_pending_payment_status.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Migration completed successfully!
    echo.
    echo You can now restart your Spring Boot application.
) else (
    echo.
    echo ❌ Migration failed!
    echo Please check your MySQL connection and try again.
)

pause
