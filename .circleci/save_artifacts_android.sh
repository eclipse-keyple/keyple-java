#!/bin/bash

. .circleci/functions.sh

#save artifacts of keyple core projects (java)
/bin/bash .circleci/save_artifacts.sh

mkdir -p ~/artifacts/jar ~/pages/jar
find . -name "*.aar"  \
  -exec cp {} ~/artifacts/jar \; \
  -exec cp {} ~/pages/jar \;

save_directory keyple-example/android/nfc
save_directory keyple-example/android/omapi
save_directory keyple-plugin/android-nfc
save_directory keyple-plugin/android-omapi

echo "End of save artifacts android"

