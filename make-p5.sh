#!/bin/sh

set -e

rm -rf Sonia.zip plib
mkdir -p plib/sonia/library


cd bin
jar cf ../plib/sonia/library/sonia.jar *
cd -

echo
jar tf plib/sonia/library/sonia.jar

cp lib/jsyn.jar plib/sonia/library/
cp library.properties plib/sonia/library/

cd plib
jar cf ../Sonia.zip *
cd ..

rm -rf plib

echo
jar tf Sonia.zip
