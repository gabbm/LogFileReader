Spring Boot command line application that reads log registries in JSON format from an external file.

## Requirements
 - JDK 8
 - HSQLDB server instance running (provided library and instructions)

## Running the tests (not necessary to have the HSQLDB server instance running)
 - Download/Clone the folder log-reader
 - Navigate to its root:
```
cd C:\log-reader
```
 - Enter the following command to run the tests:
```
mvn test
```

## Preparation
 - Create a new folder where the database files are going to be stored
 - Copy the jar file in the lib folder (hsqldb.jar) to the new folder
 - Execute the following command to start the HSQLDB server with the default configuration:
```
java -cp .\hsqldb.jar org.hsqldb.Server -database.0 file:testdb -dbname.0 testdb
```

## Runing the application
 - Download/Clone the folder log-reader
 - Navigate to its root:
```
cd C:\log-reader
```
 - Enter the following command to run the application (Example with the provided file, but the path can be replaced for a custom file):
 ```
mvn spring-boot:run "-Dspring-boot.run.arguments=src\main\resources\test\logfile.txt"
```
 - Once we have executed the application the valid data will have been added to the database, to check it we can connect to the database using any database client like DBeaver using the following credentials:
 ```
JDBC URL: jdbc:hsqldb:hsql://localhost/testdb
USERNAME: sa
```
