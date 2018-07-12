#!/bin/bash


save_directory () {
    for dir in $1
    do
        echo "* Dir $dir:"

        # Junit tests --> ~/tests
        if [ -d $dir/build/test-results/test ]; then
            echo "   * Found junit tests"
            mkdir -p ~/tests/$dir
            cp -a $dir/build/test-results/test/*.xml ~/tests/$dir
        fi

        # Junit HTML view --> ~artifacts/$project/tests
        if [ -d $dir/build/reports/tests/test ]; then
            echo "   * Found html view of tests"
            mkdir -p ~/artifacts/$dir
            cp -a $dir/build/reports/tests/test ~/artifacts/$dir/tests

            mkdir -p ~/pages/$dir
            cp -a $dir/build/reports/tests/test ~/pages/$dir/tests
        fi

        # Javadoc --> ~/artifacts/$project/javadoc
        if [ -d $dir/build/docs/javadoc ]; then
            echo "   * Found javadoc"
            mkdir -p ~/artifacts/$dir
            cp -a $dir/build/docs/javadoc ~/artifacts/$dir/javadoc

            mkdir -p ~/pages/$dir
            cp -a $dir/build/docs/javadoc ~/pages/$dir/javadoc
        fi

        # JMH --> ~/artifacts/$project/jmh
        if [ -f $dir/build/reports/jmh/results.txt ]; then
            echo "   * Found JMH"
            mv $dir/build/reports/jmh/results.txt ~/artifacts/$dir/jmh.txt
        fi
    done
}