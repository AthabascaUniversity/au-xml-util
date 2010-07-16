package ca.athabascau.apas.xml;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tests mapping collections to XML.
 * <p/>
 * Created :  Jul 15, 2010 3:23:54 PM MST
 * <p/>
 * Modified : $Date$
 * <p/>
 * Revision : $Revision$
 *
 * @author trenta
 */
public class MapTest extends TestCase
{
    /**
     * Tests to make sure that {@link XMLUtil#mapToXML(String, Object)} is
     * working with Maps.  It produces and tests the following XML
     *
     * <pre>
&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
&lt;root&gt;
  &lt;element2&gt;value2&lt;/element2&gt;
  &lt;element4&gt;
    &lt;subelement2&gt;value2&lt;/subelement2&gt;
    &lt;subelement1&gt;value1&lt;/subelement1&gt;
    &lt;innermost&gt;
      &lt;innerelement1&gt;innervalue1&lt;/innerelement1&gt;
      &lt;innerelement2&gt;innervalue2&lt;/innerelement2&gt;
    &lt;/innermost&gt;
  &lt;/element4&gt;
  &lt;element3&gt;value3&lt;/element3&gt;
  &lt;element1&gt;value1&lt;/element1&gt;
&lt;/root&gt;
     </pre>
     *
     * @throws TransformerException         if a transformer error occurs
     * @throws ParserConfigurationException if a configuration error occurs
     * @throws IOException                  if an io error occurs
     * @throws SAXException                 if an xml parsing exception occurs
     */
    public void testSimpleMapToXML()
        throws TransformerException, ParserConfigurationException, IOException,
        SAXException
    {
        final Map parameters;
        final String requestXML;

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
        final String xml = XMLUtil.mapToXML("root", parameters);
        System.out.println("xml: " + xml);
        final Document doc = XMLUtil.stringToDocument(xml);
        final NodeList elList = doc.getElementsByTagName("innermost");
        assertNotNull("Should be one, and at least one innermost element",
            elList);
        assertEquals("Should be one, and at least one innermost element",
            elList.getLength(), 1);
        final Node element = elList.item(0);
        assertEquals("innermost should have two children",
            element.getChildNodes().getLength(), 2);
    }

    /**
     * Produces and tests the following XML...
     *
     * <pre>
     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
     &lt;root&gt;
       &lt;listelement&gt;1&lt;/listelement&gt;
       &lt;listelement&gt;2&lt;/listelement&gt;
       &lt;listelement&gt;3&lt;/listelement&gt;
       &lt;listelement&gt;4&lt;/listelement&gt;
       &lt;listelement&gt;
         &lt;sub1&gt;
           &lt;anothersub1&gt;value1&lt;/anothersub1&gt;
           &lt;anothersub2&gt;value2&lt;/anothersub2&gt;
           &lt;anothersub3&gt;value3&lt;/anothersub3&gt;
           &lt;anothersub4&gt;value4&lt;/anothersub4&gt;
         &lt;/sub1&gt;
         &lt;sub2&gt;value2&lt;/sub2&gt;
         &lt;sub3&gt;value3&lt;/sub3&gt;
         &lt;sub4&gt;value4&lt;/sub4&gt;
       &lt;/listelement&gt;
     &lt;/root&gt;
     </pre>

     *
     * @throws TransformerException         if a transformer error occurs
     * @throws ParserConfigurationException if a configuration error occurs
     * @throws IOException                  if an io error occurs
     * @throws SAXException                 if an xml parsing exception occurs
     */
    public void testMapListToXML()
        throws TransformerException, ParserConfigurationException, IOException,
        SAXException
    {
        final Map parameters;

        final ArrayList subElements;
        final Map subMap;
        final Map subMap2;
        parameters = new HashMap();
        subElements = new ArrayList();
        subMap = new TreeMap();
        subMap2 = new TreeMap();
        subElements.add("1");
        subElements.add("2");
        subElements.add("3");
        subElements.add("4");
        subMap2.put("anothersub1", "value1");
        subMap2.put("anothersub2", "value2");
        subMap2.put("anothersub3", "value3");
        subMap2.put("anothersub4", "value4");
        subMap.put("sub1", subMap2);
        subMap.put("sub2", "value2");
        subMap.put("sub3", "value3");
        subMap.put("sub4", "value4");
        subElements.add(subMap);
        parameters.put("listelement", subElements);

        final String xml = XMLUtil.mapToXML("root", parameters);
        System.out.println("xml: " + xml);
        final Document doc = XMLUtil.stringToDocument(xml);
        final NodeList elList = doc.getElementsByTagName("listelement");
        assertNotNull("The list of listelement elements should not be empty",
            elList);
        assertEquals("Should be five listelement elements", 5,
            elList.getLength());
        final Node element = elList.item(4);
        assertEquals("Listelement should have four children",
            4, element.getChildNodes().getLength());
        Node sub1 = element.getFirstChild();
        assertNotNull("The list of listelement elements should not be empty",
            sub1);
        assertEquals("sub1 should have four children", 4,
            sub1.getChildNodes().getLength());
    }

    public void testBadParameters()
        throws ParserConfigurationException, TransformerException
    {
        final Map parameters;

        final ArrayList subElements;
        parameters = new HashMap();
        subElements = new ArrayList();
        subElements.add(new ArrayList());
        parameters.put("listelement", subElements);

        try
        {
            final String xml = XMLUtil.mapToXML("root", parameters);
            System.out.println("xml: " + xml);
            fail("Should not be able to pass a list with a list");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
}
