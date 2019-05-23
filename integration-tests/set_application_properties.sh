#!/bin/bash

JENKINS_USER_HOME_DIRECTORY=/var/lib/jenkins
JENKINS_PROPERTIES_DIRECTORY=$JENKINS_USER_HOME_DIRECTORY/pipelines-configuration/properties
JENKINS_CDD_PROPERTIES_DIRECTORY=$JENKINS_PROPERTIES_DIRECTORY/clinical-data-dictionary

# CDD_DIRECTORY is starting directory - location on jenkins machine where github repo is cloned
CDD_DIRECTORY="$(pwd)"
APPLICATION_PROPERTIES=application.properties
TEST_APPLICATION_PROPERTIES=test.application.properties

rsync $JENKINS_CDD_PROPERTIES_DIRECTORY/$APPLICATION_PROPERTIES $CDD_DIRECTORY/src/main/resources
rsync $JENKINS_CDD_PROPERTIES_DIRECTORY/$TEST_APPLICATION_PROPERTIES $CDD_DIRECTORY/src/test/resources
