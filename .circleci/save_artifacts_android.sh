#!/bin/sh

. .circleci/functions.sh

mkdir -p ~/artifacts/aar ~/pages/aar
find . -name "keyple-*.aar"  \
  -exec cp {} ~/artifacts/aar \; \
  -exec cp {} ~/pages/aar \;


save_directory keyple-example/android/nfc
save_directory keyple-example/android/omapi
save_directory keyple-plugin/android-nfc
save_directory keyple-plugin/android-omapi

echo "End of save artifacts android"
find . -name "*.html"

