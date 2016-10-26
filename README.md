share-site-space-templates
==========================

Adds the ability to create a default set of folders to an Alfresco Share site by leveraging Space Templates

For example, the out-of-the-box Share site "type" is shown in English as "Collaboration Site". Its preset ID is "site-dashboard".

So after installing this AMP in your Alfresco WAR, you can create a folder under Data Dictionary/Space Templates called "site-dashboard". Then anything you put in that folder will be copied into the document library of new "Collaboration Site" sites.

Alternatively, you can store your Share Site Space Templates in a Data Dictionary folder called Site Folder Templates. That will keep them separate from traditional Space Templates.

If you modify your site presets to add additional types of Share sites, you can create additional space templates for each type of site.

Maven
-----
Add the dependencies and overlays to the POM file of your WAR project.

For the repository tier, in a project created with the all-in-one archetype, edit repo/pom.xml:


    <dependencies>
      ...
      <dependency>
          <groupId>com.metaversant</groupId>
          <artifactId>share-site-space-templates-repo</artifactId>
          <version>1.1.2</version>
          <type>amp</type>
      </dependency>
      ...
    </dependencies>

    <overlays>
      ...
      <overlay>
          <groupId>com.metaversant</groupId>
          <artifactId>share-site-space-templates-repo</artifactId>
          <type>amp</type>
      </overlay>
      ...
    </overlays>

Manual Installation
-------------------
Use `mvn install` to create the AMP.

You can then install the AMP as you normally would using the MMT.

Alternatively, you can use the Maven plug-in to install the AMP by running `mvn alfresco:install -Dmaven.alfresco.warLocation=$TOMCAT_HOME/webapps/alfresco` if you are running your Alfresco WAR expanded, or specify the WAR if you are running unexpanded.

No further config or setup is necessary.
