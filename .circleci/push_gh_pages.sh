#!/bin/bash
# ideas used from https://gist.github.com/motemen/8595451

# Based on https://github.com/eldarlabs/ghpages-deploy-script/blob/master/scripts/deploy-ghpages.sh
# Used with their MIT license https://github.com/eldarlabs/ghpages-deploy-script/blob/master/LICENSE

# abort the script if there is a non-zero error
set -e

# show where we are on the machine
pwd
remote=$(git config remote.origin.url)

echo "*** make a directory to put the gp-pages branch"
mkdir gh-pages-branch
cd gh-pages-branch

if [ "$GH_EMAIL" = "" ]; then
    GH_EMAIL=pierre.terree@calypsonet.org
fi

if [ "$GH_NAME" = "" ]; then
    GH_NAME="CircleCI Pierre Terree"
fi

#now lets setup a new repo so we can update the gh-pages branch
echo "*** now lets setup a new repo so we can update the gh-pages branch"
git config --global user.email "$GH_EMAIL" > /dev/null 2>&1
git config --global user.name "$GH_NAME" > /dev/null 2>&1
git init
git remote add --fetch origin "$remote"

# Avoiding really dirty URLs
CIRCLE_BRANCH=$(echo $CIRCLE_BRANCH|sed 's/[^a-zA-Z0-9-]//g')

# switch into the the gh-pages branch
echo "*** switch into the the gh-pages branch"
if git rev-parse --verify origin/gh-pages > /dev/null 2>&1
then
    git checkout gh-pages
    # delete any old site as we are going to replace it
    # Note: this explodes if there aren't any, so moving it here for now
    #git rm -rf .
else
    git checkout --orphan gh-pages
fi

# copy over or recompile the new site
echo "*** copy over or recompile the new site"
git rm -rf $CIRCLE_BRANCH/* ||:
mkdir -p $CIRCLE_BRANCH #create if not exists
cp -a ~/pages/* $CIRCLE_BRANCH/


echo "*** stage any changes and new files"
# stage any changes and new files
git add -A
# now commit, ignoring branch gh-pages doesn't seem to work, so trying skip
message=${1:-"default message"}

git commit --allow-empty -m "$message"

sed -i -e "s/git@github.com:calypsonet/https:\/\/${GITHUB_TOKEN}:x-oauth-basic@github.com\/calypsonet/" .git/config

echo "*** push"
# and push, but send any output to /dev/null to hide anything sensitive
git push --force --quiet origin gh-pages

# go back to where we started and remove the gh-pages git repo we made and used
# for deployment
echo "*** remove the gh-pages git repo"
cd ..
rm -rf gh-pages-branch

echo "Finished Deployment!"
