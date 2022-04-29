#!/bin/sh

# get path to script
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

rootDir="$SCRIPTPATH/.."
binDir="$rootDir/app/build/libs"
pluginsDir="$rootDir/build/plugins"

java  -Dpf4j.pluginsDir="$pluginsDir" -jar "$binDir/corda-cli.jar" "$@"
