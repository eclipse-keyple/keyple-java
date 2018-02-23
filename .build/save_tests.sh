#!/bin/sh
mkdir -p ~/tests ~/artifacts
# find . -path "*/" -exec cp {} ~/junit \;
for dir in keyple-*
do
    if [ -d $dir/build/test-results/test ]; then
       mkdir -p ~/tests/$dir
       mv $dir/build/test-results/test/*.xml ~/tests/$dir
    fi
    if [ -d $dir/build/reports/tests/test ]; then
       mv $dir/build/reports/tests/test ~/artifacts/tests/$dir
    fi
done

