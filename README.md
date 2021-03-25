# Riotly ETL

An ETL transformer which handles social media data and outputs the results in a `csv` file.

## Prerequisites

1. Java 8
2. Maven 3.3 (or higher)

## How does the application work

The complete process is defined as following:
1. authenticate to google cloud with [credentials.json](src/main/resources/credentials.json)
2. download all `.json` files in `data` directory (if any) to local temp directory
3. extract and transform information 
4. generate a file for each user and create one file with merged information from all generated files
5. zip all csv files in one archive
6. upload the archive to google cloud `output` directory (if exists)
7. delete temp directory

## How to run application

To be able to see the application in action you must follow these steps:

1. run `mvn install`
2. run `java -jar riotly-0.0.1-SNAPSHOT-jar-with-dependencies.jar `

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 