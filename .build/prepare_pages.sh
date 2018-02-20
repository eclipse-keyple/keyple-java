#!/bin/sh
mkdir -p pages/javadoc pages/tests
mv keyple-core/build/docs/javadoc pages/javadoc/core
echo "<html><body>K E Y P L E</body></html>" >pages/index.html

if [ $TRAVIS_BRANCH = "master" ] || [ $TRAVIS_BRANCH = "feature-publish-javadoc" ]; then
    export DEPLOY=true
fi
