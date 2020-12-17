# Contributing to 'Eclipse Keyple' Java implementation

Thanks for your interest in this project.

## Project description

'Eclipse Keyple' Java implementation is a project containing all the content of the Java implementation of the [Eclipse Keyple](https://keyple.org/) API.

* https://github.com/eclipse/keyple-java

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team contributors must
electronically sign the Eclipse Contributor Agreement (ECA).

* http://www.eclipse.org/legal/ECA.php

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team contributors must
electronically sign the Eclipse Contributor Agreement (ECA).

* http://www.eclipse.org/legal/ECA.php

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

## Contribute via Fork
You need a [GitHub](https://github.com/join) and an [Eclipse](https://accounts.eclipse.org/user/register) account for which you signed the [Eclipse Contributor Agreement](https://accounts.eclipse.org/user/login?destination=user/eca).

 1. Fork the repository on GitHub 
 1. Check if there is a [Jira issue](https://keyple.atlassian.net/projects/KEYP) for what you want to work on or create one.
 1. Announce in the comments section that you want to work on the issue. Also describe the solution you want to implement. To improve the chances for your contribution to be accepted, you'll want to wait for the feedback of the committers. 
 1. Create a new branch from *develop* for your changes. Name it after the Jira number, e.g. *KEYP-XXX_[descriptionofchanges]*.
 1. Implement your changes. 
 1. Rebase on *develop*.
 1. Run **./gradlew spotlessApply && android/gradlew spotlessApply** to format the code and add licence headers to the files.
 1. Run **./gradlew check && android/gradlew check** (to check code formatting and run tests)
 1. Commit using [Sign off](https://git-scm.com/docs/git-commit#git-commit--s) with the same email address you are using for your Eclipse account. Use descriptive and meaningful commit messages. In particular, start the first line of the commit message with the number of the issue that the commit addresses, e.g. *KEYP-XXX [descriptionofchanges]*.
 1. Push your changes to your forked repository.
 1. Create a [pull request (PR)](https://help.github.com/articles/using-pull-requests/) to *develop*.
 1. After submitting, do not use your branch for any other development, otherwise further changes that you make will be visible in the PR. 

## Contributing for Committers
You're a committer if you have write-access to the Keyple git-repositories.

 1. Make sure there is a [Jira issue](https://keyple.atlassian.net/projects/KEYP) for what you want to work on or create one.
 1. Assign the issue to yourself.
 1. Create a local git branch from *develop*. Name it after the Jira number, e.g. *KEYP-XXX_[descriptionofchanges]*.
 1. Implement your changes. 
 1. Rebase on *develop*.
 1. Run **./gradlew spotlessApply && android/gradlew spotlessApply** to format the code and add licence headers to the files.
 1. Run **./gradlew check && android/gradlew check** (to check code formatting and run tests)
 1. Commit using [Sign off](https://git-scm.com/docs/git-commit#git-commit--s) with the same email address you are using for your Eclipse account. Use descriptive and meaningful commit messages. In particular, start the first line of the commit message with the number of the issue that the commit addresses, e.g. *KEYP-XXX Update Keyple Core Unit tests*.
 1. Push the branch into the repository.
 1. Create a pull request and ask somebody who is familiar with the code you modified to review it.
 1. If the reviewer approves and all checks are OK, merge using squash commit.
