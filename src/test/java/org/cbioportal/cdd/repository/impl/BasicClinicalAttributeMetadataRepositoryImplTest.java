/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.cdd.repository.impl;

import java.io.InputStream;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author ochoaa
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasicClinicalAttributeMetadataRepositoryImplTest {
    String rdfFilename = "/Users/ochoaa/cbio-projects/clinical-data-dictionary/src/main/resources/clinical_data_dictionary_using_prefLabel_2018_04_05.rdf";
    String relativeURI = "http://data.mskcc.org/ontologies/clinical_data_dictionary/";
    
    /**
     * Reading the RDF File.
     * 
     * subject: http://data.mskcc.org/ontologies/clinical_data_dictionary/C001924
     * predicate: http://www.w3.org/1999/02/22-rdf-syntax-ns#type
     * object: http://data.mskcc.org/ontologies/clinical_data_dictionary/ClinicalAttribute
     */
    @Test
    public void testGetClinicalAttributeMetadata() {
        List<ClinicalAttributeMetadata> list = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(rdfFilename);
        if (in == null) {
            throw new RuntimeException("File not found: " + rdfFilename);
        }
        model.read(in, relativeURI);
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            Resource subject = iter.next();
            //override C001427
            if (!subject.toString().contains("C001924")) {
                continue;
            }
            RDFNode n = null;
            SimpleSelector selector = new SimpleSelector(subject, null, n);
            StmtIterator stIter = model.listStatements(selector);
            
            String columnHeader = "";
            String displayName = "";
            String description = "";
            String datatype = "";
            String attributeType = "";
            String priority = "";
            while (stIter.hasNext()) {
                Statement stmt      = stIter.nextStatement();  // get next statement
                Property  stPredicate = stmt.getPredicate();   // get the predicate
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
            ClinicalAttributeMetadata cam = new ClinicalAttributeMetadata(columnHeader, displayName, description, datatype, attributeType, priority);
            
        }
    }
    
    @Test
    public void testGetClinicalAttributeMetadataOverrides() {
        List<ClinicalAttributeMetadata> list = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(rdfFilename);
        if (in == null) {
            throw new RuntimeException("File not found: " + rdfFilename);
        }
        model.read(in, relativeURI);
        
        StmtIterator stIter = model.listStatements();
        
        Set<Property> subjectsWithOverrides = new HashSet<>();
        Set<RDFNode> nodesWithOverrides = new HashSet<>();
        while (stIter.hasNext()) {
            Statement stmt = stIter.nextStatement();  // get next statement
            Resource subject = stmt.getSubject();
            Property  stPredicate = stmt.getPredicate();   // get the predicate
            RDFNode   stObject    = stmt.getObject();      // get the object i.e., ClinicalAttribute
            if ((stObject.toString().contains("http://data.mskcc.org/ontologies/clinical_data_dictionary/ClinicalAttributeOverride") && stPredicate.getLocalName().equalsIgnoreCase("type")) || 
                    stPredicate.getLocalName().equalsIgnoreCase("studyid")) {
                subjectsWithOverrides.add(stPredicate);
                nodesWithOverrides.add(stObject);
            }
        }
        System.out.println("\n\n\nRDFNODES WITH OVERRIDES");
        for (RDFNode n : nodesWithOverrides) {
            System.out.println("NODE: :" + n.toString());
            SimpleSelector selector = new SimpleSelector(null, null, n);
            StmtIterator iter = model.listStatements(selector);
            while (iter.hasNext()) {
                Statement stmt = iter.nextStatement();
                System.out.println(stmt.toString());
            }
        }
    }
    
    
    @Test
    public void testGetClinicalAttributeOVerridesMetadata() {
        List<ClinicalAttributeMetadata> list = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(rdfFilename);
        if (in == null) {
            throw new RuntimeException("File not found: " + rdfFilename);
        }
        model.read(in, relativeURI);
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            //override C001427
            Resource subject = iter.next();
            if (!subject.toString().contains("HLA_B_mel_cell_2016")) {
                continue;
            }
            System.out.println("\n\n\nSUBJECT CONTAINS HLA_B_mel_cell_2016");
            StmtIterator properties = subject.listProperties();
            System.out.println("\n\nPROPERTIES");
            while (properties.hasNext()) {
                Statement pStatement = properties.nextStatement();
                System.out.println(pStatement.toString());
            }

            SimpleSelector selector = new SimpleSelector(subject, null, (RDFNode) null);
            StmtIterator stIter = model.listStatements(selector);
            System.out.println("\nadditional stuff\n");
            while (stIter.hasNext()) {
                Statement stmt      = stIter.nextStatement();  // get next statement
                Property  stPredicate = stmt.getPredicate();   // get the predicate
                RDFNode   stObject    = stmt.getObject();      // get the object i.e., ClinicalAttribute
                if (stPredicate.getLocalName().equalsIgnoreCase("priority")) {
                    System.out.println("\nPRIORITY\n" + stmt.toString());
                    System.out.println(String.valueOf(stmt.getInt()) + "\n");
                }
                String[] array = new String[]{subject.toString(), stPredicate.getLocalName(), stObject.toString()};
                System.out.println(StringUtils.join(array, "\t"));
            }
            
        }
    }
    
}
