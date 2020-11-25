#!/bin/bash

clear
if cd; then
  echo "Navigating..."
else exit
fi
if cd jCleanup/jCleanup/src/com/morris/jCleanup; then
  echo "Starting jCleanup"
else exit
fi
java Main.java
