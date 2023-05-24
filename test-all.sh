#!/bin/bash
set -e
set -x
MAVEN_OPTS="-Xmx3g"
mvn clean install -B -P extras
mvn clean install -B -pl *test -P bitbucket
mvn clean install -B -pl *test -P mailchimp
mvn clean install -B -pl *test -P github
mvn clean install -B -pl *test -P docusign
