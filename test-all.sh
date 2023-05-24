#!/bin/bash
set -e
set -x
MAVEN_OPTS="-Xmx3g"
mvn clean install -B -P extras
rm -rf /tmp/extras && cp -pr *test/target /tmp/extras
mvn clean install -B -pl *test -P bitbucket
rm -rf /tmp/bb && cp -pr *test/target /tmp/bb
mvn clean install -B -pl *test -P mailchimp
rm -rf /tmp/mc && cp -pr *test/target /tmp/mc
mvn clean install -B -pl *test -P github
rm -rf /tmp/gh && cp -pr *test/target /tmp/gh
mvn clean install -B -pl *test -P docusign
rm -rf /tmp/ds && cp -pr *test/target /tmp/ds
