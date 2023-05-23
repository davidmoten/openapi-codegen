#!/bin/bash
set -e
set -x
mvn clean install -pl *test -P extras
mvn clean install -pl *test -P github
mvn clean install -pl *test -P bitbucket
mvn clean install -pl *test -P docusign
mvn clean install -pl *test -P mailchimp
