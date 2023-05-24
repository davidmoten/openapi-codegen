#!/bin/bash
set -e
set -x
MAVEN_OPTS="-Xmx3g"
mvn clean install -pl *test -P extras
mvn clean install -pl *test -P bitbucket
mvn clean install -pl *test -P mailchimp
mvn clean install -pl *test -P github
mvn clean install -pl *test -P docusign
