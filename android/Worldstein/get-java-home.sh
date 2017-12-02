#!/usr/bin/sh
perl -ne'print "$1/jre\n" if /^sdk.dir=(\S+)/' < local.properties
