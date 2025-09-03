package com.cyverse.api.services;

import com.cyverse.api.config.LdapServiceConfig;
import com.cyverse.api.config.UserPortalServiceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class UserPortalServiceTest {
    @Mock
    private UserPortalServiceConfig config;

    @Spy
    @InjectMocks
    UserPortalService userPortalService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSingularProperty() {
        String aware_channels = "aware_channels";
        String funding_agencies = "funding_agencies";
        String universities = "universities";
        String genders = "genders";

        String singularIDac = userPortalService.getSingularPropertyIdFieldName(aware_channels);
        Assertions.assertEquals("aware_channel_id", singularIDac);

        String singularIDfa = userPortalService.getSingularPropertyIdFieldName(funding_agencies);
        Assertions.assertEquals("funding_agency_id", singularIDfa);

        String singularIDu = userPortalService.getSingularPropertyIdFieldName(universities);
        Assertions.assertEquals("university_id", singularIDu);

        String singularIDg = userPortalService.getSingularPropertyIdFieldName(genders);
        Assertions.assertEquals("gender_id", singularIDg);
    }
}
