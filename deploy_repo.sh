#!/bin/bash
gradle publish
cd ..
git clone -b maven-repo 'https://github.com/NyaaCat/nyaautils.git' nyaautils-mvn
rm -rf nyaautils-mvn/cat
cp -r nyaautils/build/repo/cat nyaautils-mvn/
cd nyaautils-mvn
git add .
git commit -m "Travis CI generated maven repo for v$main_version.$TRAVIS_BUILD_NUMBER"
git push -q https://$GITHUB_KEY@github.com/NyaaCat/nyaautils
cd ../nyaautils
