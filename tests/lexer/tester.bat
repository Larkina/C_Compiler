@echo off
for %%i in (*.in) do java -jar C_Compiler.jar %%i > %%~ni.ans
for %%i in (*.out) do diff %%i %%~ni.ans && if ERRORLEVEL 0 echo OK
