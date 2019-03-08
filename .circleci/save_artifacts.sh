#!/bin/bash

. .circleci/functions.sh

mkdir -p ~/artifacts/jars ~/pages/jars
find . -name "keyple-*.jar" \
  -exec cp  -rv {} ~/artifacts/jars \; \
  -exec cp  -rv {} ~/pages/jars \;

save_directory java/component/keyple-core
save_directory java/component/keyple-calypso
save_directory java/component/keyple-plugin/pcsc
save_directory java/component/keyple-plugin/stub
save_directory java/component/keyple-plugin/remotese

cp .build/web/* ~/pages/

echo "End of save artifacts java"



