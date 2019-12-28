## Run this to install the pre-installed opencv-jar

`
mvn install:install-file -Dfile=/usr/share/java/opencv4/opencv-420.jar -DgroupId=org -DartifactId=opencv -Dversion=4.2.0 -Dpackaging=jar
`

## OpenCV Build For Java

Follow these instructions: 

1. https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html

- Set build prefix to /usr/java/packages

2. Copy /usr/java/packages/share/OpenCV-3.4.2/java/libopencv_java342.so /usr/java/packages/lib

## Run Time Server

1. mvn package && mvn exec:java -PServer
2. mvn package && mvn exec:java -PClient
