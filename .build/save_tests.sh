#!/bin/sh
mkdir -p ~/junit
find . -path "*/build/test-results/test/*.xml" -exec cp {} ~/junit \;
#find . -path "*/build/reports/tests/test"
