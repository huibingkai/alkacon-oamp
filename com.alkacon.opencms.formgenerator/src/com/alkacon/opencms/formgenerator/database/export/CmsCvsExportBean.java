/*
 * File   : $Source: /alkacon/cvs/alkacon/com.alkacon.opencms.formgenerator/src/com/alkacon/opencms/formgenerator/database/export/CmsCvsExportBean.java,v $
 * Date   : $Date: 2007/12/21 14:34:01 $
 * Version: $Revision: 1.1 $
 *
 * This file is part of the Alkacon OpenCms Add-On Module Package
 *
 * Copyright (c) 2007 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * The Alkacon OpenCms Add-On Module Package is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Alkacon OpenCms Add-On Module Package is distributed 
 * in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Alkacon OpenCms Add-On Module Package.  
 * If not, see http://www.gnu.org/licenses/.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com.
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org.
 */
package com.alkacon.opencms.formgenerator.database.export;

import com.alkacon.opencms.formgenerator.CmsFormHandler;
import com.alkacon.opencms.formgenerator.database.CmsFormDataAccess;
import com.alkacon.opencms.formgenerator.database.CmsFormDataBean;
import com.alkacon.opencms.formgenerator.database.I_CmsFormDataAccess;

import java.sql.SQLException;
import java.text.Collator;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Bean that supports the data export.<p>
 * 
 * Works together with 
 * /system/modules/com.alkacon.opencms.formgenerator/elements/datadownload.jsp.<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.0.4
 *
 */
public class CmsCvsExportBean {
    
    /** Request parameter for the start time of the data to export. */
    public static final String PARAM_EXPORT_DATA_TIME_END = "endtime";

    /** Request parameter for the start time of the data to export. */
    public static final String PARAM_EXPORT_DATA_TIME_START = "starttime";

    /** The end time for data sets to export. */
    private Date m_endTime;

    /**
     * The form that was used to input the data to export. 
     */
    private CmsFormHandler m_formHandler;

    /** The start time for data sets to export. */
    private Date m_startTime;

    /**
     * Creates an instance based upon data that was entered with the given form.<p> 
     * 
     * @param formHandler the origin of the data 
     */
    public CmsCvsExportBean(final CmsFormHandler formHandler) {

        m_formHandler = formHandler;
        this.m_startTime = new Date(0);
        this.m_endTime = new Date(Long.MAX_VALUE);
    }

    /** 
     * Escapes CSV values for excel.<p> 
     * 
     * @param value the value to escape 
     * 
     * @return the escaped excel value. 
     */
    private String escapeExcelCsv(final String value) {

        String result = value;
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"");
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            // escape double quote escape delimiter within value: 
            if ('"' == chars[i]) {
                buffer.append("\"");
            }
            buffer.append(chars[i]);
        }
        buffer.append("\"");
        result = buffer.toString();
        return result;
    }

    /** The default value delimiter for CSV files in Excel. */
    public static final char EXCEL_DEFAULT_CSV_DELMITER = ';';

    /**
     * Returns the csv export file content.<p> 
     * 
     * @return the csv export file content
     * 
     * @throws SQLException if sth goes wrong 
     */
    public String exportData() throws SQLException {

        /*
         * TODO: Access the CmsForm (or CmsFormHandler) and put out all 
         * fields in the exact order - put fields that do not exist any longer 
         * to the back (note: readAllFormFieldNames is required for the old values). 
         */

        StringBuffer result = new StringBuffer();
        I_CmsFormDataAccess db = this.getFormDataAccess();
        List columnNames = db.readAllFormFieldNames(this.getForm(), this.getStartTime(), this.getEndTime());
        Collections.sort(columnNames, Collator.getInstance(m_formHandler.getRequestContext().getLocale()));

        List dataEntries = db.readFormData(this.getForm(), this.getStartTime(), this.getEndTime());
        CmsFormDataBean row;
        String columnName;
        Iterator itRows;
        Iterator itColumns;
        itRows = dataEntries.iterator();
        String value;
        // loop 1 - write the headers:
        itColumns = columnNames.iterator();
        while (itColumns.hasNext()) {
            columnName = (String)itColumns.next();
            columnName = escapeExcelCsv(columnName);
            result.append(columnName);
            if (itColumns.hasNext()) {
                result.append(EXCEL_DEFAULT_CSV_DELMITER);
            }
        }
        result.append("\r\n");
        // loop 2 - write the data:
        while (itRows.hasNext()) {
            row = (CmsFormDataBean)itRows.next();
            // create an entry for each column, even if some rows (data sets) 
            // do not contain the field value because it was 
            // a) not entered 
            // b) the form was changed in structure over time 
            // c) developer errors,  hw /sw problems... 
            itColumns = columnNames.iterator();
            while (itColumns.hasNext()) {
                columnName = (String)itColumns.next();
                value = (String)row.getFieldEntry(columnName).getValue();
                value = this.escapeExcelCsv(value);
                result.append(value);
                if (itColumns.hasNext()) {
                    result.append(EXCEL_DEFAULT_CSV_DELMITER);
                }
            }
            result.append("\r\n");
        }
        return result.toString();
    }

    /**
     * Returns the endTime.<p>
     *
     * @return the endTime
     */
    public Date getEndTime() {

        return m_endTime;
    }

    /**
     * Returns the form handler.<p>
     *
     * @return the form handler
     */
    public CmsFormHandler getForm() {

        return m_formHandler;
    }

    /**
     * Returns the access layer to the form data in the database.<p> 
     * 
     * @return the access layer to the form data in the database
     */
    private I_CmsFormDataAccess getFormDataAccess() {

        return new CmsFormDataAccess();
    }

    /**
     * Returns the startTime.<p>
     *
     * @return the startTime
     */
    public Date getStartTime() {

        return m_startTime;
    }

    /**
     * Sets the endTime.<p>
     *
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {

        m_endTime = endTime;
    }

    /**
     * Sets the startTime.<p>
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {

        m_startTime = startTime;
    }

}