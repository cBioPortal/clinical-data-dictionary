#!/bin/bash

TESTING_DIRECTORY=/var/lib/jenkins/tempdir
if [ ! -d $TESTING_DIRECTORY ] ; then
    mkdir -p $TESTING_DIRECTORY
fi
TESTING_DIRECTORY_TEMP=$(mktemp -d $TESTING_DIRECTORY/pr-integration.XXXXXX)
ROOT_WORKSPACE=`pwd`
CMO_PIPELINES_DIRECTORY=$ROOT_WORKSPACE/cmo-pipelines
CDD_DIRECTORY=$ROOT_WORKSPACE/cdd

CDD_JAR=$CDD_DIRECTORY/target/cdd.jar
CCD_SCRIPTS_DIRECTORY=$CDD_DIRECTORY/scripts
CCD_DOCS_DIRECTORY=$CDD_DIRECTORY/docs
REDCAP_JAR=$CMO_PIPELINES_DIRECTORY/redcap/redcap_pipeline/target/redcap_pipeline.jar
IMPORT_SCRIPTS_DIRECTORY=$CMO_PIPELINES_DIRECTORY/import-scripts
EXPECTED_METADATA_HEADERS=$CDD_DIRECTORY/integration-tests/expected_metadata_headers.txt

JENKINS_USER_HOME_DIRECTORY=/var/lib/jenkins
JENKINS_PROPERTIES_DIRECTORY=$JENKINS_USER_HOME_DIRECTORY/pipelines-configuration/properties
APPLICATION_PROPERTIES=application.properties
TEST_APPLICATION_PROPERTIES=test.application.properties

REDCAP_EXPORT_TEST_SUCCESS=0
ADD_TEST_CDD_HEADERS_SUCCESS=0
EQUAL_METADATA_HEADERS_TEST_SUCCESS=0
FAKE_STUDY_ID_TEST_SUCCESS=0
CDD_TOPBRAID_URI_VALIDATION_SUCCESS=0

function find_and_kill_cdd_process {
    CDD_PORT_NUMBER=$1
    if [ ! -z $CDD_PORT_NUMBER ] ; then
        CDD_PROCESS_NUMBER=`netstat -tanp | grep LISTEN | sed 's/\s\s\s*/\t/g' | grep -P ":$CDD_PORT_NUMBER\t" | cut -f6 | sed 's/\/.*//'`
    fi
    if [ ! -z $CDD_PROCESS_NUMBER ] ; then
        kill -9 $CDD_PROCESS_NUMBER
        if [ $? -gt 0 ] ; then
            echo "failed to kill process $CDD_PROCESS_NUMBER, please check for running cdd process"
        fi
    else
        echo "no cdd process found"
    fi
}
trap 'find_and_kill_cdd_process $CDD_PORT' EXIT

function find_free_port {
    CHECKED_PORT=10000
    MAX_PORT=65535
    EXISTING_PORT=PORTNUMBER
    # each loop sets EXISTING_PORT to line from netstat (corresponding to CHECKED_PORT)
    # if grep CHECKED_PORT returns nothing, EXISTING_PORT is unset
    # CHECKED_PORT is a free port since not found in netstat output
    while [[ ! -z $EXISTING_PORT || $CHECKED_PORT -gt $MAX_PORT ]] ; do
        CHECKED_PORT=$(($CHECKED_PORT + 1))
        EXISTING_PORT=`netstat -tanp | sed 's/\s\s\s*/\t/g' | grep -P ":$CHECKED_PORT\t"`
    done
    if [ $CHECKED_PORT -gt $MAX_PORT ] ; then
        echo -1
    else
        echo $CHECKED_PORT
    fi
}

# Copy in CDD properties and build jar
rsync $JENKINS_PROPERTIES_DIRECTORY/clinical-data-dictionary/$APPLICATION_PROPERTIES $CDD_DIRECTORY/src/main/resources
rsync $JENKINS_PROPERTIES_DIRECTORY/clinical-data-dictionary/$TEST_APPLICATION_PROPERTIES $CDD_DIRECTORY/src/test/resources
cd $CDD_DIRECTORY ; mvn package -DskipTests=true -Dpackaging.type=jar

#start up CDD on some port on dashi-dev
CDD_PORT=`find_free_port`

TIME_BETWEEN_CDD_AVAILIBILITY_TESTS=3
CDD_DEPLOYMENT_SUCCESS=0
CURRENT_WAIT_TIME=0
MAXIMUM_WAIT_TIME=120

if [ $CDD_PORT -gt 0 ] ; then
    java -jar $CDD_JAR --server.port=$CDD_PORT >> /dev/null 2>&1 &

    # maximum time to wait for cdd to deploy 2 minutes
    # every 3 seconds check if job is still running
    # attempt to hit endpoint - successful return code indicated CDD has started up
    CDD_URL="http://dashi-dev.cbio.mskcc.org:$CDD_PORT/api/"
    CDD_ENDPOINT="http://dashi-dev.cbio.mskcc.org:$CDD_PORT/api/SAMPLE_ID"
    while [ $CDD_DEPLOYMENT_SUCCESS -eq 0 ] ; do
        CURRENT_WAIT_TIME=$(($CURRENT_WAIT_TIME + 10))
        JOB_RUNNING=`jobs | grep "cdd.jar" | grep "Running"`
        if [ -z $JOB_RUNNING ] ; then
            curl --fail -X GET --header 'Accept: */*' $CDD_ENDPOINT
            if [ $? -eq 0 ] ; then
                CDD_DEPLOYMENT_SUCCESS=1
                break
            fi
        else
            echo "CDD unable to start up... canceling tests"
            break
        fi

        if [ $CURRENT_WAIT_TIME -gt $MAXIMUM_WAIT_TIME ] ; then
            echo "CDD is inaccessible (after a 2 min wait time)... canceling tests"
        fi
        sleep $TIME_BETWEEN_CDD_AVAILIBILITY_TESTS
    done
fi

if [ $CDD_DEPLOYMENT_SUCCESS -gt 0 ] ; then
    # First test redcap exports still work (uses CDD to add metadata)
    rsync $JENKINS_PROPERTIES_DIRECTORY/redcap-pipeline/$APPLICATION_PROPERTIES $CMO_PIPELINES_DIRECTORY/redcap/redcap_pipeline/src/main/resources
    cd $CMO_PIPELINES_DIRECTORY/redcap ; mvn clean install
    java "-Dcdd_base_url=$CDD_URL" -jar $REDCAP_JAR -e -s mskimpact_heme -d $TESTING_DIRECTORY_TEMP
    if [ $? -gt 0 ] ; then
        echo "export from redcap using new CDD failed -- new CDD code incompatible with CMO pipelines (redcap)"
    else
        python $CDD_DIRECTORY/integration-tests/verify_metadata_headers.py $TESTING_DIRECTORY_TEMP/data_clinical_sample.txt
        if [ $? -gt 0 ] ; then
            echo "metadata headers in redcap export failed validation"
        else
            REDCAP_EXPORT_TEST_SUCCESS=1
        fi
    fi

    # create default file for testing metadata headers (SAMPLE_ID and PATIENT_ID should always be constant)
    echo "SAMPLE_ID	PATIENT_ID" > $TESTING_DIRECTORY_TEMP/data_clinical_sample.txt

    # add headers using test CDD - failure means new CDD schema not compatible (i.e invalid endpoint, invalid returned json)
    python $IMPORT_SCRIPTS_DIRECTORY/add_clinical_attribute_metadata_headers.py -f $TESTING_DIRECTORY_TEMP/data_clinical_sample.txt -c "$CDD_URL"
    if [ $? -gt 0 ] ; then
        echo "addition of metadata headers failed from test CDD failed -- new CDD code incompatible with CMO pipelines (add_clinical_attribute_metadata_headers.py)"
    else
        ADD_TEST_CDD_HEADERS_SUCCESS=1
    fi

    # compare added headers - difference means CDD schema has unintended consequence
    diff $TESTING_DIRECTORY_TEMP/data_clinical_sample.txt $EXPECTED_METADATA_HEADERS
    if [ $? -gt 0 ] ; then
        echo "added metadata headers differ between  production and test CDD -- new CDD code incompatibe with CMO pipelines (add_clinical_attribute_metadata_headers.py)"
    else
        EQUAL_METADATA_HEADERS_TEST_SUCCESS=1
    fi

    # add headers using test CDD with fake study id - success means new CDD schema not compatible (i.e potential difference in cancer study id endpoint)
    python $IMPORT_SCRIPTS_DIRECTORY/add_clinical_attribute_metadata_headers.py -f $TESTING_DIRECTORY_TEMP/data_clinical_sample.txt -c $CDD_URL -s fake_study_id
    if [ $? -eq 0 ] ; then
        echo "addition of metadata headers succeeded when it should have failed -- new CDD code incompatible with CMO pipelines (add_clinical_attribute_metadata_headers.py)"
    else
        FAKE_STUDY_ID_TEST_SUCCESS=1
    fi
fi

rm -rf $TESTING_DIRECTORY_TEMP

# test that the resource_uri_to_clinical_attribute_mapping.txt is valid and matches Topbriad
python $CCD_SCRIPTS_DIRECTORY/validate_topbraid_uris.py --curated-file $CCD_DOCS_DIRECTORY/resource_uri_to_clinical_attribute_mapping.txt --properties-file $JENKINS_PROPERTIES_DIRECTORY/clinical-data-dictionary/$APPLICATION_PROPERTIES
if [ $? -gt 0 ] ; then
    echo "validate_topbraid_uris.py failed, resource_uri_to_clinical_attribute_mapping.txt is invalid or in conflict with Topbraid"
else
    CDD_TOPBRAID_URI_VALIDATION_SUCCESS=1
fi

# all five tests must pass for integration test to succeed
if [[ $REDCAP_EXPORT_TEST_SUCCESS -eq 0 || $ADD_TEST_CDD_HEADERS_SUCCESS -eq 0 || $EQUAL_METADATA_HEADERS_TEST_SUCCESS -eq 0 || $FAKE_STUDY_ID_TEST_SUCCESS -eq 0 || $CDD_TOPBRAID_URI_VALIDATION_SUCCESS -eq 0 ]] ; then
    echo "Integration tests for CDD failed"
    exit 1
fi
