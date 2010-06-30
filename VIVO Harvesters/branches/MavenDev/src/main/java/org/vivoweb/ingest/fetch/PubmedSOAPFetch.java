/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.fetch;

import gov.nih.nlm.ncbi.www.soap.eutils.*;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet_type0;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Module for fetching PubMed Citations using the PubMed SOAP Interface<br>
 * Based on the example code available at the PubMed Website.
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 * @author Dale R. Scheppler dscheppler@ctrip.ufl.edu
 * @author Christopher Haines cah@ctrip.ufl.edu
 */
public class PubmedSOAPFetch implements Harvestor
{
	private static Log log = LogFactory.getLog(PubmedSOAPFetch.class);							//Initialize the logger
	private String strEmailAddress;
	private String strToolLocation;
	private OutputStreamWriter xmlWriter;
	
	/***
	 * Primary method for running a PubMed SOAP Fetch. The email address and location of the<br>
	 * person responsible for this install of the program is required by PubMed guidelines so<br>
	 * the person can be contacted if there is a problem, such as sending too many queries<br>
	 * too quickly. 
	 * @author Dale Scheppler
	 * @author Chris Haines
	 * @param strEmail - Contact email address of the person responsible for this install of the PubMed Harvester
	 * @param strToolLoc - Location of the current tool installation (Eg: UF or Cornell or Pensyltucky U.
	 * @param osOutStream - The output stream for the method.
	 */
	public PubmedSOAPFetch(String strEmail, String strToolLoc, OutputStream osOutStream)
	{
		this.strEmailAddress = strEmail; // NIH Will email this person if there is a problem
		this.strToolLocation = strToolLoc; // This provides further information to NIH
		try {
			// Writer to the stream we're getting from the controller.
			this.xmlWriter = new OutputStreamWriter(osOutStream, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			log.error("",e);
		}
	}
	
	/**
	 * This method returns a query for the PubMed Fetch that will request all records<br>
	 * from the year 1 to the year 8000, essentially, all records.
	 * @return A string consisting of "1:8000[dp]".
	 */
	public String fetchAll()
	{
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of method "PubmedSOAPFetch.fetchAll" to Private
		//FIXME This code was marked as may cause compile errors by UCDetector.
		return "1:8000[dp]";
	}
	
	/**
	 * Takes in a string that consists of the latter half of an email address, including the @<br>
	 * such as "@ufl.edu" and creates a query string to locate all records associated with<br>
	 * that address.
	 * @param strAffiliation - The latter half of an email address, such as "@ufl.edu"
	 * @return A query string that will allow a search by affiliation.
	 */
	public String queryByAffiliation(String strAffiliation)
	{
		return strAffiliation+"[ad]";
	}

	/**
	 * Performs an ESearch against PubMed database using a search term.<br>
	 * The search terms are generated by other methods in this class or <br>
	 * can be passed in directly.
	 * 
	 * @param term - The search term, in string format.
	 * @param maxNumRecords - Maximum number of records to pull, set currently by Fetch.throttle.
	 * @return List<Integer> of ids found in the search result
	 * @author Chris Haines
	 * @author Dale Scheppler
	 */
	public List<Integer> ESearch(String term, Integer maxNumRecords)
	{
		// define the list to hold our ids
		ArrayList<Integer> idList = new ArrayList<Integer>();
		try
		{
			// create service connection
			EUtilsServiceStub service = new EUtilsServiceStub();
			// create a new search
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			// set search to pubmed database
			req.setDb("pubmed");
			// set search term
			req.setTerm(term);
			// set max number of records to return from search
			req.setRetMax(maxNumRecords.toString());
			// run the search and get result set
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			log.trace("Fetching a total of " + res.getIdList().getId().length + " records.");
			// for each id in the list of ids in the search results
			for (String id : res.getIdList().getId())
			{
				try
				{
					// put it in our List
					idList.add(new Integer(id));
				}
				// just in case there is a non-number in the ID list (should not happen)
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
			log.trace(idList.size()+" records found");
		}
		catch (AxisFault f)
		{
			log.error("Failed to initialize service connection");
			f.printStackTrace();
		}
		catch (RemoteException e)
		{
			log.error("Failed to run the search");
			e.printStackTrace();
		}
		// return the list of ids
		return idList;
	}
	
	/**
	 * FIXME Chris could you please document this? I can't tell what it's doing at a glance.<br>
	 * It looks like it does something related to ESearch but I'm not seeing it.
	 * @param term - The search term generated by other methods in this class, in string format.
	 * @param maxNumRecords - The maximum number of records to fetch.
	 * @return An array of strings
	 */
	public String[] ESearchEnv(String term, Integer maxNumRecords) {
		return ESearchEnv(term, maxNumRecords, 0);
	}
	
	/**
	 * Performs an ESearch against PubMed database using a search term and a web environment/query key.
	 * 
	 * @param term - The search term as a string generated by other methods in this class, or passed in manually.
	 * @param maxNumRecords - Maximum number of records to pull, set currently by Fetch.throttle.
	 * @param retStart - FIXME this was marked as todo, what is it todo?
	 * @return String[] = {WebEnv, QueryKey, idListLength} FIXME better documentation needed.
	 * @author Chris Haines
	 * @author Dale Scheppler
	 */
	public String[] ESearchEnv(String term, Integer maxNumRecords, Integer retStart)
	{
		String[] env = new String[3];
		try
		{
			// create service connection
			EUtilsServiceStub service = new EUtilsServiceStub();
			// create a new search
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			// set search to pubmed database
			req.setDb("pubmed");
			// set search term
			req.setTerm(term);
			// set max number of records to return from search
			req.setRetMax(maxNumRecords.toString());
			// set number to start at
			req.setRetStart(retStart.toString());
			// save this search so we can use the returned set
			req.setUsehistory("y");
			// run the search and get result set
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			log.trace("Query resulted in a total of " + res.getIdList().getId().length + " records.");
			// save the environment data
			env[0] = res.getWebEnv();
			env[1] = res.getQueryKey();
			env[2] = ""+res.getIdList().getId().length;
		}
		catch (RemoteException e)
		{
			log.error("PubMedSOAPFetch ESearchEnv failed with error: ",e);
		}
		return env;
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env =  = {WebEnv, QueryKey, idListLength}
	 * @throws IllegalArgumentException 
	 * FIXME Also needs better documentation
	 * @author Chris Haines
	 */
	public void fetchPubMedEnv(String[] env) throws IllegalArgumentException {
		if(env.length != 3) {
			throw new IllegalArgumentException("Invalid WebEnv, QueryKey, and idListLength");
		}
		fetchPubMedEnv(env[0], env[1], "0", env[2]);
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env = {WebEnv, QueryKey, [idListLength]}
	 * @param start = String of record number to start at 
	 * @param numRecords = String of number of records to pull
	 * @throws IllegalArgumentException 
	 */
	public void fetchPubMedEnv(String[] env, String start, String numRecords) throws IllegalArgumentException {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		if(!(env.length == 2 || env.length == 3)) {
			throw new IllegalArgumentException("Invalid WebEnv and QueryKey");
		}
		fetchPubMedEnv(env[0], env[1], start, numRecords);
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param WebEnv
	 * @param QueryKey
	 * @param intStart 
	 * @param maxRecords 
	 */
	public void fetchPubMedEnv(String WebEnv, String QueryKey, String intStart, String maxRecords) {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of method "PubmedSOAPFetch.fetchPubMedEnv" to Private
		//FIXME This code was marked as may cause compile errors by UCDetector.
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		req.setQuery_key(QueryKey);
		req.setWebEnv(WebEnv);
		req.setEmail(this.strEmailAddress);
		req.setTool(this.strToolLocation);
		req.setRetstart(intStart);
		req.setRetmax(maxRecords);
		log.trace("Fetching records from search");
		try {
			serializeFetchRequest(req);
		}catch(RemoteException e) {
			log.error("Could not run search",e);
		}
	}
	
	/**
	 * This method takes in a range of PMIDs and returns Query string to get all the ids
	 * 
	 * @param ids
	 *            Range of PMID you want to pull, in list form
	 * @return 
	 */
	public String queryPubMedIDs(List<Integer> ids) {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		StringBuilder strPMID = new StringBuilder();
		for(int id = 0; id < ids.size(); id++ ) {
			if(id != 0) {
				strPMID.append(",");
			}
			strPMID.append(ids.get(id));
		}
		return strPMID.toString()+"[uid]";
	}
	/**
	 * FIXME What in the world is this doing? There are no comments.
	 * @param req
	 * @throws RemoteException
	 */
	private void serializeFetchRequest(EFetchPubmedServiceStub.EFetchRequest req) throws RemoteException {
		ByteArrayOutputStream buffer=new ByteArrayOutputStream();
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		EFetchResult result = service.run_eFetch(req);
		PubmedArticleSet_type0 articleSet = result.getPubmedArticleSet();
		XMLStreamWriter writer;
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
			MTOMAwareXMLSerializer serial = new MTOMAwareXMLSerializer(writer);
			log.trace("Writing to output");
			articleSet.serialize(new QName("RemoveMe"), null, serial);
			serial.flush();
			log.trace("Writing complete");
//			log.trace("buffer size: "+buffer.size());
			String iString = buffer.toString("UTF-8");
			sanitizeXML(iString);
		} catch(XMLStreamException e) {
			log.error("Unable to write to output",e);
		} catch(UnsupportedEncodingException e) {
			log.error("Cannot get xml from buffer",e);
		}
	}
	
	/**
	 * This method takes in a range of PMIDs and returns MedLine XML to the main
	 * method as an outputstream.
	 * 
	 * @param id
	 *            PMID you want to pull
	 * @return 
	 */
	public String queryPubMedID(int id) {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		return id+"[uid]";
	}
	
	/**
	 * 
	 * @param intStartRecord
	 * @param intStopRecord
	 * @return 
	 */
	public String queryByRange(int intStartRecord, int intStopRecord)
	{
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		return intStartRecord+":"+intStopRecord+"[uid]";
	}
	
	/**
	 * TODO
	 * @param intStartMonth
	 * @param intStartDay
	 * @param intStartYear
	 * @param intStopMonth
	 * @param intStopDay
	 * @param intStopYear
	 * @return 
	 */
	public String queryAllByDateRange(int intStartMonth, int intStartDay, int intStartYear, int intStopMonth, int intStopDay, int intStopYear)
	{
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		return intStartYear+"/"+intStartMonth+"/"+intStartDay+"[PDAT]:"+intStopYear+"/"+intStopMonth+"/"+intStopDay+"[PDAT]";		
	}
	
	/**
	 * 
	 * @param intLastRunMonth
	 * @param intLastRunDay
	 * @param intLastRunYear 
	 * @return String query to fetch all from given date
	 */
	public String queryAllFromLastFetch(int intLastRunMonth, int intLastRunDay, int intLastRunYear)
	{
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		return intLastRunYear+"/"+intLastRunMonth+"/"+intLastRunDay+"[PDAT]:8000[PDAT]";
	}
	
	/**
	 * This function simply checks to see what is the highest PubMed article PMID at the time it is called.<br>
	 * The pubmed website might have 2-5 more records past what this one pulls<br>
	 * But this function pulls them up to what they have indexed.<br>
	 * So it's as good a "Highest number" as we're going to get.<br>
	 * 
	 * @return Returns an integer of the highest PMID at the time it is run
	 * @author Dale Scheppler
	 */
	public int getHighestRecordNumber()
	{
		Calendar gcToday = Calendar.getInstance();
		int intYear = gcToday.get(Calendar.YEAR);
		int intMonth = gcToday.get(Calendar.MONTH);
		int intDay = gcToday.get(Calendar.DATE);
//		List<Integer> lstResult = ESearchEnv("\""+intYear+"/"+intMonth+"/"+intDay+"\""+"[PDAT] : \""+(intYear + 5)+"/"+12+"/"+31+"\"[PDAT]", 1);
		List<Integer> lstResult = ESearch("\""+intYear+"/"+intMonth+"/"+intDay+"\""+"[PDAT] : \""+(intYear + 5)+"/"+12+"/"+31+"\"[PDAT]", 1);
		return lstResult.get(0);
	}
	
	/**
	 * Sanitize Method<br>
	 * Adds the dtd and xml code to the top of the xml file and removes the extraneous<br>
	 * xml namespace attributes.  This function is slated for deprecation during milestone 2<br>
	 * 
	 * @param strInput - The XML Stream to Sanitize.
	 * @throws IOException In case we run into problems doing this
	 * @author Chris Haines
	 * @author Stephen Williams
	 */
	private void sanitizeXML(String strInput) {
		log.trace("Sanitizing Output");
		
		//System Messages
//		log.trace("=================================\n=======================================================================\n=======================================================================\n=======================================================================");
//		log.trace(s);
//		log.trace("+++++++++++++++++++++++++++++++++\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle");
		log.trace("XML File Length - Pre Sanitize: " + strInput.length());
		log.trace("XML File Length - Post Sanitze: " + newS.length());
		try {
			this.xmlWriter.write(newS);
			//file close statements.  Warning, not closing the file will leave incomplete xml files and break the translate method
			this.xmlWriter.write("\n");
			this.xmlWriter.flush();
		} catch(IOException e) {
			log.error("Unable to write XML to file.",e);
		}
//		log.trace(newS);
//		log.trace("---------------------------------\n-----------------------------------------------------------------------\n-----------------------------------------------------------------------\n-----------------------------------------------------------------------");
		log.trace("Sanitization Complete");
	}
	
	/**
	 * This method adds the header to the XML stream.
	 * @throws IOException
	 */
	public void beginXML() throws IOException {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of method "PubmedSOAPFetch.BeginXML" to Private
		//FIXME This code was marked as may cause compile errors by UCDetector.
		this.xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		this.xmlWriter.write("<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n");
		this.xmlWriter.write("<PubmedArticleSet>\n");
		this.xmlWriter.flush();
	}
	
	/**
	 * This method adds the footer to the XML stream.
	 * @throws IOException
	 */
	public void endXML() throws IOException {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of method "PubmedSOAPFetch.endXML" to Private
		//FIXME This code was marked as may cause compile errors by UCDetector.
		this.xmlWriter.flush();
		this.xmlWriter.write("</PubmedArticleSet>");
		this.xmlWriter.flush();
		this.xmlWriter.close();
	}
	
	/**
	 * Executes the fetch
	 * 
	 * FIXME eventually Fetch should be initialized with parameters such that it know which of the fetches to run and all
	 * -- that needs to be called is execute()
	 */
	public void execute()
	{
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		log.info("Fetch Begin");
		//xml write functions, take in a stream pass it to a writer
		//Header lines for XML files from pubmed
		try {
			beginXML();
			this.fetchAll();
			endXML();
		} catch(IOException e) {
			log.error("",e);
		}
//		this.fetchByAffiliation("ufl.edu", 20);
		log.info("Fetch End");
		// TODO throttling should be done as part of the queries maybe? the current throttle does not work with the idea of
		// -- WebEnv/QueryKey fetching... Will need to research how that will work
	}
	
}