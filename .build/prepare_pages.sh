#!/bin/sh
mkdir -p pages/javadoc pages/tests
mv keyple-core/build/docs/javadoc pages/javadoc/core
echo "<html><body>K E Y P L E - $TRAVIS_BRANCH</body></html>" >pages/index.html
