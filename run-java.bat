@echo off
title Weavo Artisan Marketplace - Java Syllabus Verification Suite
echo ========================================================================
echo    WEAVO ARTISAN MARKETPLACE - JAVA 5-PILLAR VERIFICATION SUITE
echo                  From the loom to your living room
echo ========================================================================
echo.
echo Verifying Java Runtime Environment...
java -version
if %errorlevel% neq 0 (
    echo [ERROR] Java JRE/JDK is not found on PATH. Please install Java 17+ or set JAVA_HOME.
    pause
    exit /b %errorlevel%
)
echo.
echo ========================================================================
echo  Java Source Code Structure for Academic Evaluation:
echo   1. OOP Design               : weavo-app\weavo-java-core\src\main\java\com\weavo\model\
echo   2. Inheritance & Interfaces : User hierarchy and PaymentGateway interface
echo   3. Custom Exceptions        : weavo-app\weavo-java-core\src\main\java\com\weavo\exception\
echo   4. Multithreading           : weavo-app\weavo-java-core\src\main\java\com\weavo\concurrency\
echo   5. File Read & Write        : weavo-app\weavo-java-core\src\main\java\com\weavo\fileio\
echo.
echo  Enterprise Multi-Module Code : weavo-app\kalasetu-domain, kalasetu-payment,
echo                                kalasetu-concurrency, kalasetu-fileio, kalasetu-web
echo ========================================================================
echo.
echo Complete documentation guide: JAVA_SYLLABUS_ARCHITECTURE_GUIDE.md
echo.
pause
