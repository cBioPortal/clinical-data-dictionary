/*
 * Copyright (c) 2020 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.cdd.repository.topbraid;

import java.util.*;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClinicalAttributeMetadataRepositoryConfiguration {

    @Value("${topbraid.url}")
    private String topbraidUrl;

    @Value("${topbraid.username:}")
    private String topbraidUsername;

    @Value("${topbraid.password:}")
     private String topbraidPassword;

    @Value("${mskVocabulary.url}")
    private String mskVocabularyUrl;

    @Value("${mskVocabulary.username:}")
    private String mskVocabularyUsername;

    @Value("${mskVocabulary.password:}")
     private String mskVocabularyPassword;

    @Bean
    @Qualifier("topBraidSessionConfiguration")
    public TopBraidSessionConfiguration topBraidSessionConfiguration() {
        TopBraidSessionConfiguration topBraidSessionConfiguration = new TopBraidSessionConfiguration(); 
        topBraidSessionConfiguration.setURL(topbraidUrl);
        topBraidSessionConfiguration.setUsername(topbraidUsername);
        topBraidSessionConfiguration.setPassword(topbraidPassword);
        return topBraidSessionConfiguration;
    }

    @Bean
    @Qualifier("mskVocabularySessionConfiguration")
    public TopBraidSessionConfiguration mskVocabularySessionConfiguration() {
        TopBraidSessionConfiguration mskVocabularySessionConfiguration = new TopBraidSessionConfiguration(); 
        mskVocabularySessionConfiguration.setURL(topbraidUrl);
        mskVocabularySessionConfiguration.setUsername(topbraidUsername);
        mskVocabularySessionConfiguration.setPassword(topbraidPassword);
        return mskVocabularySessionConfiguration;
    }

    @Bean
    @Qualifier("topBraidSessionManager")
    public TopBraidSessionManager topBraidSessionManager() {
        return new TopBraidSessionManager(topBraidSessionConfiguration());
    }

    @Bean
    @Qualifier("mskVocabularySessionManager")
    public TopBraidSessionManager mskVocabularySessionManager() {
        return new TopBraidSessionManager(mskVocabularySessionConfiguration());
    }

    @Bean
    @Qualifier("clinicalAttributeMetadataRepositoryTopBraidImpl")
    public ClinicalAttributeMetadataRepositoryTopBraidImpl clinicalAttributeMetadataRepositoryTopBraidImpl() {
        return new ClinicalAttributeMetadataRepositoryTopBraidImpl(topBraidSessionManager());
    }
}
