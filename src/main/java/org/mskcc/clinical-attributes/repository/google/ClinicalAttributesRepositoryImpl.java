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

package org.mskcc.clinical_attributes.repository.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Strings;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import org.mskcc.clinical_attributes.model.ClinicalAttribute;
import org.mskcc.clinical_attributes.repository.ClinicalAttributesRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Avery Wang, Manda Wilson
 */
@Repository
public class ClinicalAttributesRepositoryImpl implements ClinicalAttributesRepository {

    @Value("${google.id}")
    private String googleId;
    @Value("${google.pw}")
    private String googlePw;
    @Value("${importer.spreadsheet_service_appname}")
    private String appName;
    @Value("${importer.spreadsheet}")
    private String gdataSpreadsheet;
    @Value("${importer.google.service.email}")
    private String googleServiceEmail;
    @Value("${importer.google.service.private.key.file}")
    private String googleServicePrivateKeyFile;
    @Value ("${importer.clinical_attributes_worksheet}")
    private String clinicalAttributesWorksheet;

    private SpreadsheetService spreadsheetService;

    private static final Logger logger = LoggerFactory.getLogger(ClinicalAttributesRepositoryImpl.class);

    public SpreadsheetService getSpreadsheetService() {
        if (spreadsheetService == null) {
            initSpreadsheetService();
        }
        return spreadsheetService;
    }

    /**
     * Gets an array of ClinicalAttribute.
     * @return ClinicalAttribute[] array 
     */
    public List<ClinicalAttribute> getClinicalAttribute() {
        // generate matrix representing each record in metadata worksheet
        ArrayList<ArrayList<String>> clinicalAttributesMatrix = getWorksheetData(gdataSpreadsheet, clinicalAttributesWorksheet);
        // initialize array to store all metadata bojects
        List<ClinicalAttribute> clinicalAttributeMetadataList = getClinicalAttributeFromMatrix(clinicalAttributesMatrix);
        return clinicalAttributeMetadataList;
    }

    /**
     * Constructs a collection of objects of the given classname from the given matrix.
     *
     * @param metadataMatrix ArrayList<ArrayList<String>>
     * @return List<ClinicalAttribute> list
     */
    private List<ClinicalAttribute> getClinicalAttributeFromMatrix(ArrayList<ArrayList<String>> metadataMatrix) {
        logger.debug("getClinicalAttributeFromMatrix() -- metadataMatrix.size(): " + metadataMatrix.size());
        List<ClinicalAttribute> clinicalAttributeMetadataList = new ArrayList<ClinicalAttribute>(metadataMatrix.size() - 1);
        // we start at one and subtract 1 from metadataMatrix size because row 0 is the column headers
        for (int row = 1; row < metadataMatrix.size(); row++) {
            ArrayList<String> record = metadataMatrix.get(row);
            ClinicalAttribute clinicalAttributeMetadata = new ClinicalAttribute(record.get(0),
                record.get(1),
                record.get(2),
                record.get(3),
                record.get(4),
                record.get(5));
            clinicalAttributeMetadataList.add(clinicalAttributeMetadata);
        }
        return clinicalAttributeMetadataList;
    }

    private void initSpreadsheetService() {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = new JacksonFactory();
            String [] SCOPESArray= {"https://spreadsheets.google.com/feeds", "https://docs.google.com/feeds"};
            final List SCOPES = Arrays.asList(SCOPESArray);
            GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(googleServiceEmail)
                .setServiceAccountScopes(SCOPES)
                .setServiceAccountPrivateKeyFromP12File(new File(googleServicePrivateKeyFile)).build();
            spreadsheetService = new SpreadsheetService("data");
            spreadsheetService.setOAuth2Credentials(credential);
        } catch (IOException | GeneralSecurityException e) {
            // we don't throw the exception, we will just return no data
            // TODO we should probably throw an exception the user must catch
            logger.error("initSpreadsheetService():", e);
        }
    }


    /**
     * Gets the spreadsheet.
     *
     * @param spreadsheetName String
     * @returns SpreadsheetEntry
     * @throws Exception
     */
    private SpreadsheetEntry getSpreadsheet(String spreadsheetName) throws Exception {
        FeedURLFactory factory = FeedURLFactory.getDefault();
        SpreadsheetFeed feed = null;
        // error happens here
        feed = spreadsheetService.getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
        for (SpreadsheetEntry entry : feed.getEntries()) {
            logger.info("getSpreadsheet(): " + spreadsheetName + " title: " + entry.getTitle().getPlainText());
            if (entry.getTitle().getPlainText().equals(spreadsheetName)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets the worksheet feed.
     *
     * @param spreadsheetName String
     * @param worksheetName String
     * @returns WorksheetFeed
     * @throws Exception
     */
    private WorksheetEntry getWorksheet(String spreadsheetName, String worksheetName) throws Exception {
        // first get the spreadsheet
        SpreadsheetEntry spreadsheet = getSpreadsheet(spreadsheetName);
        if (spreadsheet != null) {
            WorksheetFeed worksheetFeed = spreadsheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
                if (worksheet.getTitle().getPlainText().equals(worksheetName)) {
                    return worksheet;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param worksheetName
     * @param columnName
     * @return A List of String values from a specified column in a specified worksheet
     *
     */
    private List<String> getWorksheetDataByColumnName(String worksheetName, String columnName) {
        com.google.common.base.Preconditions.checkState(!Strings.isNullOrEmpty(this.gdataSpreadsheet),
                "The Google spreadsheet has not been defined.");
        com.google.common.base.Preconditions.checkArgument(!Strings.isNullOrEmpty(worksheetName),
                "A worksheet name is required");
        com.google.common.base.Preconditions.checkArgument(!Strings.isNullOrEmpty(columnName),
                "A worksheet column name is required");
        return null;
    }

    /**
     * Helper function to retrieve the given google worksheet data matrix. as a
     * list of string lists.
     *
     * @param spreadsheetName String
     * @param worksheetName String
     * @return ArrayList<ArrayList<String>>
     */
    private ArrayList<ArrayList<String>> getWorksheetData(String spreadsheetName, String worksheetName) {
        this.spreadsheetService = getSpreadsheetService();
        ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();
        logger.info("getWorksheetData(): " + spreadsheetName + ", " + worksheetName);
        try {
            //login();
            WorksheetEntry worksheet = getWorksheet(spreadsheetName, worksheetName);
            if (worksheet != null) {
                ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
                if (feed != null && feed.getEntries().size() > 0) {
                    boolean needHeaders = true;
                    for (ListEntry entry : feed.getEntries()) {
                        if (needHeaders) {
                            ArrayList<String> headers = new ArrayList<String>(entry.getCustomElements().getTags());
                            toReturn.add(headers);
                            needHeaders = false;
                        }
                        ArrayList<String> customElements = new ArrayList<String>();
                        for (String tag : toReturn.get(0)) {
                            String value = entry.getCustomElements().getValue(tag);
                            if (value == null) {
                                value = "";
                            }
                            customElements.add(value);
                        }
                        toReturn.add(customElements);
                    }
                } else {
                    logger.info("Worksheet contains no entries!");
                }
            }
        } catch (Exception e) {
            System.err.println("Problem connecting to " + spreadsheetName + ":" + worksheetName);
            throw new RuntimeException(e);
        }
        return toReturn;
    }
}
