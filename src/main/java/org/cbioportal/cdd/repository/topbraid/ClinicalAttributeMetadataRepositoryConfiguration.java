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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClinicalAttributeMetadataRepositoryConfiguration {

    @Value("${topbraid.knowledgesystems.base_url:}")
    private String topbraidKnowledgeSystemsBaseUrl;

    @Value("${topbraid.knowledgesystems.username:}")
    private String topbraidKnowledgeSystemsUsername;

    @Value("${topbraid.knowledgesystems.password:}")
    private String topbraidKnowledgeSystemsPassword;

    @Value("${topbraid.mskVocabulary.base_url}")
    private String mskVocabularyUrl;

    @Value("${topbraid.mskVocabulary.username:}")
    private String mskVocabularyUsername;

    @Value("${topbraid.mskVocabulary.password:}")
    private String mskVocabularyPassword;

    @Bean
    @Qualifier("knowledgeSystemsSessionConfiguration")
    public TopBraidSessionConfiguration knowledgeSystemsSessionConfiguration() {
        TopBraidSessionConfiguration knowledgeSystemsSessionConfiguration = new TopBraidSessionConfiguration(); 
        knowledgeSystemsSessionConfiguration.setURL(topbraidKnowledgeSystemsBaseUrl);
        knowledgeSystemsSessionConfiguration.setUsername(topbraidKnowledgeSystemsUsername);
        knowledgeSystemsSessionConfiguration.setPassword(topbraidKnowledgeSystemsPassword);
        return knowledgeSystemsSessionConfiguration;
    }

    @Bean
    @Qualifier("mskVocabularySessionConfiguration")
    public TopBraidSessionConfiguration mskVocabularySessionConfiguration() {
        TopBraidSessionConfiguration mskVocabularySessionConfiguration = new TopBraidSessionConfiguration(); 
        mskVocabularySessionConfiguration.setURL(topbraidKnowledgeSystemsBaseUrl);
        mskVocabularySessionConfiguration.setUsername(topbraidKnowledgeSystemsUsername);
        mskVocabularySessionConfiguration.setPassword(topbraidKnowledgeSystemsPassword);
        return mskVocabularySessionConfiguration;
    }

    @Bean
    @Qualifier("knowledgeSystemsSessionManager")
    public TopBraidSessionManager knowledgeSystemsSessionManager() {
        return new TopBraidSessionManager(knowledgeSystemsSessionConfiguration());
    }

    @Bean
    @Qualifier("mskVocabularySessionManager")
    public TopBraidSessionManager mskVocabularySessionManager() {
        return new TopBraidSessionManager(mskVocabularySessionConfiguration());
    }

    @Bean
    @Qualifier("knowledgeSystemsClinicalAttributeMetadataRepository")
    public KnowledgeSystemsClinicalAttributeMetadataRepository knowledgeSystemsClinicalAttributeMetadataRepository() {
        return new KnowledgeSystemsClinicalAttributeMetadataRepository(knowledgeSystemsSessionManager());
    }

    @Bean
    @Qualifier("mskVocabularyClinicalAttributeMetadataRepository")
    public MskVocabularyClinicalAttributeMetadataRepository mskVocabularyClinicalAttributeMetadataRepository() {
        return new MskVocabularyClinicalAttributeMetadataRepository(mskVocabularySessionManager());
    }
}
