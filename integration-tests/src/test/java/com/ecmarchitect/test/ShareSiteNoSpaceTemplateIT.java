package com.ecmarchitect.test;

import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(value = AlfrescoTestRunner.class)
public class ShareSiteNoSpaceTemplateIT extends AbstractAlfrescoIT {

    private static final String ADMIN_USER_NAME = "admin";

    static Logger logger = Logger.getLogger(ShareSiteNoSpaceTemplateIT.class);

    @Test
    public void testCreateWithoutSpaceTemplate() {
        SiteService siteService = getServiceRegistry().getSiteService();
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        String siteShortName = "test-site-" + System.currentTimeMillis();
        logger.debug("without space template: " + siteShortName);
        SiteInfo testSite = siteService.createSite("site-dashboard", siteShortName, "test site", "test site description", SiteVisibility.PUBLIC);

        NodeRef documentLibrary = siteService.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);

        assertEquals(null, documentLibrary);

        siteService.deleteSite(siteShortName);
    }

}
