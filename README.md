# mashup-privacy-checker

This program is a small utility to check privacy compliance using SPECIAL vocabulary in a linked data mashup settings. 

## Building

this project is written in kotlin and uses gradle. please change accordingly if you prefer to use maven. 

###Building a zipped java application (default) 

`gradle build`

###Building fat-jar

open `build.gradle` file, comment line `apply plugin: 'application'` and uncomment last line in the `jar` block containing : `from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }`



## Running

### From tar/zip archive

extract archive and run `bin\privacycheck -check <config.ttl>`

### From combined JAR

run `java -jar privacycheck.jar -check <config.ttl>`



