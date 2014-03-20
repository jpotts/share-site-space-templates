package com.ecmarchitect.test;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class ShareSiteSpaceTemplateTest {
	    
    private static final String ADMIN_USER_NAME = "admin";

    static Logger logger = Logger.getLogger(ShareSiteSpaceTemplateTest.class);

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("SiteService")
    protected SiteService siteService;

    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;
    
    @Test
    public void testCreateWithoutSpaceTemplate() {
    	AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

    	String siteShortName = "test-site-" + System.currentTimeMillis();

    	SiteInfo testSite = siteService.createSite("site-dashboard", siteShortName, "test site", "test site description", SiteVisibility.PUBLIC);
    	
    	NodeRef documentLibrary = siteService.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);

    	assertEquals(documentLibrary, null);
    	
    	siteService.deleteSite(siteShortName);
    }
    
    @Test
    public void testCreateWithSpaceTemplate() {
    	AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

    	String query = "+PATH:\"/app:company_home/app:dictionary/app:space_templates\"";
		ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);

		NodeRef stFolder = rs.getNodeRef(0);
		String presetName = "site-dashboard";
		Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, presetName);
		NodeRef spaceTemplate = nodeService.createNode(stFolder,
													   ContentModel.ASSOC_CONTAINS,
													   QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, presetName),
													   ContentModel.TYPE_FOLDER,
													   contentProps).getChildRef();

		String subFolderName = "testFolder";
		contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, presetName);
		nodeService.createNode(spaceTemplate,
							   ContentModel.ASSOC_CONTAINS,
							   QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, subFolderName),
							   ContentModel.TYPE_FOLDER,
							   contentProps).getChildRef();

    	String siteShortName = "test-site-" + System.currentTimeMillis();
		
		SiteInfo testSite = siteService.createSite(presetName, siteShortName, "test site", "test site description", SiteVisibility.PUBLIC);
    	
    	NodeRef documentLibrary = siteService.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);
    
    	List<ChildAssociationRef> children = nodeService.getChildAssocs(documentLibrary);
    	
    	assertEquals(1, children.size());

    	siteService.deleteSite(siteShortName);
    	nodeService.deleteNode(spaceTemplate);
    }
}
