# IpAddressesProcessor

## Introduction

This application was implemented for two goals:
1. Generating text files with random IP's.
2. Parsing text files with random IP's to count the uniques IP's.

## Using

Run the application using jar file or from IDE using command "__mvn spring-boot:run__".
For generating file, type the next command: 

__generate__

You can customize _generate_ command with following attributes:

_generate -d path_to_file  -c lines_count_

Please, use double backslash in the path_to_file, for example:

_generate -d C:\\\\temp\\\\file.txt -c 1000000_
