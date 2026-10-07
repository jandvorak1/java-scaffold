# Java Webapp project template with Maven

## Prerequisities

- Liberica Native Image Kit 25 (Java 25) JDK Full or higher
- Maven 3.8 or higher
- Powershell 5 or higher (for Windows only)
- VC++ Redistributable 14.0 or higher (for Windows only)

## Creating a project

```console
cookiecutter . --directory java-webapp --output-dir /home/jan/Projekty/Java
cookiecutter . --directory java-webapp --output-dir d:/Dokumenty/Development/Projekty/Java
```
## Creating a Github repository

```console
cd ~/Projekty/Java/java-scaffold
git init
git remote add origin git@github.com:jandvorak1/java-scaffold.git
git add . -A
git commit -m 'Initial commit with all files'
git push -u origin master
```
## Creating a Bitbucket repository

```console
cd ~/Projekty/Java/java-scaffold
git init
git remote add origin git@bitbucket.org:dvorak1/java-scaffold.git
git add . -A
git commit -m 'Initial commit with all files'
git push -u origin master
```
## Application properties

Application configuration properties, their default values, and descriptions.

| Property | Default | Description |
|----------|---------|-------------|
| `logs.console.level` | `INFO` | Console logging level. |
| `logs.file.level` | `INFO` | File logging level. |
| `logs.file.size` | 1024000 | Maximum log file size in bytes before rotation. |
| `logs.file.rotate` | 5 | Number of rotated log files to retain. |
| `logs.file.path` | `$XDG_STATE_HOME/java-scaffold` | Log file path; when unset, the platform default is used (Linux). |
| `logs.file.path` | `%LOCALAPPDATA%/java-scaffold` | Log file path; when unset, the platform default is used (Windows). |
| `logs.file.path` | `$HOME/Library/Logs/java-scaffold` | Log file path; when unset, the platform default is used (MacOS). |
| `server.host` | `localhost` | HTTP server host name or bind address. |
| `server.port` | 3000 | HTTP server port. |
| `server.timeout` | 5000 | Server timeout in milliseconds. |
| `server.origins.allowed` | null | Additional origins allowed to access the server. |
| `server.origins.same` | `http://localhost:3000` | Origin treated as the application's same origin. |
| `settings.locale` | `cs_CZ` | Application locale used for localization and formatting. |
| `settings.format.decimal` | `space_comma` | Decimal number formatting convention. |
| `settings.format.date` | `dd_mm_yyyy_dot` | Date formatting convention. |
| `settings.format.time` | `hh_mm_ss_24` | Time formatting convention. |
| `settings.format.datetime` | `dd_mm_yyyy_dot_hh_mm_ss` | Date and time formatting convention. |
| `settings.timezone` | `Europe/Prague` | Time zone used by the application. |
| `settings.touch` | false | Enables touch-oriented user interface behavior. |
| `settings.theme` | `sap_fiori_3` | Color theme for the application |
| `duckdb.database.path` | `$XDG_DATA_HOME"/java-scaffold/duckdb` | Directory for the persistent DuckDB database (Linux). |
| `duckdb.database.path` | `%APPDATA%/java-scaffold/duckdb` | Directory for the persistent DuckDB database (Windows). |
| `duckdb.database.path` | `$HOME/Library/Application Support/java-scaffold/duckdb` | Directory for the persistent DuckDB database (MacOS). |
| `duckdb.library.path` | `$XDG_DATA_HOME"/java-scaffold/duckdb_native` | Directory where the native DuckDB library is extracted (Linux). |
| `duckdb.library.path` | `%APPDATA%/java-scaffold/duckdb_native` | Directory where the native DuckDB library is extracted (Windows). |
| `duckdb.library.path` | `$HOME/Library/Application Support/java-scaffold/duckdb_native` | Directory where the native DuckDB library is extracted (MacOS). |
| `duckdb.temp.path` | system temporary directory | Directory for DuckDB temporary files. |
| `duckdb.temp.size` | 90% available storage | Maximum size in bytes of DuckDB temporary storage. |
| `sqlite.database.path` | `$XDG_DATA_HOME"/java-scaffold/sqlite` | Directory for the persistent SQLite database (Linux). |
| `sqlite.database.path` | `%APPDATA%/java-scaffold/sqlite` | Directory for the persistent SQLite database (Windows). |
| `sqlite.database.path` | `$HOME/Library/Application Support/java-scaffold/sqlite` | Directory for the persistent SQLite database (MacOS). |
| `sqlite.backup.path` | `$XDG_DATA_HOME"/java-scaffold/sqlite` | Directory for SQLite database backups (Linux). |
| `sqlite.backup.path` | `%APPDATA%/java-scaffold/sqlite` | Directory for SQLite database backups (Windows). |
| `sqlite.backup.path` | `$HOME/Library/Application Support/java-scaffold/sqlite` | Directory for SQLite database backups (MacOS). |

## Configuration overrides

Environment variables and JVM system properties used to override application configuration.

| Property | Environment Variable | System Property |
|----------|----------------------|-----------------|
| `logs.console.level` | `JAVA_SCAFFOLD_LOGS_CONSOLE_LEVEL` | `logs.console.level` |
| `logs.file.level` | `JAVA_SCAFFOLD_LOGS_FILE_LEVEL` | `logs.file.level` |
| `logs.file.size` | `JAVA_SCAFFOLD_LOGS_FILE_SIZE` | `logs.file.size` |
| `logs.file.rotate` | `JAVA_SCAFFOLD_LOGS_FILE_ROTATE` | `logs.file.rotate` |
| `logs.file.path` | `JAVA_SCAFFOLD_LOGS_FILE_PATH` | `logs.file.path` |
| `server.host` | `JAVA_SCAFFOLD_SERVER_HOST` | `server.host` |
| `server.port` | `JAVA_SCAFFOLD_SERVER_PORT` | `server.port` |
| `server.timeout` | `JAVA_SCAFFOLD_SERVER_TIMEOUT` | `server.timeout` |
| `server.origins.allowed` | `JAVA_SCAFFOLD_SERVER_ORIGINS_ALLOWED` | `server.origins.allowed` |
| `duckdb.database.path` | `JAVA_SCAFFOLD_DUCKDB_DATABASE_PATH` | `duckdb.database.path` |
| `duckdb.library.path` | `JAVA_SCAFFOLD_DUCKDB_LIBRARY_PATH` | `duckdb.library.path` |
| `duckdb.temp.path` | `JAVA_SCAFFOLD_DUCKDB_TEMP_PATH` | `duckdb.temp.path` |
| `duckdb.temp.size` | `JAVA_SCAFFOLD_DUCKDB_TEMP_SIZE` | `duckdb.temp.size` |
| `sqlite.database.path` | `JAVA_SCAFFOLD_SQLITE_DATABASE_PATH` | `sqlite.database.path` |
| `sqlite.backup.path` | `JAVA_SCAFFOLD_SQLITE_BACKUP_PATH` | `sqlite.backup.path` |

## Preferences system properties

| Environment Variable | System Property | Default | Description |
|----------------------|-----------------|---------|-------------|
| `JAVA_SCAFFOLD_PREFS_PATH` | `prefs.path` | `$XDG_CONFIG_HOME"/java-scaffold` | Directory for the preferences (Linux). |
| `JAVA_SCAFFOLD_PREFS_PATH` | `prefs.path` | `%APPDATA%/java-scaffold` | Directory for the preferences (Windows). |
| `JAVA_SCAFFOLD_PREFS_PATH` | `prefs.path` | `$HOME/Library/Application Support/java-scaffold` | Directory for the preferences (MacOS). |

## Path resolution

Paths are resolved in the following order:

1. Explicit system property.
2. Environment variable.
3. Application default.
