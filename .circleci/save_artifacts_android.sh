#!/bin/sh

. .circleci/save_artifacts.sh

mkdir -p ~/artifacts/jars ~/pages/jars
find . -name "keyple-*.jar" -not -name "*-jmh.jar" \
  -exec cp {} ~/artifacts/jars \; \
  -exec cp {} ~/pages/jars \;


save_directory keyple-example/android/nfc
save_directory keyple-example/android/omapi
save_directory keyple-plugin/android-nfc
save_directory keyple-plugin/android-omapi

cp .build/web/* ~/pages/




