#!/bin/bash

. .circleci/functions.sh

mkdir -p ~/artifacts/jars ~/pages/jars
find . -name "keyple-*.jar" -not -name "*-jmh.jar" \
  -exec cp {} ~/artifacts/jars \; \
  -exec cp {} ~/pages/jars \;

save_directory keyple-core
save_directory keyple-calypso
save_directory keyple-example/common
save_directory keyple-example/pc
save_directory keyple-plugins/pcsc
save_directory keyple-plugins/stub

cp .build/web/* ~/pages/

echo "End of save artifacts java"



