#!/bin/sh
dir=$(dirname "$0")
java -cp "$dir/h2-1.3.167.jar:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console "$@"
