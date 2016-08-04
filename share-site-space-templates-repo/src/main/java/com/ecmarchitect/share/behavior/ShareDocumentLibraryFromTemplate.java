package com.ecmarchitect.share.behavior;
import static org.alfresco.repo.site.SiteModel.PROP_SITE_PRESET;
import static org.alfresco.repo.site.SiteModel.TYPE_SITE;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class ShareDocumentLibraryFromTemplate implements NodeServicePolicies.OnCreateNodePolicy {

	private static final String DOCUMENT_LIBRARY = "documentLibrary";
	// Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private SiteService siteService;

    // Behaviors
    private Behaviour onCreateNode;
    
    private Logger logger = Logger.getLogger(ShareDocumentLibraryFromTemplate.class);
    
    public void init() {
    	if (logger.isDebugEnabled()) logger.debug("Initializing rateable behaviors");

        // Create behaviors
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviors to node policies
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), TYPE_SITE, this.onCreateNode);
    }
    
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		if (logger.isDebugEnabled()) logger.debug("Inside onCreateNode for ShareDocumentLibraryFromTemplate");
		
		NodeRef siteFolder = childAssocRef.getChildRef();
		
		if (!nodeService.exists(siteFolder)) {
			logger.debug("Site folder doesn't exist yet");
			return;
		}
		
		//grab the site preset value
		String sitePreset = (String) nodeService.getProperty(siteFolder, PROP_SITE_PRESET);
		
		//see if there is a folder in the Space Templates folder of the same name
        String query = "+PATH:\"/app:company_home/app:dictionary/app:site_folder_templates/*\" +@cm\\:name:\"" + sitePreset + "\"";

		ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, query);

        // if nothing was found in the Site Folder Templates folder, try again with Space Templates folder
        if (rs.length() == 0) {
            query = "+PATH:\"/app:company_home/app:dictionary/app:space_templates/*\" +@cm\\:name:\"" + sitePreset + "\"";
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, query);
        }

		//if not, bail, there is nothing more to do
		if (rs.length() <= 0) {
			logger.debug("Found no space templates for: " + sitePreset);
			return;
		}

		NodeRef spaceTemplate = null;
		for (int i = 0; i < rs.length(); i++) {
			spaceTemplate = rs.getNodeRef(i);
			if (!nodeService.exists(spaceTemplate)) {
				spaceTemplate = null;
				continue;
			} else {
				//confirm that the space template's name is an exact match -- Issue #3
				String templateName = (String) nodeService.getProperty(spaceTemplate, ContentModel.PROP_NAME);
				if (!templateName.equals(sitePreset)) {
					logger.debug("Space template name is not an exact match: " + templateName);
					spaceTemplate = null;
					continue;
				} else {
					break;
				}
			}
		}

		if (spaceTemplate == null) {
			logger.debug("Space template doesn't exist");
			return;
		} else {
			logger.debug("Found space template: " + nodeService.getProperty(spaceTemplate, ContentModel.PROP_NAME));
		}
		
		// otherwise, create the documentLibrary folder
		String siteId = (String) nodeService.getProperty(siteFolder, ContentModel.PROP_NAME);
		logger.debug("Site ID: " + siteId);
		
		// use the site service to do this so that permissions get set correctly
		NodeRef documentLibrary = siteService.getContainer(siteId, DOCUMENT_LIBRARY);
		if (documentLibrary == null) {
			// create the document library container using the site service
			documentLibrary = siteService.createContainer(siteId, DOCUMENT_LIBRARY, null, null);
			if (documentLibrary == null) {
				logger.error("Document library could not be created for: " + siteId);
			}			
		}
		
		// now, for each child in the space template, do a copy to the documentLibrary		
		List<ChildAssociationRef> children = nodeService.getChildAssocs(spaceTemplate);
		for (ChildAssociationRef childRef : children) {
			// we only want contains associations
			if (childRef.getQName().equals(ContentModel.ASSOC_CONTAINS)) {
				continue;
			}
			NodeRef child = childRef.getChildRef();
			try {
				fileFolderService.copy(child, documentLibrary, null);
				logger.debug("Successfully copied a child node from the template");
			} catch (FileExistsException e) {
				logger.debug("The child node already exists in the document library.");
			} catch (FileNotFoundException e) {
				//can't find the space template, just bail
				logger.warn("Share site tried to use a space template, but the source space template could not be found.");
			}
		}		
	}

	public NodeService getNodeService() {
		return nodeService;
	}


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}


	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

}

