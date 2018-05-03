@ECHO OFF
if exist { "%JAVA_HOME%\bin\java" } (
    set "JAVA="%JAVA_HOME%\bin\java""
)
java -cp %cd%/classes  com.Main
Pause