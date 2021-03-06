/**
 * This file is part of the au-xml-util package
 *
 * Copyright Trenton D. Adams <trenton daught d daught adams at gmail daught ca>
 * 
 * au-xml-util is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * au-xml-util is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with au-xml-util.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * See the COPYING file for more information.
 */
package ca.athabascau.apas.xml;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * Created :  Aug 24, 2009 3:58:52 PM MST
 * <p/>
 * Modified : $Date$
 * <p/>
 * Revision : $Revision$
 *
 * @author trenta
 */
public class XMLUtilTest extends TestCase
{
    private static final Logger logger = Logger.getLogger(XMLUtilTest.class);

    /**
     * Tests xsDate formatting methods.
     *
     * @throws ParseException if an error occurs parsing the dat format.
     */
    public void testXsDateFormatting() throws ParseException
    {
        final Calendar initialCal = new GregorianCalendar(2009, 11, 12);
        final String xsDate;
        xsDate = XMLUtil.xsDateFormat(initialCal);
        assertEquals("Dates are different", xsDate, "2009-12-12");

        assertEquals("Calendars are not equal", initialCal,
            XMLUtil.xsDateToCalendar(xsDate));

        try
        {
            assertEquals("Calendars are not equal", initialCal,
                XMLUtil.xsDateToCalendar("blah" + xsDate));
            fail("Date parsing should fail");
        }
        catch (ParseException ignored)
        {
        }

        try
        {
            assertNull("should return a null Calendar",
                XMLUtil.xsDateToCalendar(null));
        }
        catch (ParseException e)
        {
            fail("should not throw a parse exception, parameter was null");
        }
    }

/*    public void testMapToXML()
        throws TransformerException, ParserConfigurationException
    {
        final Map parameters;
        final Map subElements;
        final Map innerElements;
        parameters = new HashMap();
        subElements = new HashMap();
        innerElements = new HashMap();

        parameters.put("element1", "value1");
        parameters.put("element2", "value2");
        parameters.put("element3", "value3");
        parameters.put("element4", subElements);
        subElements.put("subelement1", "value1");
        subElements.put("subelement2", "value2");
        subElements.put("innermost", innerElements);
        innerElements.put("innerelement1", "innervalue1");
        innerElements.put("innerelement2", "innervalue2");
        final String xml;
        xml = XMLUtil.mapToXML("root", parameters);
        assertTrue("Not an expected XML document: " + xml,
            xml.matches("element4.*innerelement1"));
    }*/

    public void testRemoteXmlStylesheet()
    {
        final String transcriptHTML;
        try
        {
            final Document transcriptDOC;
            transcriptDOC = XMLUtil.loadXMLFrom(
                XMLUtil.class.getResourceAsStream("/transcript-test.xml"));
            transcriptHTML =
                XMLUtil.xslTransformation(transcriptDOC, null, null);
            assertTrue("Not an HTML document",
                transcriptHTML.toUpperCase().lastIndexOf("<HTML") != -1);
        }
        catch (Throwable e)
        {
            logger.error("testXmlStylesheet failed", e);
            fail(e.getMessage());
        }
    }

    public void testXsDateTimeToCalendar() throws ParseException
    {
        Calendar xsDateTime = XMLUtil.xsDateTimeToCalendar(
            "2010-10-05T18:14:14-06:00");
        assertEquals("year", 2010, xsDateTime.get(Calendar.YEAR));
        assertEquals("month", 10, xsDateTime.get(Calendar.MONTH) + 1);
        assertEquals("day", 05, xsDateTime.get(Calendar.DAY_OF_MONTH));
        assertEquals("hour", 18, xsDateTime.get(Calendar.HOUR_OF_DAY));
        assertEquals("minute", 14, xsDateTime.get(Calendar.MINUTE));
        assertEquals("second", 14, xsDateTime.get(Calendar.SECOND));
        assertEquals("timezone offset", -6, TimeZone.getDefault().getOffset(
            xsDateTime.getTime().getTime()) / (1000 * 60 * 60));

        // verify timezone correction to Canada/Mountain
        xsDateTime = XMLUtil.xsDateTimeToCalendar(
            "2010-12-05T18:14:14-06:00");
        assertEquals("year", 2010, xsDateTime.get(Calendar.YEAR));
        assertEquals("month", 12, xsDateTime.get(Calendar.MONTH) + 1);
        assertEquals("day", 05, xsDateTime.get(Calendar.DAY_OF_MONTH));
        assertEquals("hour", 17, xsDateTime.get(Calendar.HOUR_OF_DAY));
        assertEquals("minute", 14, xsDateTime.get(Calendar.MINUTE));
        assertEquals("second", 14, xsDateTime.get(Calendar.SECOND));
        assertEquals("timezone offset", -7, TimeZone.getDefault().getOffset(
            xsDateTime.getTime().getTime()) / (1000 * 60 * 60));
    }
}
