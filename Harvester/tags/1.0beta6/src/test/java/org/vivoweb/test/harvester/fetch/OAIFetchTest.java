/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.fetch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * @author Dale Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */

public class OAIFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(OAIFetchTest.class);
	/** */
	private RecordHandler rh;
	/** */
	private File configFile;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(OAIFetchTest.class);
		this.configFile = File.createTempFile("oaiRHConfig", "xml");
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.configFile));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<RecordHandler type=\"org.vivoweb.harvester.util.repo.JDBCRecordHandler\">\n	<Param name=\"dbClass\">org.h2.Driver</Param>\n	<Param name=\"dbUrl\">jdbc:h2:mem:TestOAIFetchRH</Param>\n	<Param name=\"dbUser\">sa</Param>\n	<Param name=\"dbPass\"></Param>\n	<Param name=\"dbTable\">recordTable</Param>\n	<Param name=\"dataFieldName\">dataField</Param>\n</RecordHandler>");
		bw.close();
		this.rh = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.rh != null) {
			this.rh.close();
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.fetch.OAIFetch#main(java.lang.String[]) main(String... args)}.
	 */
	public final void testOAIFetchMain() {
		log.info("BEGIN testOAIFetchMain");
		try {
			this.rh = RecordHandler.parseConfig(this.configFile.getAbsolutePath());
			// FIXME cah: fix this later
			// OAIFetch.main(new String[]{"-u", "archivesic.ccsd.cnrs.fr/oai/oai.php", "-s", "2000-01-01", "-e",
			// "2002-12-12", "-o", this.configFile.getAbsolutePath()});
			// assertTrue(this.rh.iterator().hasNext());
			// DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// for(Record r : this.rh) {
			// log.info("=====================================");
			// log.info(r.getData());
			// log.info("=====================================");
			// Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
			// Element elem = doc.getDocumentElement();
			// traverseNodes(elem.getChildNodes());
			// }
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		}
		log.info("END testOAIFetchMain");
	}
	
	// /**
	// * @param nodeList the nodes
	// */
	// private void traverseNodes(NodeList nodeList) {
	// for(int x = 0; x < nodeList.getLength(); x++) {
	// Node child = nodeList.item(x);
	// String name = child.getNodeName();
	// if(!name.contains("#text")) {
	// log.info(name);
	// traverseNodes(child.getChildNodes());
	// }
	// }
	// }
}