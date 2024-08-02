# Compares concept URIs in docs/resource_uri_to_clinical_attribute_mapping.txt to the current concept URIs defined in Graphite.
#
# ./validate_graphite_uris.py --curated-file resource_uri_to_clinical_attribute_mapping_file --properties-file application.properties
#
# Author: Manda Wilson

import optparse
import os.path
import sys
import csv
import re
import configparser
import requests
from requests.auth import HTTPBasicAuth


GRAPHITE_QUERY_TEMPLATE = """
PREFIX cdd: <%s>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?subject ?column_header ?display_name ?attribute_type ?datatype ?description ?priority WHERE { 
	?subject skos:inScheme <%s> .
	?subject rdfs:label ?column_header .
	?subject cdd:attributetype ?attribute_type .
	?subject cdd:datatype ?datatype . 
	?subject cdd:description ?description . 
	?subject cdd:displayname ?display_name . 
	?subject cdd:priority ?priority .
}
"""
NUM_FIELDS_IN_CURATED_FILE = 2
URI_PATTERN_STR = "^[0-9a-z]+-[0-9a-z]+-[0-9a-z]+-[0-9a-z]+-[0-9a-z]+$" # e.g. af41e236-1bf1-46c7-b5a1-bec19fd72f76
URI_PATTERN = re.compile(URI_PATTERN_STR)
COLUMN_HEADER_PATTERN_STR = "^(\")?[A-Z][A-Z_0-9]{0,99}(\"@en)?$" # e.g. "DELIVERED_DOSE"@en or PLATINUM_OS_MONTHS
COLUMN_HEADER_PATTERN = re.compile(COLUMN_HEADER_PATTERN_STR)
MULTIPLE_UNDERSCORES_PATTERN = re.compile("_{2,}")
GRAPHITE_URL_PROPERTY_NAME = "graphite.url"
GRAPHITE_USERNAME_PROPERTY_NAME = "graphite.username"
GRAPHITE_PASSWORD_PROPERTY_NAME = "graphite.password"
GRAPHITE_NAMESPACE_PREFIX_PROPERTY_NAME = "graphite.cddNamespacePrefix"
GRAPHITE_CDD_GRAPH_ID_PROPERTY_NAME = "graphite.cddGraphId"
DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE = "DEFAULT"
errors = []
warnings = []
information = []

# from https://stackoverflow.com/questions/2819696/parsing-properties-file-in-python/2819788#2819788
def add_section_header(fp):
    yield "[%s]\n" % (DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE)
    yield from fp

def query_graphite(graphite_url, graphite_username, graphite_password, graphite_namespace_prefix, graphite_cdd_graph_id):
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': 'application/sparql-results+json',
    }

    data = {
        'query': GRAPHITE_QUERY_TEMPLATE % (graphite_namespace_prefix, graphite_cdd_graph_id) 
    }

    auth = HTTPBasicAuth(graphite_username, graphite_password)

    response = requests.post(graphite_url, headers=headers, data=data, auth=auth)
    if response.status_code != 200:
        sys.stderr.write("ERROR: Failed to query '%s', response status code is '%d', body is '%s'\n" % (graphite_url, response.status_code, response.text))
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
                uri = fields[0]
                uris[uri] = fields[1]
                validate_uri(uri, curated_filename)
                # do not validate the column header
                # if this is in Graphite, it will be validated in that section
                # if it isn't in Graphite, don't worry about what it says
    return uris

def read_graphite_uris(graphite_results):
    uris = {}
    
    for clinical_attribute in graphite_results['results']['bindings']:
        # subject looks like 'http://data.mskcc.org/ontologies/clinical_data_dictionary/C002753'
        uri = clinical_attribute['subject']['value'].split("#")[-1]
        validate_uri(uri, "Graphite")
        validate_column_header(clinical_attribute['column_header']['value'], "Graphite")
        uris[uri] = clinical_attribute['column_header']['value']
    return uris

def compare_uris(curated_uris, graphite_uris):
    # validate that both sets are the same
    curated_key_set = set(curated_uris.keys())
    graphite_key_set = set(graphite_uris.keys())

    in_curated_only = curated_key_set - graphite_key_set
    if in_curated_only:
        information.append("Curated URIs not found in Graphite: '%s'" % (", ".join([ "%s (%s)" % (key, curated_uris[key]) for key in sorted(in_curated_only)])))

    in_graphite_only = graphite_key_set - curated_key_set
    if in_graphite_only:
        errors.append("Graphite URIs not found in curated: '%s'" % (", ".join(sorted(in_graphite_only))))

    keys_in_both = curated_key_set & graphite_key_set
    for key in sorted(keys_in_both):
        if curated_uris[key] != graphite_uris[key].replace("@en", "").strip('"'):
            errors.append("column_header for key '%s' does not match between curated '%s' and Graphite '%s'" % (key, curated_uris[key], graphite_uris[key]))

def usage():
    print('python3 validate_graphite_uris.py --curated-file [path/to/curated/file] --properties-file [path/to/properties/file]')

def main():
    # get command line stuff
    parser = optparse.OptionParser()
    parser.add_option('-c', '--curated-file', action = 'store', dest = 'curated_filename')
    parser.add_option('-p', '--properties-file', action = 'store', dest = 'properties_filename')

    (options, args) = parser.parse_args()
    curated_filename = options.curated_filename
    properties_filename = options.properties_filename

    if not curated_filename:
        print('Curated file is required')
        usage()
        sys.exit(2)
    if not properties_filename:
        print('Properties file is required')
        usage()
        sys.exit(2)
    if not os.path.exists(curated_filename):
        print('No such file:', curated_filename)
        usage()
        sys.exit(2)
    if not os.path.exists(properties_filename):
        print('No such file:', properties_filename)
        usage()
        sys.exit(2)

    config = configparser.RawConfigParser()
    config.read_file(add_section_header(open(properties_filename)))
    try:
        graphite_url = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, GRAPHITE_URL_PROPERTY_NAME)
        graphite_username = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, GRAPHITE_USERNAME_PROPERTY_NAME)
        graphite_password = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, GRAPHITE_PASSWORD_PROPERTY_NAME)
        graphite_namespace_prefix = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, GRAPHITE_NAMESPACE_PREFIX_PROPERTY_NAME)
        graphite_cdd_graph_id = config.get(DEFAULT_SECTION_HEAD_FOR_PROPERTIES_FILE, GRAPHITE_CDD_GRAPH_ID_PROPERTY_NAME)
    except configparser.NoOptionError as noe:
        print("ERROR: %s in properties file" % noe, file=sys.stderr)
        sys.exit(2)

    graphite_results = query_graphite(graphite_url, graphite_username, graphite_password, graphite_namespace_prefix, graphite_cdd_graph_id)

    curated_uris = read_curated_uris(curated_filename)
    graphite_uris = read_graphite_uris(graphite_results)

    compare_uris(curated_uris, graphite_uris)

    # print information to stdout and do not exit with failure error code
    if information:
        for info in information:
            print("INFO:", info)

    # print warnings to stdout and do not exit with failure error code
    if warnings:
        for warning in warnings:
            print("WARNING:", warning)

    # print warnings to stderr and exit with failure error code
    if errors:
        for error in errors:
            print("ERROR: %s" % error, file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    main()
