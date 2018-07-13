#!/bin/python

import os
import sys
import linecache

RESULT_KEY = "RESULT"
HEADER_KEY = "HEADER"

metadata_header_file = sys.argv[1]
if not os.path.exists(metadata_header_file):
    print "Error, specified file not found: " + metadata_header_file
    sys.exit(1)

DISPLAY_NAME_METADATA_HEADER = linecache.getline(metadata_header_file, 1)
DESCRIPTION_METADATA_HEADER = linecache.getline(metadata_header_file, 2)
DATATYPE_METADATA_HEADER = linecache.getline(metadata_header_file, 3)
PRIORITY_METADATA_HEADER = linecache.getline(metadata_header_file, 4)
NORMALIZED_COLUMN_HEADER = linecache.getline(metadata_header_file, 5)

metadata_headers = [DISPLAY_NAME_METADATA_HEADER, DESCRIPTION_METADATA_HEADER, DATATYPE_METADATA_HEADER, PRIORITY_METADATA_HEADER]

# boolean tracking first four lines are commented out (metadata) and fifth line is not (regular column headers)
headers_present = all([True if header.startswith("#") and len(header.rstrip().split("\t")) == len(NORMALIZED_COLUMN_HEADER.rstrip().split("\t")) else False for header in metadata_headers]) and not NORMALIZED_COLUMN_HEADER.startswith("#")
display_name_metadata_header_is_valid = all([True if display_name else False for display_name in DISPLAY_NAME_METADATA_HEADER.replace("#","").rstrip().split("\t")])
description_metadata_header_is_valid = all([True if description else False for description in DESCRIPTION_METADATA_HEADER.replace("#","").rstrip().split("\t")])
datatype_metadata_header_is_valid = all([True if datatype == "NUMBER" or datatype == "STRING" else False for datatype in DATATYPE_METADATA_HEADER.replace("#","").rstrip().split("\t")])
priority_metadata_header_is_valid = all([True if priority.isdigit() else False for priority in PRIORITY_METADATA_HEADER.replace("#","").rstrip().split("\t")])

validation_test_map = {"Headers Present" : {RESULT_KEY : headers_present, HEADER_KEY : NORMALIZED_COLUMN_HEADER},
            "Display Name Header Validation" : {RESULT_KEY : display_name_metadata_header_is_valid, HEADER_KEY : DISPLAY_NAME_METADATA_HEADER},
            "Description Header Validation" : {RESULT_KEY : description_metadata_header_is_valid, HEADER_KEY : DESCRIPTION_METADATA_HEADER},
            "Datatype Header Validation" : {RESULT_KEY : datatype_metadata_header_is_valid, HEADER_KEY : DATATYPE_METADATA_HEADER},
            "Priority Header Validaiton" : {RESULT_KEY : priority_metadata_header_is_valid, HEADER_KEY : PRIORITY_METADATA_HEADER}}

for test_name, test_results in validation_test_map.items():
    if not test_results[RESULT_KEY]:
        print "Error: header check, " + test_name + ", failed. Returned header is " + test_results[HEADER_KEY]
        sys.exit(1)
