ps -ef | grep rmiregistry | grep -v grep | awk '{print $2}' | xargs kill
javac *.java
rmiregistry &
java Mapper &
java Reducer &

