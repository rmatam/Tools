/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginTemplateHelper;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import freemarker.template.Configuration;

public class SiteAdminController extends FreemarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SiteAdminController.class);
    private static final String TEMPLATE_DEFAULT = "siteAdmin-main.ftl";
    private static final int REQUIRED_LOGIN_LEVEL = LoginStatusBean.EDITOR;
    
    @Override
	public String getTitle(String siteName) {
        return siteName + " Site Administration";
	}

    /* requiredLoginLevel() must be an instance method, else, due to the way sublcass
     * hiding works, when called from FreemarkerHttpServlet we will get its own method,
     * rather than the subclass method. To figure out whether to display links at the
     * page level, we need another, static method.
     */
    public static int staticRequiredLoginLevel() {
        return REQUIRED_LOGIN_LEVEL;
    }

    @Override
    protected int requiredLoginLevel() {
        return staticRequiredLoginLevel();
    }
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        // Note that we don't get here unless logged in at least at editor level, due
        // to requiresLoginLevel().
    	LoginStatusBean loginBean = LoginStatusBean.getBean(vreq);
    	
        Map<String, Object> body = new HashMap<String, Object>();        

        UrlBuilder urlBuilder = new UrlBuilder(vreq.getPortal());
        
        body.put("dataInput", getDataInputData(vreq));

        // rjy7 There is a risk that the login levels required to show the links will get out
        // of step with the levels required by the pages themselves. We should implement a 
        // mechanism similar to what's used on the front end to display links to Site Admin
        // and Revision Info iff the user has access to those pages.
        if (loginBean.isLoggedInAtLeast(LoginStatusBean.CURATOR)) {
            body.put("siteConfig", getSiteConfigurationData(vreq, urlBuilder));
            body.put("ontologyEditor", getOntologyEditorData(vreq, urlBuilder));
            
            if (loginBean.isLoggedInAtLeast(LoginStatusBean.DBA)) {
                body.put("dataTools", getDataToolsData(vreq, urlBuilder));
                
                // Only for DataStar. Should handle without needing a DataStar-specific version of this controller.
                //body.put("customReports", getCustomReportsData(vreq));
            }
        }
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
 
    }
    
    private Map<String, Object> getDataInputData(VitroRequest vreq) {
    
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("formAction", UrlBuilder.getUrl("/edit/editRequestDispatch.jsp"));
        
        WebappDaoFactory wadf = vreq.getFullWebappDaoFactory();
        
        // Create map for data input entry form options list
        List classGroups = wadf.getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals        
        Iterator classGroupIt = classGroups.iterator();
        LinkedHashMap<String, List> orderedClassGroups = new LinkedHashMap<String, List>(classGroups.size());
        while (classGroupIt.hasNext()) {
            VClassGroup group = (VClassGroup)classGroupIt.next();
            List classes = group.getVitroClassList();
            orderedClassGroups.put(group.getPublicName(),FormUtils.makeOptionListFromBeans(classes,"URI","PickListName",null,null,false));
        }
        
        map.put("groupedClassOptions", orderedClassGroups);
        return map;
    }
    
    private Map<String, Object> getSiteConfigurationData(VitroRequest vreq, UrlBuilder urlBuilder) {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, String> urls = new HashMap<String, String>();

        urls.put("tabs", urlBuilder.getPortalUrl("/listTabs"));
        
        if (LoginStatusBean.getBean(vreq).isLoggedInAtLeast(LoginStatusBean.DBA)) {                
            urls.put("users", urlBuilder.getPortalUrl("/listUsers"));
        }

        if (!vreq.getFullWebappDaoFactory().getPortalDao().isSinglePortal()) {
            urls.put("portals", urlBuilder.getPortalUrl("/listPortals"));
        }
 
        urls.put("siteInfo", urlBuilder.getPortalUrl("/editForm", new ParamMap("controller", "Portal", "id", String.valueOf(urlBuilder.getPortalId()))));  
        
        map.put("urls", urls);
        
        return map;
    }
    
    private Map<String, Object> getOntologyEditorData(VitroRequest vreq, UrlBuilder urlBuilder) {

        Map<String, Object> map = new HashMap<String, Object>();
 
        String pelletError = null;
        String pelletExplanation = null;
        Object plObj = getServletContext().getAttribute("pelletListener");
        if ( (plObj != null) && (plObj instanceof PelletListener) ) {
            PelletListener pelletListener = (PelletListener) plObj;
            if (!pelletListener.isConsistent()) {
                pelletError = "INCONSISTENT ONTOLOGY: reasoning halted.";
                pelletExplanation = pelletListener.getExplanation();
            } else if ( pelletListener.isInErrorState() ) {
                pelletError = "An error occurred during reasoning. Reasoning has been halted. See error log for details.";
            }
        }

        if (pelletError != null) {
            Map<String, String> pellet = new HashMap<String, String>();
            pellet.put("error", pelletError);
            if (pelletExplanation != null) {
                pellet.put("explanation", pelletExplanation);
            }
            map.put("pellet", pellet);
        }
                
        Map<String, String> urls = new HashMap<String, String>();
        
        urls.put("ontologies", urlBuilder.getPortalUrl("/listOntologies"));
        urls.put("classHierarchy", urlBuilder.getPortalUrl("/showClassHierarchy"));
        urls.put("classGroups", urlBuilder.getPortalUrl("/listGroups"));
        urls.put("dataPropertyHierarchy", urlBuilder.getPortalUrl("/showDataPropertyHierarchy"));
        urls.put("propertyGroups", urlBuilder.getPortalUrl("/listPropertyGroups"));            
        urls.put("objectPropertyHierarchy", urlBuilder.getPortalUrl("/showObjectPropertyHierarchy", new ParamMap("iffRoot", "true")));
        map.put("urls", urls);
        
        // RY Make sure this works for true, false, and undefined values of the param
        String verbose = vreq.getParameter("verbose");
        boolean verbosePropertyValue = "true".equals(verbose) ? true : false;
        vreq.getSession().setAttribute("verbosePropertyListing", verbosePropertyValue);
        
        Map<String, Object> verbosePropertyForm = new HashMap<String, Object>();
        verbosePropertyForm.put("verboseFieldValue", String.valueOf(!verbosePropertyValue)); // the form toggles the current value
        verbosePropertyForm.put("action", urlBuilder.getPortalUrl(Route.SITE_ADMIN));
        verbosePropertyForm.put("currentValue", verbosePropertyValue ? "on" : "off");
        verbosePropertyForm.put("newValue", verbosePropertyValue ? "off" : "on");
        map.put("verbosePropertyForm", verbosePropertyForm);
        
        return map;
    }

    private Map<String, Object> getDataToolsData(VitroRequest vreq, UrlBuilder urlBuilder) {

        Map<String, Object> map = new HashMap<String, Object>();
        
        Map<String, String> urls = new HashMap<String, String>();
        urls.put("ingest", urlBuilder.getUrl("/ingest"));
        urls.put("rdfData", urlBuilder.getPortalUrl("/uploadRDFForm"));
        urls.put("rdfExport", urlBuilder.getPortalUrl("/export"));
        urls.put("sparqlQuery", urlBuilder.getUrl("/admin/sparqlquery"));
        urls.put("sparqlQueryBuilder", urlBuilder.getUrl("/admin/sparqlquerybuilder"));
        map.put("urls", urls);
        
        return map;
    }

}