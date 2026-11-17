@echo off
start javaw -jar "dist\laundry.jar"
if %errorlevel% neq 0 (
    java -jar "dist\laundry.jar"
    pause
)
