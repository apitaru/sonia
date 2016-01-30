#!/bin/sh

set -e

# recreate the javadocs
./make-docs.sh

# make zip folders
rm -rf Sonia.zip plib
mkdir -p plib/sonia/library

# create our jar file
cd bin
jar cf ../plib/sonia/library/sonia.jar data pitaru
cd -

# print out the jar contents
echo
jar tf plib/sonia/library/sonia.jar

# add jsyn.jar to our zip dir
cp lib/jsyn.jar plib/sonia/library/

# add props file to our zip dir
cp library.properties plib/sonia/library/

# add example sketches to our zip dir
cp -r examples plib/sonia

# add docs to our zip dir
cp -r doc plib/sonia/reference

# zip it all up
cd plib
jar cf ../Sonia.zip *
cd ..

# clean up our mess
rm -rf plib

# print out the zip contents
echo
jar tf Sonia.zip
