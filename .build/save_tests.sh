#!/bin/sh
for dir in keyple-*
do
    if [ -d $dir/build/test-results/test ]; then
       mkdir -p ~/tests/$dir
       mv $dir/build/test-results/test/*.xml ~/tests/$dir
    fi
    if [ -d $dir/build/reports/tests/test ]; then
       mkdir -p ~/artifacts/$dir
       mv $dir/build/reports/tests/test ~/artifacts/$dir/tests
    fi
    if [ -f $dir/build/reports/jmh/results.txt ]; then
       mkdir -p ~/artifacts
       mv $dir/build/reports/jmh/results.txt ~/artifacts/$dir/jmh.txt
    fi
done

