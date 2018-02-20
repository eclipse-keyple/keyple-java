#!/bin/sh
mkdir -p pages/$TRAVIS_BRANCH/javadoc
mv keyple-core/build/docs/javadoc pages/$TRAVIS_BRANCH/javadoc/core
echo "<html><body>K E Y P L E</body></html>" >pages/index.html
