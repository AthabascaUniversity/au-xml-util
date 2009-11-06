package ca.athabascau.apas.xml;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
}
