#!/bin/sh

mkdir -p ~/artifacts/jars
find . -name "keyple-*.jar" -not -name "*-jmh.jar" -exec cp ~/artifacts/jars

for dir in keyple-*
do
    # Junit tests --> ~/tests
    if [ -d $dir/build/test-results/test ]; then
       mkdir -p ~/tests/$dir
       mv $dir/build/test-results/test/*.xml ~/tests/$dir
    fi

    # Junit HTML view --> ~artifacts/$project/tests
    if [ -d $dir/build/reports/tests/test ]; then
       mkdir -p ~/artifacts/$dir
       mv $dir/build/reports/tests/test ~/artifacts/$dir/tests
    fi

    # Javadoc --> ~/artifacts/$project/javadoc
    if [ -d $dir/build/docs/javadoc ]; then
        mv $dir/build/docs/javadoc
    fi

    # JMH --> ~/artifacts/$project/jmh
    if [ -f $dir/build/reports/jmh/results.txt ]; then
       mkdir -p ~/artifacts
       mv $dir/build/reports/jmh/results.txt ~/artifacts/$dir/jmh.txt
    fi
done

