#!/bin/bash
set -e
set -x
MAVEN_OPTS="-Xmx3g -Dmaven.javadoc.skip=true"
mvnd clean install -P extras
rm -rf /tmp/extras && cp -pr *test/target /tmp/extras
mvnd clean install -pl *test -P bitbucket
rm -rf /tmp/bb && cp -pr *test/target /tmp/bb
mvnd clean install -pl *test -P mailchimp
rm -rf /tmp/mc && cp -pr *test/target /tmp/mc
mvnd clean install -pl *test -P github
rm -rf /tmp/gh && cp -pr *test/target /tmp/gh
mvnd clean install -pl *test -P zuora 
rm -rf /tmp/zuora && cp -pr *test/target /tmp/zuora
mvnd clean install -pl *test -P docusign
rm -rf /tmp/ds && cp -pr *test/target /tmp/ds
