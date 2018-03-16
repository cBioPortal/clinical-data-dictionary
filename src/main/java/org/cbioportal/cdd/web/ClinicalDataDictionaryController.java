/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.model.OverridePolicy;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.OverridePolicyNotFoundException;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * @author Avery Wang, Manda Wilson 
 */
@CrossOrigin // enable CORS on all endpoints (by default @CrossOrigin allows all origins and the HTTP methods specified in the @RequestMapping annotation
@RestController // shorthand for @Controller, @ResponseBody
@RequestMapping(value = "/api/")
public class ClinicalDataDictionaryController {

    @Autowired
    private ClinicalDataDictionaryService clinicalAttributesService;

    @RequestMapping(method = RequestMethod.GET, value="/")
    public Iterable<ClinicalAttributeMetadata> getClinicalAttributeMetadata(@RequestParam(value = "overridePolicy", required = false) String overridePolicyName) {
        return clinicalAttributesService.getClinicalAttributeMetadata(overridePolicyName);
    }

    @RequestMapping(method = RequestMethod.POST, value="/")
    public Iterable<ClinicalAttributeMetadata> getClinicalAttributeMetadata(@RequestParam(value = "overridePolicy", required = false) String overridePolicyName, @RequestBody List<String> columnHeaders) {
        return clinicalAttributesService.getMetadataByColumnHeaders(overridePolicyName, columnHeaders);
    }

    @RequestMapping(value = "/{columnHeader}", method = RequestMethod.GET)
    public ClinicalAttributeMetadata getClinicalAttribute(@RequestParam(value = "overridePolicy", required = false) String overridePolicyName, @PathVariable String columnHeader) {
        return clinicalAttributesService.getMetadataByColumnHeader(overridePolicyName, columnHeader);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/overridePolicies")
    public Iterable<OverridePolicy> getOverridePolicies() {
        return clinicalAttributesService.getOverridePolicies();
    }
    
    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Clinical attribute not found")
    @ExceptionHandler(ClinicalAttributeNotFoundException.class)
    public void handleClinicalAttributeNotFound() {}

    @ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "Clinical attribute metadata source unavailable")
    @ExceptionHandler(ClinicalMetadataSourceUnresponsiveException.class)
    public void handleClinicalMetadataSourceUnresponsive() {}

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Override policy not found")
    @ExceptionHandler(OverridePolicyNotFoundException.class)
    public void handleOverridePolicyNotFound() {}
}
