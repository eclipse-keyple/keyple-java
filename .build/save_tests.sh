#!/bin/sh
mkdir -p test-results
find . -path "*/build/test-results/test/*.xml" -exec cp {} test-results
