#!/bin/sh
for i in `find app -name '*.png'`; do identify $i; done  | perl -ne'print "convert icon-master.png -geometry $2 $1\n" if /(\S+) PNG (\S+)/'
