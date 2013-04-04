REM PGPASSWORD - superuser password
REM postgresFromDir - old server's installation directory
REM postgresToDir - new server's installation directory
REM postgresHost - old and new servers' host
REM postgresFromPort - old server's port
REM postgresToPort - new server's port
REM databaseName - name of the database you want to migrate
set PGPASSWORD=sdl34kapht
set postgresFromDir=C:\Program Files\PostgreSQL\9.0
set postgresToDir=C:\Program Files\PostgreSQL\9.2
set postgresHost=127.0.0.1
set postgresFromPort=5432
set postgresToPort=10092
set databaseName=moloco

REM Reloading PostgreSQL configuration
REM "%postgresFromDir%\bin\pg_ctl" -D "%postgresFromDir%\data" reload

REM Dumping roles
"%postgresFromDir%\bin\pg_dumpall" --host=%postgresHost% --port=%postgresFromPort% --database=%databaseName% --file="%postgresFromDir%\roles.backup" --username=postgres --globals-only

REM Adding CREATE DATABASE SQL to roles dump
rename "%postgresFromDir%\roles.backup" roles.backup.temp
echo CREATE DATABASE %databaseName%; > "%postgresFromDir%\roles.backup"
type "%postgresFromDir%\roles.backup.temp" >> "%postgresFromDir%\roles.backup"
del "%postgresFromDir%\roles.backup.temp"
copy "%postgresFromDir%\roles.backup" f:\

REM Dumping database
"%postgresFromDir%\bin\pg_dump" --create --host=%postgresHost% --port=%postgresFromPort% --format=tar --file="f:/%databaseName%.backup" --username=postgres %databaseName%

REM Importing roles to the new PostgreSQL server. Assuming that superuser name and password are the same
REM Also creating new database
"%postgresToDir%\bin\psql" --username=postgres -f "%postgresFromDir%\roles.backup" --host=%postgresHost% --port=%postgresToPort% postgres

REM Importing this database to new PostgreSQL server. Assuming that superuser name and password are the same
"%postgresToDir%\bin\pg_restore" --dbname=%databaseName% --format=tar --username=postgres --host=%postgresHost% --port=%postgresToPort% "%postgresFromDir%\%databaseName%.backup"