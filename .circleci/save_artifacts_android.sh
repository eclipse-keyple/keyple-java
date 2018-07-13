#!/bin/bash

. .circleci/functions.sh

mkdir -p ~/artifacts/aar ~/pages/aar
find . -name "keyple-*.aar"  \
  -exec cp {} ~/artifacts/jar \; \
  -exec cp {} ~/pages/jar \;


save_directory keyple-example/android/nfc
save_directory keyple-example/android/omapi
save_directory keyple-plugin/android-nfc
save_directory keyple-plugin/android-omapi

echo "End of save artifacts android"

