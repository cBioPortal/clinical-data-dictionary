/*
 * Copyright (c) 2018 - 2020 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

package org.cbioportal.cdd.web;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.cbioportal.cdd.service.util.MSKVocabStudyUtil;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.model.CancerStudy;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.CancerStudyNotFoundException;
import org.cbioportal.cdd.service.exception.FailedCacheRefreshException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author Avery Wang, Manda Wilson
 */
@Api(description="Operations pertaining to the clinical data dictionary")
@CrossOrigin // enable CORS on all endpoints (by default @CrossOrigin allows all origins and the HTTP methods specified in the @RequestMapping annotation
@RestController // shorthand for @Controller, @ResponseBody
@RequestMapping(value = "/api/")
public class ClinicalDataDictionaryController {

    @Autowired
    @Qualifier("servicerouter")
    private ClinicalDataDictionaryService clinicalAttributesService;

    @ApiOperation(value = "Get metadata for all clinical attributes", response = ClinicalAttributeMetadata.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved list of clinical attributes"),
        @ApiResponse(code = 404, message = "Could not find cancer study"),
        @ApiResponse(code = 503, message = "Clinical attribute metadata source unavailable")
        }
    )
    @RequestMapping(method = RequestMethod.GET, value="/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<ClinicalAttributeMetadata> getClinicalAttributeMetadata(
        @ApiParam(value = "Cancer study name e.g. mskimpact")
        @RequestParam(value = "cancerStudy", required = false) String cancerStudyName) {
        return clinicalAttributesService.getClinicalAttributeMetadata(cancerStudyName);
    }

    @ApiOperation(value = "Get metadata for a list of clinical attributes", response = ClinicalAttributeMetadata.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved list of clinical attributes"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 404, message = "Could not find cancer study or column header"),
        @ApiResponse(code = 503, message = "Clinical attribute metadata source unavailable")
        }
    )
    @RequestMapping(method = RequestMethod.POST, value="/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<ClinicalAttributeMetadata> getClinicalAttributeMetadata(
        @ApiParam(value = "Cancer study name e.g. mskimpact")
        @RequestParam(value = "cancerStudy", required = false) String cancerStudyName,
        @ApiParam(value = "List of column headers to retrieve clinical attribute metadata for. For example: [\"PATIENT_ID\", \"SAMPLE_ID\", \"CANCER_TYPE\"]")
        @RequestBody(required = true) List<String> columnHeaders) {
        return clinicalAttributesService.getMetadataByColumnHeaders(cancerStudyName, columnHeaders);
    }

    @ApiOperation(value = "Get metadata for a search term", response = ClinicalAttributeMetadata.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved list of clinical attributes matching search term"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 404, message = "Could not find any clinical attributes matching search term"),
        @ApiResponse(code = 503, message = "Clinical attribute metadata source unavailable")
        }
    )
    @RequestMapping(method = RequestMethod.POST, value="/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<ClinicalAttributeMetadata> getClinicalAttributeMetadataBySearchTerms(
        @ApiParam(value = "Attribute type e.g. PATIENT or SAMPLE")
        @RequestParam(value = "attributeType", required = false) String attributeType,
        @ApiParam(value = "Inclusive search - all search terms must be present when searching")
        @RequestParam(value = "inclusiveSearch", defaultValue = "false", required = true) boolean inclusiveSearch,
        @ApiParam(value = "List of search terms that may be present in the description, display name, or column header. For example: [\"TMB\", \"mutation burden\"]")
        @RequestBody(required = true) List<String> searchTerms) {
        return clinicalAttributesService.getMetadataBySearchTerms(searchTerms, attributeType, inclusiveSearch);
    }

    @ApiOperation(value = "Get metadata for one clinical attribute", response = ClinicalAttributeMetadata.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved clinical attribute"),
        @ApiResponse(code = 404, message = "Could not find cancer study or column header"),
        @ApiResponse(code = 503, message = "Clinical attribute metadata source unavailable")
        }
    )
    @RequestMapping(value = "/{columnHeader}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ClinicalAttributeMetadata getClinicalAttribute(
        @ApiParam(value = "Cancer study name e.g. mskimpact")
        @RequestParam(value = "cancerStudy", required = false) String cancerStudyName,
        @ApiParam(value = "Column header to retrieve clinical attribute metadata for")
        @PathVariable(required = true) String columnHeader) {
        return clinicalAttributesService.getMetadataByColumnHeader(cancerStudyName, columnHeader);
    }

    @ApiOperation(value = "Get all cancer studies", response = CancerStudy.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved list of cancer studies"),
        @ApiResponse(code = 503, message = "Clinical attribute metadata source unavailable")
        }
    )
    @RequestMapping(method = RequestMethod.GET, value = "/cancerStudies", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<CancerStudy> getCancerStudies() {
        return clinicalAttributesService.getCancerStudies();
    }

    @ApiOperation(value = "Refresh clinical attribute cache")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully refreshed cache"),
        @ApiResponse(code = 503, message = "Clinical attribute metadata source unavailable")
        }
    )
    @ApiIgnore
    @RequestMapping(method = RequestMethod.GET, value = "/refreshCache", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> forceResetCache() {
        return clinicalAttributesService.forceResetCache();
    }

    @ExceptionHandler
    public void handleClinicalAttributeNotFound(ClinicalAttributeNotFoundException e, HttpServletResponse response)
        throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "Failed to refresh metadata cache")
    @ExceptionHandler(FailedCacheRefreshException.class)
    public void handleFailedCacheRefreshException() {}

    @ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "Clinical attribute metadata source unavailable")
    @ExceptionHandler(ClinicalMetadataSourceUnresponsiveException.class)
    public void handleClinicalMetadataSourceUnresponsive() {}

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Cancer study not found")
    @ExceptionHandler(CancerStudyNotFoundException.class)
    public void handleCancerStudyNotFound() {}
}
