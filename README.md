## Ip Addresses Processor

### Introduction

This application has been implemented for two goals:
1. Create text files with random IP's list.
2. Parsing text files with random IP's to count unique IP's.


### Using

Run the application using jar file or from the IDE using command "__mvn spring-boot:run__".
To create a file, enter the next command: 

__generate__

You can customize _generate_ command with the following attributes:

_generate -d path_to_file  -c lines_count_

Please use double backslash in _path_to_file_, for example:

_generate -d C:\\\\temp\\\\file.txt -c 1000000_

Parsing:

For file parsing use the command:

__parse__

Customize _parse_ with options:

_parse -s path_to_file_

Also use double backslash in the path.

### Contacts

Mike Scherbakov

email: scherbakov@gmail.com

tel: +79165303619