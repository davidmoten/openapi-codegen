#!/bin/bash
set -e
set -x
OPTS="-Dspotbugs.skip=true -Dpmd.skip=true -Dcpd.skip=true -Dmaven.javadoc.skip=true"
MAVEN_OPTS="-Xmx3g"
mvnd clean install -P extras $OPTS 
rm -rf /tmp/extras && cp -pr *test/target /tmp/extras
mvnd clean install -pl *test -P bitbucket $OPTS
rm -rf /tmp/bb && cp -pr *test/target /tmp/bb
mvnd clean install -pl *test -P mailchimp $OPTS
rm -rf /tmp/mc && cp -pr *test/target /tmp/mc
mvnd clean install -pl *test -P github $OPTS
rm -rf /tmp/gh && cp -pr *test/target /tmp/gh
mvnd clean install -pl *test -P zuora $OPTS 
rm -rf /tmp/zuora && cp -pr *test/target /tmp/zuora
## docusign skipped because has > 256 fields
# mvnd clean install -pl *test -P docusign $OPTS
rm -rf /tmp/ds && cp -pr *test/target /tmp/ds
