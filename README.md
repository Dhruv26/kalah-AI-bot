# COMP34120-sem1-project

# put all files directory in source.txt
## WINDOWS
dir /s /B *.java > source.txt

## LINUX
find $(pwd) -name *.java > source.txt


# compile
javac @source.txt


# run
java -classpath MKAgent Main







# logging
## note: 
## - have to setlevel
## - setlevel(Level.INFO) logs info, warning, and sever msg; 
## - setlevel(Level.WARNING) logs warning, and sever msg and so on

Log log = new Log("log");
log.logger.setLevel(Level.SEVERE);
log.logger.info("Info msg");
log.logger.warning("Warning msg");
log.logger.severe("Severe msg");