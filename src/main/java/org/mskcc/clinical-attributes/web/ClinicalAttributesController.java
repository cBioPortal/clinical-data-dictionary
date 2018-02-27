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

package org.mskcc.clinical_attributes.web;

import org.mskcc.clinical_attributes.model.ClinicalAttribute;
import org.mskcc.clinical_attributes.service.ClinicalAttributesService;
import org.mskcc.clinical_attributes.service.exception.ClinicalAttributeNotFoundException;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonView;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Manda Wilson 
 */
@RestController // shorthand for @Controller, @ResponseBody
@RequestMapping(value = "/api/")
public class ClinicalAttributesController {

    @Autowired
    private ClinicalAttributesService clinicalAttributesService;

    @RequestMapping(method = RequestMethod.GET, value="/{studyId}")
    public Iterable<ClinicalAttribute> getClinicalAttributes(@PathVariable String studyId, @RequestParam(value = "normalizedColumnHeaders", required = false) String normalizedColumnHeaders) {
        if (normalizedColumnHeaders != null) {
            return clinicalAttributesService.getMetadataByNormalizedColumnHeaders(studyId, Arrays.asList(normalizedColumnHeaders.split(",")));
        }
        // otherwise return all clinical attributes
        return clinicalAttributesService.getClinicalAttributes(studyId);
    }

    @RequestMapping(value = "/{studyId}/{normalizedColumnHeader}", method = RequestMethod.GET)
    public ClinicalAttribute getClinicalAttribute(@PathVariable String studyId, @PathVariable String normalizedColumnHeader) {
        return clinicalAttributesService.getMetadataByNormalizedColumnHeader(studyId, normalizedColumnHeader);
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Clinical attribute not found")
    @ExceptionHandler(ClinicalAttributeNotFoundException.class)
    public void handleClinicalAttributeNotFound() {}
}

