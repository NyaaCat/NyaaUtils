#!/bin/bash
gradle publish
cd ..
git clone -b maven-repo 'git@github.com:NyaaCat/nyaautils.git' nyaautils-mvn
rm -rf nyaautils-mvn/cat
cp -r nyaautils/build/repo/cat nyaautils-mvn/
cd nyaautils-mvn
git add .
git commit -m "manually generated maven repo"
git push
cd ..
rm nyaautils-mvn
cd nyaautils
