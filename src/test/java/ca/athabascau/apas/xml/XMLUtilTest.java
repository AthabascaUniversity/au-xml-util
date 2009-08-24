package ca.athabascau.apas.xml;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.ParseException;

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
    /**
     * Tests xsDate formatting methods.
     *
     * @throws ParseException if an error occurs parsing the dat format.
     */
    public void testXsDateFormatting() throws ParseException
    {
        Calendar initialCal = new GregorianCalendar(2009, 11, 12);
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
        catch(ParseException e)
        {
            fail("should not throw a parse exception, parameter was null");
        }
    }
}
