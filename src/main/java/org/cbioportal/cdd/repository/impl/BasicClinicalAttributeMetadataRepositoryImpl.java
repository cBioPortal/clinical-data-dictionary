/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
*/

package org.cbioportal.cdd.repository.impl;

import java.io.InputStream;
import java.util.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;
import org.springframework.stereotype.Repository;
/**
 *
 * @author ochoaa
 */
@Repository
public class BasicClinicalAttributeMetadataRepositoryImpl implements ClinicalAttributeMetadataRepository {
    String rdfFilename = "/Users/ochoaa/cbio-projects/clinical-data-dictionary/src/main/resources/clinical_data_dictionary_using_prefLabel_2018_04_05.rdf";
    String relativeURI = "http://data.mskcc.org/ontologies/clinical_data_dictionary/";

    @Override
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        List<ClinicalAttributeMetadata> clinicalAttributes = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(rdfFilename);
        if (in == null) {
            throw new RuntimeException("File not found: " + rdfFilename);
        }
        model.read(in, relativeURI);
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            String columnHeader = "";
            String displayName = "";
            String description = "";
            String datatype = "";
            String attributeType = "";
            String priority = "";

            // select all RDF subjects with matching URI to collect the clinical attribute metadata
            Resource subject = iter.next();
            SimpleSelector selector = new SimpleSelector(subject, null, (RDFNode) null);
            StmtIterator stIter = model.listStatements(selector);
            while (stIter.hasNext()) {
                Statement stmt      = stIter.nextStatement();
                Property  stPredicate = stmt.getPredicate();
                String property = stPredicate.getLocalName();
                RDFNode value = stmt.getObject();
                if (property.equalsIgnoreCase("prefLabel")) {
                    columnHeader = value.toString();
                } else if (property.equalsIgnoreCase("displayName")) {
                    displayName = value.toString();
                } else if (property.equalsIgnoreCase("description")) {
                    description = value.toString();
                } else if (property.equalsIgnoreCase("datatype")) {
                    datatype = value.toString();
                } else if (property.equalsIgnoreCase("attributeType")) {
                    attributeType = value.toString();
                } else if (property.equalsIgnoreCase("priority")) {
                    priority = String.valueOf(stmt.getInt());
                }
            }
            // if any of the metadata values are null then skip - either an error
            // occurred while parsing the metadata or the current RDF object is not a
            // clinical attribute and instead is another topbraid concept or meta information
            if (columnHeader.isEmpty() || displayName.isEmpty() || description.isEmpty() || datatype.isEmpty() || attributeType.isEmpty() || priority.isEmpty()) {
                // TODO: LOG if there is an issue with one of these
                continue;
            }
            clinicalAttributes.add(new ClinicalAttributeMetadata(columnHeader, displayName, description, datatype, attributeType, priority));
        }
        return clinicalAttributes;
    }

    @Override
    public Map<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
