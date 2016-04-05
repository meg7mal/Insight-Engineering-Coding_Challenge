#!/usr/bin/env bash
javac -cp "lib/guava-19.0.jar;lib/json-simple-1.1.1.jar" src/*.java
java -cp "lib/guava-19.0.jar;lib/json-simple-1.1.1.jar;src" MainClass "./tweet_input/tweets.txt" "./tweet_output/output.txt"
