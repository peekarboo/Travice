#!/bin/sh

if which java &>/dev/null
then
  if java -jar test.jar
  then
    mkdir -p /opt/travice
    cp -r * /opt/travice
    rm -r ../out
    rm /opt/travice/setup.sh
    chmod -R go+rX /opt/travice
    cd /opt/travice/bin/
    /opt/travice/bin/installDaemon.sh
  else
    echo 'Java 7 or higher is required'
  fi
else
  echo 'Java runtime is required'
fi
