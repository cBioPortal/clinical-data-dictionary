#!/bin/bash

JENKINS_USER_HOME_DIRECTORY=/var/lib/jenkins
JENKINS_PROPERTIES_DIRECTORY=$JENKINS_USER_HOME_DIRECTORY/pipelines-configuration/properties
JENKINS_CDD_PROPERTIES_DIRECTORY=$JENKINS_PROPERTIES_DIRECTORY/clinical-data-dictionary

# CDD_DIRECTORY is starting directory - location on jenkins machine where github repo is cloned
# Need to copy over jenkins specific application.properties because default properties
# do not include several properties that are provided at runtime (k8s deployment)
CDD_DIRECTORY="$(pwd)"
JENKINS_TEST_APPLICATION_PROPERTIES=jenkins.test.application.properties
APPLICATION_PROPERTIES=application.properties
TEST_APPLICATION_PROPERTIES=test.application.properties

rsync $JENKINS_CDD_PROPERTIES_DIRECTORY/$JENKINS_TEST_APPLICATION_PROPERTIES $CDD_DIRECTORY/src/main/resources/$APPLICATION_PROPERTIES
rsync $JENKINS_CDD_PROPERTIES_DIRECTORY/$TEST_APPLICATION_PROPERTIES $CDD_DIRECTORY/src/test/resources
