#!/bin/bash
javac -cp "src;src/guava-19.0.jar;src/json-simple-1.1.1.jar" -d "src" "src/*.java"
java -cp "src/guava-19.0.jar;src/json-simple-1.1.1.jar;src" MainClass "./tweet_input/tweets.txt" "./tweet_output/output.txt"