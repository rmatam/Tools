/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.visualization.constants;

import java.util.Calendar;

/**
 * This contains the constants related to all the value objects.
 * @author cdtank
 */
public class VOConstants {
	
	public static final String DEFAULT_PUBLICATION_YEAR = "Unknown";
	public static final String DEFAULT_GRANT_YEAR = "Unknown";
	
	/*
	 * Employee related constants 
	 * */
	public static enum EmployeeType {
		ACADEMIC_FACULTY_EMPLOYEE, ACADEMIC_STAFF_EMPLOYEE
	} 
	
	public static final int NUM_CHARS_IN_YEAR_FORMAT = 4;
	public static final int MINIMUM_PUBLICATION_YEAR = 1800;
	public static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

	
}