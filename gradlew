#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
APP_HOME=$(cd "$(dirname "$0")" && pwd)

# Add default JVM options here
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use JAVA_HOME if set
if [ -n "$JAVA_HOME" ] ; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi

exec "$JAVA" $DEFAULT_JVM_OPTS -cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
