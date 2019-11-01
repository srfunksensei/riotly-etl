# Riotly ETL

An ETL transformer which handles social media data and outputs the results in a `csv` file.

## Prerequisites

1. Java 8
2. Maven 3.3 (or higher)

## How does the process looks like

The complete process is defined as following:
1. authenticate to google cloud with `credentials.json`
2. download all `.json` files in `data/` (if any) to local temp directory
3. extract and transform information 
4. generate file for each user and merge them all in one
5. zip all csv files in one archive
6. upload the archive to google cloud `output/` (if exists)
7. delete temp directory

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 