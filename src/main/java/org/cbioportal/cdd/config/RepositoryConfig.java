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

package org.cbioportal.cdd.config;

import org.cbioportal.cdd.repository.topbraid.KnowledgeSystemsRepository;
import org.cbioportal.cdd.repository.topbraid.MskVocabularyRepository;
import org.cbioportal.cdd.repository.topbraid.TopBraidSessionConfiguration;
import org.cbioportal.cdd.repository.topbraid.TopBraidSessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Value("${topbraid.knowledgeSystems.serviceUrl:}")
    private String topbraidKnowledgeSystemsServiceUrl;

    @Value("${topbraid.knowledgeSystems.loginUrl:}")
    private String topbraidKnowledgeSystemsLoginUrl;

    @Value("${topbraid.knowledgeSystems.username:}")
    private String topbraidKnowledgeSystemsUsername;

    @Value("${topbraid.knowledgeSystems.password:}")
    private String topbraidKnowledgeSystemsPassword;

    @Value("${topbraid.mskVocabulary.serviceUrl:}")
    private String mskVocabularyServiceUrl;

    @Value("${topbraid.mskVocabulary.loginUrl:}")
    private String mskVocabularyLoginUrl;

    @Value("${topbraid.mskVocabulary.username:}")
    private String mskVocabularyUsername;

    @Value("${topbraid.mskVocabulary.password:}")
    private String mskVocabularyPassword;

    @Bean
    @Qualifier("knowledgeSystemsSessionConfiguration")
    public TopBraidSessionConfiguration knowledgeSystemsSessionConfiguration() {
        TopBraidSessionConfiguration knowledgeSystemsSessionConfiguration = new TopBraidSessionConfiguration();
        knowledgeSystemsSessionConfiguration.setServiceURL(topbraidKnowledgeSystemsServiceUrl);
        knowledgeSystemsSessionConfiguration.setLoginURL(topbraidKnowledgeSystemsLoginUrl);
        knowledgeSystemsSessionConfiguration.setUsername(topbraidKnowledgeSystemsUsername);
        knowledgeSystemsSessionConfiguration.setPassword(topbraidKnowledgeSystemsPassword);
        return knowledgeSystemsSessionConfiguration;
    }

    @Bean
    @Qualifier("mskVocabularySessionConfiguration")
    public TopBraidSessionConfiguration mskVocabularySessionConfiguration() {
        TopBraidSessionConfiguration mskVocabularySessionConfiguration = new TopBraidSessionConfiguration();
        mskVocabularySessionConfiguration.setServiceURL(mskVocabularyServiceUrl);
        mskVocabularySessionConfiguration.setLoginURL(mskVocabularyLoginUrl);
        mskVocabularySessionConfiguration.setUsername(mskVocabularyUsername);
        mskVocabularySessionConfiguration.setPassword(mskVocabularyPassword);
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
    @Qualifier("knowledgeSystemsRepository")
    public KnowledgeSystemsRepository knowledgeSystemsRepository() {
        return new KnowledgeSystemsRepository(knowledgeSystemsSessionManager());
    }

    @Bean
    @Qualifier("mskVocabularyRepository")
    public MskVocabularyRepository mskVocabularyRepository() {
        return new MskVocabularyRepository(mskVocabularySessionManager());
    }
}
