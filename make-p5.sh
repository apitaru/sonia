#!/bin/sh

set -e

rm -f Sonia.zip plib/sonia/library/*

cd bin
jar cf ../plib/sonia/library/sonia.jar *
cd -

jar tf plib/sonia/library/sonia.jar

cp lib/jsyn.jar plib/sonia/library/
cp library.properties plib/sonia/library/

cd plib
jar cf ../Sonia.zip *
cd ..

echo
jar tf Sonia.zip
