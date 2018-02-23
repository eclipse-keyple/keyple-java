#!/bin/sh -x
mkdir -p pages/javadoc
mv keyple-core/build/docs/javadoc pages/javadoc/core
echo "<html><body>K E Y P L E - ${TRAVIS_BRANCH}</body></html>" >pages/index.html

env
