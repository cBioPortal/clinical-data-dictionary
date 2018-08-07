# Compares concept URIs in docs/resource_uri_to_clinical_attribute_mapping.txt to the current concept URIs defined in Topbraid.
#
# ./validate_topbraid_uris.py --curated-file resource_uri_to_clinical_attribute_mapping_file --topbraid-file topbriad_clinical_attribute_sparql_file
#
# Author: Manda Wilson

import optparse
import os.path
import sys
import csv
import re

"""
Download the current concept URIs for clinical attributes in Topbraid as a TSV file from https://evn.mskcc.org/edg/tbl/swp?_viewClass=endpoint:HomePage:

PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX cdd:<http://data.mskcc.org/ontologies/clinical_data_dictionary/>
        PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
        SELECT ?column_header ?display_name ?attribute_type ?datatype ?description ?priority
        WHERE {
            GRAPH <urn:x-evn-master:clinical_data_dictionary> {
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
errors = []
warnings = []

def validate_uri(uri, filename):
    """Adds error to errors array if uri does not match expected pattern"""
    if not URI_PATTERN.match(uri):
        errors.append("'%s' does not match expected pattern '%s' in file '%s'" % (uri, URI_PATTERN_STR, filename))

def validate_column_header(str, filename):
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
        errors.append("'%s' does not match expected pattern '%s' in file '%s'" % (str, COLUMN_HEADER_PATTERN_STR, filename))
    if MULTIPLE_UNDERSCORES_PATTERN.search(str):
        warnings.append("'%s' contains multiple underscores in file '%s'" % (str, filename))

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
                validate_column_header(fields[1], curated_filename)
    return uris

def read_topbraid_uris(topbraid_filename):
    uris = {}
    with open(topbraid_filename) as topbraid_file:
        reader = csv.DictReader(topbraid_file, dialect='excel-tab',)
        for row in reader:
            # subject looks like 'cdd:C001745'
            uri = row['subject'].split(":")[1]
            uris[uri] = row['column_header']
            validate_uri(uri, topbraid_filename)
            validate_column_header(row['column_header'], topbraid_filename)
    return uris

def compare_uris(curated_uris, topbraid_uris):
    # validate that both sets are the same
    curated_key_set = set(curated_uris.keys())
    topbraid_key_set = set(topbraid_uris.keys())

    in_curated_only = curated_key_set - topbraid_key_set
    if in_curated_only:
        errors.append("Curated URIs not found in Topbraid: '%s'" % (", ".join(sorted(in_curated_only))))

    in_topbraid_only = topbraid_key_set - curated_key_set
    if in_topbraid_only:
        errors.append("Topbraid URIs not found in curated: '%s'" % (", ".join(sorted(in_topbraid_only))))

    keys_in_both = curated_key_set & topbraid_key_set
    for key in sorted(keys_in_both):
        if curated_uris[key] != topbraid_uris[key].replace("@en", "").strip('"'):
            errors.append("column_header for key '%s' does not match between curated '%s' and Topbraid '%s'" % (key, curated_uris[key], topbraid_uris[key]))

def usage():
    print 'python validate_topbraid_uris.py --curated-file [path/to/curated/file] --topbraid-file [path/to/topbraid/file]'

def main():
    # get command line stuff
    parser = optparse.OptionParser()
    parser.add_option('-c', '--curated-file', action = 'store', dest = 'curated_filename')
    parser.add_option('-t', '--topbraid-file', action = 'store', dest = 'topbraid_filename')

    (options, args) = parser.parse_args()
    curated_filename = options.curated_filename
    topbraid_filename = options.topbraid_filename

    if not curated_filename:
        print 'Curated file is required'
        usage()
        sys.exit(2)
    if not topbraid_filename:
        print 'Topbraid file is required'
        usage()
        sys.exit(2)
    if not os.path.exists(curated_filename):
        print 'No such file:', curated_filename
        usage()
        sys.exit(2)
    if not os.path.exists(topbraid_filename):
        print 'No such file:', topbraid_filename
        usage()
        sys.exit(2)

    curated_uris = read_curated_uris(curated_filename)
    topbraid_uris = read_topbraid_uris(topbraid_filename)

    compare_uris(curated_uris, topbraid_uris)

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
