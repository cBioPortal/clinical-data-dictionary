# Compares concept URIs in docs/resource_uri_to_clinical_attribute_mapping.txt to the current concept URIs defined in Topbraid.
#
# ./validate_topbraid_uris.py --curated-file resource_uri_to_clinical_attribute_mapping_file --properties-file application.properties
#
# Author: Manda Wilson

import optparse
import os.path
import sys
import csv
import re
import ConfigParser
import requests


TOPBRAID_QUERY_TEMPLATE = """
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX cdd:<%s>
        PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
        SELECT ?subject ?column_header ?display_name ?attribute_type ?datatype ?description ?priority
        WHERE {
            GRAPH <%s> {
                ?subject skos:prefLabel ?column_header.
                ?subject cdd:AttributeType ?attribute_type.
                ?subject cdd:Datatype ?datatype.
                ?subject cdd:Description ?description.
                ?subject cdd:DisplayName ?display_name.
                ?subject cdd:Priority ?priority.
            }
        }
"""
NUM_FIELDS_IN_CURATED_FILE = 2
URI_PATTERN_STR = "^C[0-9]{6}$" # e.g. C000000
URI_PATTERN = re.compile(URI_PATTERN_STR)
COLUMN_HEADER_PATTERN_STR = "^(\")?[A-Z][A-Z_0-9]{0,99}(\"@en)?$" # e.g. "DELIVERED_DOSE"@en or PLATINUM_OS_MONTHS
COLUMN_HEADER_PATTERN = re.compile(COLUMN_HEADER_PATTERN_STR)
MULTIPLE_UNDERSCORES_PATTERN = re.compile("_{2,}")
TOPBRAID_SERVICE_URL_PROPERTY_NAME = "topbraid.knowledgeSystems.serviceUrl"
TOPBRAID_LOGIN_URL_PROPERTY_NAME = "topbraid.knowledgeSystems.loginUrl"
TOPBRAID_USERNAME_PROPERTY_NAME = "topbraid.knowledgeSystems.username"
TOPBRAID_PASSWORD_PROPERTY_NAME = "topbraid.knowledgeSystems.password"
TOPBRAID_NAMESPACE_PREFIX_PROPERTY_NAME = "topbraid.knowledgeSystems.cddNamespacePrefix"
TOPBRAID_CDD_GRAPH_ID_PROPERTY_NAME = "topbraid.knowledgeSystems.cddGraphId"
DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE = "DEFAULT"
JSESSION_ID_COOKIE_NAME = "JSESSIONID"
errors = []
warnings = []
information = []

# from https://stackoverflow.com/questions/2819696/parsing-properties-file-in-python/2819788#2819788
class DefaultSectionHeadOnPropertiesFile:

    def __init__(self, fp):
        self.fp = fp
        self.section_head = "[%s]\n" % (DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE)

    def readline(self):
        if self.section_head:
            try:
                return self.section_head
            finally:
                self.section_head = None
        else:
            return self.fp.readline()

def get_logged_in_session_id(topbraid_login_url, topbraid_username, topbraid_password):
    # first we just hit the page and get a session id
    session = requests.Session()
    response = session.get(topbraid_login_url)
    if response.status_code != 200:
        print >> sys.stderr, "ERROR: Initial connection to '%s' failed, response status code is '%d', body is '%s'" % (topbraid_login_url, response.status_code, response.text)
        sys.exit(2)
    initial_jsession_id = session.cookies.get_dict()[JSESSION_ID_COOKIE_NAME]
    # now we login using that session id
    response = session.get(topbraid_login_url + "/j_security_check?j_username=" + topbraid_username + "&j_password=" + topbraid_password, cookies={ JSESSION_ID_COOKIE_NAME : initial_jsession_id })
    if response.status_code != 200:
        print >> sys.stderr, "ERROR: Failed to log into '%s', response status code is '%d', body is '%s'" % (topbraid_login_url, response.status_code, response.text)
        sys.exit(2)
    logged_in_session_id = session.cookies.get_dict()[JSESSION_ID_COOKIE_NAME]
    return logged_in_session_id

def query_topbraid(topbraid_service_url, logged_in_session_id, topbraid_namespace_prefix, topbraid_cdd_graph_id):
    session = requests.Session()
    data = {"format" : "json-simple", "query" : TOPBRAID_QUERY_TEMPLATE % (topbraid_namespace_prefix, topbraid_cdd_graph_id) }
    response = session.post(topbraid_service_url, cookies={ JSESSION_ID_COOKIE_NAME : logged_in_session_id}, data=data)
    if response.status_code != 200:
        print >> sys.stderr, "ERROR: Failed to query '%s', response status code is '%d', body is '%s'" % (topbraid_service_url, response.status_code, response.text)
        sys.exit(2)
    return response.json()

def validate_uri(uri, source):
    """Adds error to errors array if uri does not match expected pattern"""
    if not URI_PATTERN.match(uri):
        errors.append("'%s' does not match expected pattern '%s' in '%s'" % (uri, URI_PATTERN_STR, source))

def validate_column_header(str, source):
    """Adds error to errors array if str does not match expected pattern"""
    # validate
    #   has no whitespace
    #   must begin with letter
    #   no lower case letters
    #   less than or equal to 100 characters
    #   no characters aside from digits
    #   upper case letters
    #   underscore character
    #   do not have 2 underscores in a row
    if not COLUMN_HEADER_PATTERN.match(str):
        errors.append("'%s' does not match expected pattern '%s' in '%s'" % (str, COLUMN_HEADER_PATTERN_STR, source))
    if MULTIPLE_UNDERSCORES_PATTERN.search(str):
        warnings.append("'%s' contains multiple underscores in '%s'" % (str, source))

def read_curated_uris(curated_filename):
    # validate that file is tab delimited
    uris = {}
    with open(curated_filename) as curated_file:
        for line in curated_file:
            line = line.strip()
            fields = line.split("\t")
            if "\t" not in line:
                errors.append("Line '%s' does not have a '\\t' as a delimiter in file '%s'" % (line, curated_filename))
            elif len(fields) != NUM_FIELDS_IN_CURATED_FILE:
                errors.append("Line '%s' has %d field(s) in file '%s', when %d are expectd.  Fields are: %s" % (line, len(fields), curated_filename, NUM_FIELDS_IN_CURATED_FILE, ",".join("'" + field + "'" for field in fields)))
            else:
                uris[fields[0]] = fields[1]
                validate_uri(fields[0], curated_filename)
                # do not validate the column header
                # if this is in Topbraid, it will be validated in that section
                # if it isn't in Topbraid, don't worry about what it says
    return uris

def read_topbraid_uris(topbraid_results):
    uris = {}
    for clinical_attribute in topbraid_results:
        # subject looks like 'http://data.mskcc.org/ontologies/clinical_data_dictionary/C002753'
        uri = clinical_attribute['subject'].split("/")[-1]
        uris[uri] = clinical_attribute['column_header']
        validate_uri(uri, "Topbraid")
        validate_column_header(clinical_attribute['column_header'], "Topbraid")
    return uris

def compare_uris(curated_uris, topbraid_uris):
    # validate that both sets are the same
    curated_key_set = set(curated_uris.keys())
    topbraid_key_set = set(topbraid_uris.keys())

    in_curated_only = curated_key_set - topbraid_key_set
    if in_curated_only:
        information.append("Curated URIs not found in Topbraid: '%s'" % (", ".join([ "%s %s)" % (key, curated_uris[key]) for key in sorted(in_curated_only)])))

    in_topbraid_only = topbraid_key_set - curated_key_set
    if in_topbraid_only:
        errors.append("Topbraid URIs not found in curated: '%s'" % (", ".join(sorted(in_topbraid_only))))

    keys_in_both = curated_key_set & topbraid_key_set
    for key in sorted(keys_in_both):
        if curated_uris[key] != topbraid_uris[key].replace("@en", "").strip('"'):
            errors.append("column_header for key '%s' does not match between curated '%s' and Topbraid '%s'" % (key, curated_uris[key], topbraid_uris[key]))

def usage():
    print 'python validate_topbraid_uris.py --curated-file [path/to/curated/file] --properties-file [path/to/properties/file]'

def main():
    # get command line stuff
    parser = optparse.OptionParser()
    parser.add_option('-c', '--curated-file', action = 'store', dest = 'curated_filename')
    parser.add_option('-p', '--properties-file', action = 'store', dest = 'properties_filename')

    (options, args) = parser.parse_args()
    curated_filename = options.curated_filename
    properties_filename = options.properties_filename

    if not curated_filename:
        print 'Curated file is required'
        usage()
        sys.exit(2)
    if not properties_filename:
        print 'Properties file is required'
        usage()
        sys.exit(2)
    if not os.path.exists(curated_filename):
        print 'No such file:', curated_filename
        usage()
        sys.exit(2)
    if not os.path.exists(properties_filename):
        print 'No such file:', properties_filename
        usage()
        sys.exit(2)

    config = ConfigParser.RawConfigParser()
    config.readfp(DefaultSectionHeadOnPropertiesFile(open(properties_filename)))
    try:
        topbraid_service_url = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, TOPBRAID_SERVICE_URL_PROPERTY_NAME)
        topbraid_login_url = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, TOPBRAID_LOGIN_URL_PROPERTY_NAME)
        topbraid_username = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, TOPBRAID_USERNAME_PROPERTY_NAME)
        topbraid_password = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, TOPBRAID_PASSWORD_PROPERTY_NAME)
        topbraid_namespace_prefix = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, TOPBRAID_NAMESPACE_PREFIX_PROPERTY_NAME)
        topbraid_cdd_graph_id = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, TOPBRAID_CDD_GRAPH_ID_PROPERTY_NAME)
    except ConfigParser.NoOptionError as noe:
        print >> sys.stderr, "ERROR:", noe, "in properties file"
        sys.exit(2)

    jsession_id = get_logged_in_session_id(topbraid_login_url, topbraid_username, topbraid_password)
    topbraid_results = query_topbraid(topbraid_service_url, jsession_id, topbraid_namespace_prefix, topbraid_cdd_graph_id)

    curated_uris = read_curated_uris(curated_filename)
    topbraid_uris = read_topbraid_uris(topbraid_results)

    compare_uris(curated_uris, topbraid_uris)

    # print information to stdout and do not exit with failure error code
    if information:
        for info in information:
            print "INFO:", info

    # print warnings to stdout and do not exit with failure error code
    if warnings:
        for warning in warnings:
            print "WARNING:", warning

    # print warnings to stderr and exit with failure error code
    if errors:
        for error in errors:
            print >> sys.stderr, "ERROR:", error
        sys.exit(1)

if __name__ == '__main__':
    main()
