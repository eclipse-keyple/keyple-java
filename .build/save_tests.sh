#!/bin/sh
mkdir -p ~/tests ~/artifacts
# find . -path "*/" -exec cp {} ~/junit \;
for dir in keyple-*
do
    if [ ! -d $dir/build/test-results/test ]; then
       continue
    fi
    mv $dir/build/test-results/test/*.xml ~/tests
    mkdir -p ~/artifacts/tests/$dir
    mv $dir/build/reports/tests/test ~/artifacts/tests/$dir
done

