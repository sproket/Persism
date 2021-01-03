FOR /d /r . %%d IN (".svn") DO @IF EXIST "%%d" rd /s /q "%%d"
REM ECHO %%d
