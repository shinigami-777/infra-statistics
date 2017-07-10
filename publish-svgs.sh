#!/bin/sh -xe

./generate-index.sh ./target/stats/

# Delete our local branch if it exists
git branch -D gh-pages || true
# Check out the most recent version of the gh-pages branch
git fetch origin
git checkout -b gh-pages --track origin/gh-pages

git config user.name `hostname`
git config user.email "no-reply@jenkins.io"

cp -R target/svg/* jenkins-stats/svg/
cp -R target/stats/* plugin-installation-trend/
cp -R target/pluginversions/* pluginversions/

git add jenkins-stats/svg
git add plugin-installation-trend
git add pluginversions

git commit -am "Generating stats" || true
git push git@github.com:jenkins-infra/infra-statistics.git gh-pages
