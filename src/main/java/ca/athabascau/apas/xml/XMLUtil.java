package ca.athabascau.apas.xml;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provides some basic XML utility methods.
 * <p/>
 * Created :  Aug 18, 2009 3:53:36 PM MST
 * <p/>
 * Modified : $Date$
 * <p/>
 * Revision : $Revision$
 *
 * @author trenta
 */
public class XMLUtil
{
    private static final Logger logger = Logger.getLogger(XMLUtil.class);

    static final int BUFFER_CAPACITY = 50000;

    public static void main(final String[] args)
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
        System.out.println(XMLUtil.mapToXML("root", parameters));
    }

    /**
     * Example for retrieving the APAS institutions list
     *
     * @param xml the string representation of the XML
     *
     * @return the Document object created from the XML string representation
     *
     * @throws IOException                  if an I/O error occurs
     * @throws SAXException                 if an XML parsing exception occurs.
     * @throws ParserConfigurationException if a JAXP configuration error
     *                                      occurs.
     */
    public static Document stringToDocument(final String xml)
        throws SAXException, IOException, ParserConfigurationException
    {
        return loadXMLFrom(new InputSource(new StringReader(xml)));
    }

    /**
     * Loads an XML document from the input stream into a DOM Document.
     *
     * @param is the input stream to load from
     *
     * @return the new Document
     *
     * @throws SAXException                 if a SAX parsing error occurs
     * @throws IOException                  if an IO error occurs
     * @throws ParserConfigurationException if a JAXP configuration parsing
     *                                      error occurs
     */
    public static Document loadXMLFrom(final InputStream is)
        throws SAXException, IOException, ParserConfigurationException
    {
        final DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        builder = factory.newDocumentBuilder();
        assert builder != null;
        final Document doc = builder.parse(is);
        is.close();
        return doc;
    }

    public static Document loadXMLFrom(final InputSource is)
        throws ParserConfigurationException, IOException, SAXException
    {
        final DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        builder = factory.newDocumentBuilder();
        assert builder != null;
        final Document doc = builder.parse(is);
        return doc;
    }

    /**
     * Runs a xalan transformation of the xml, with the specified xsl.
     *
     * @param xml        a String xml document, or a java.io.File object
     *                   pointing to the file
     * @param xsl        a String filename, or a java.io.File object pointing to
     *                   the file.  A null value indicates you want to resolve
     *                   the XML's "xml-stylesheet" processing instruction as
     *                   the XSL to use.  If it does not exist, it may fail.  If
     *                   using a filename, it will be looked up in the class
     *                   path.  Prefix all filenames with '/', where '/' is the
     *                   root of the classpath.
     * @param parameters a map of parameters to pass to the XSL
     *
     * @return the String of the transformed xml
     *
     * @throws IOException                  if an IO error occurs
     * @throws ParserConfigurationException if a JAXP configuration parsing
     *                                      error occurs
     * @throws TransformerException         if a transformation error occurs
     * @throws SAXException                 if a SAX parsing error occurs
     */
    public static String xslTransformation(final Object xml, final Object xsl,
        final Map parameters)
        throws TransformerException, ParserConfigurationException, IOException,
        SAXException
    {   // BEGIN xslTransformation()
        // create an instance of TransformerFactory
        final StringWriter stringWriter;
        final Result result;
        final TransformerFactory transFact;
        final Transformer trans;
        final Set keys;
        final Iterator keyIt;
        final ExceptionErrorListener errorListener;

        errorListener = new ExceptionErrorListener();
        stringWriter = new StringWriter(BUFFER_CAPACITY);
        result = new StreamResult(stringWriter);
        transFact = TransformerFactory.newInstance();
        transFact.setErrorListener(errorListener);
        final Source xmlSource;
        if (xml instanceof Document)
        {
            xmlSource = new DOMSource((Node) xml);
        }
        else if (xml instanceof String)
        {
            final Document doc = stringToDocument((String) xml);
            logger.debug(documentToString(doc));
            xmlSource = new DOMSource(doc);
/*            logger.debug("incoming XML: " + xml);
            xmlSource = new StreamSource(new StringReader((String) xml));*/
        }
        else if (xml instanceof File)
        {
            xmlSource = new StreamSource((File) xml);
        }
        else
        {
            throw new IllegalArgumentException(
                "Only java.lang.String and java.io.File xml are supported " +
                    "for the xml parameter");
        }

        final Source xsltSource;

        if (xsl == null)
        {   // grab the XSL defined by the XML's xml-stylesheet instruction
            xsltSource =
                transFact.getAssociatedStylesheet(xmlSource, null, null, null);
            if (xsltSource == null)
            {
                throw new IllegalArgumentException("the XML document does " +
                    "not contain an \"xml-stylesheet\" processing instruction" +
                    ",\nwe are unable transform this document unless you " +
                    "explicitly specify an XSL\ndocument to use");
            }
        }
        else if (xsl instanceof String)
        {
            final InputStream xsltResource = XMLUtil.class.getResourceAsStream(
                (String) xsl);
            if (xsltResource == null)
            {
                throw new IllegalArgumentException(
                    xsl + " is an invalid XSL file");
            }
            xsltSource = new StreamSource(xsltResource);
        }
        else if (xsl instanceof File)
        {
            xsltSource = new StreamSource((File) xsl);
        }
        else
        {
            throw new IllegalArgumentException(
                "Only java.lang.String xsl filenames, or java.io.File " +
                    "are supported for the xsl parameter");
        }


        trans = transFact.newTransformer(xsltSource);
        trans.setErrorListener(errorListener);
        if (parameters != null)
        {
            keys = parameters.keySet();
            keyIt = keys.iterator();
            while (keyIt.hasNext())
            {
                final String key;
                key = (String) keyIt.next();    // assume string key
                trans.setParameter(key, parameters.get(key));
            }
        }
        trans.transform(xmlSource, result);
        return stringWriter.toString();
    }   // END xslTransformation()

    /**
     * Calls {@link #xslTransformation(Object, Object, Map)} with the first
     * parameter set to xml, and the rest set to null}.  This implies that we
     * should resolve the XSL from the xml-stylesheet processing instruction in
     * the passed in xml.  As with {@link #xslTransformation (Object, Object,
     * Map)}, the xml can be a String, or a {@link File} object.
     *
     * @param xml the XML to transform
     *
     * @return the transformed XML
     *
     * @throws IOException                  if an IO error occurs
     * @throws ParserConfigurationException if a JAXP configuration parsing
     *                                      error occurs
     * @throws TransformerException         if a transformation error occurs
     * @throws SAXException                 if a SAX parsing error occurs
     */
    public static String xslTransformation(final Object xml)
        throws TransformerException, IOException, SAXException,
        ParserConfigurationException
    {
        return xslTransformation(xml, null, null);
    }

    /**
     * Converts the calendar into a string formated according to the xs:date
     * format.
     *
     * @param calendar the calendar to convert, must not be null
     *
     * @return the new format yyyy-MM-dd
     */
    public static String xsDateFormat(final Calendar calendar)
    {
        final SimpleDateFormat dateFormat;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(calendar.getTime());
    }

    /**
     * Converts the "now" calendar into xs:date format by calling {@link
     * #xsDateFormat(Calendar)}
     *
     * @return the new format yyyy-MM-dd
     */
    public static String xsDateFormatNow()
    {
        return xsDateFormat(Calendar.getInstance());
    }

    /**
     * Converts an XML xs:date string to a Calendar object.  We do not mess
     * around with formatting issues.  It either works, or it doesn't.
     *
     * @param xsDate the xs:date formatted string
     *
     * @return the Calendar object
     *
     * @throws ParseException if a parsing error occurs
     */
    public static Calendar xsDateToCalendar(final String xsDate) throws
        ParseException
    {
        Calendar newCal = null;
        if (xsDate != null)
        {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            final Date date = dateFormat.parse(xsDate);
            newCal = Calendar.getInstance();
            newCal.setTime(date);
        }
        return newCal;
    }

    /**
     * Formats a date in the xml xs:dateTime format, for mountain time.  This
     * method does not support any other timezone.
     *
     * @param calendar the calendar to format
     *
     * @return the formatted date
     */
    public static String xsDateTimeFormatMountain(final Calendar calendar)
    {
        final SimpleDateFormat dateFormat;
        final SimpleDateFormat timeFormat;
        final TimeZone currentTimeZone;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        currentTimeZone = TimeZone.getTimeZone("Canada/Mountain");
        final long offset;
        final long hours;
        final String tzID;
        offset = currentTimeZone.getOffset(calendar.getTimeInMillis());
        hours = -offset / (1000 * 60 * 60);
        tzID = "-0" + hours + ":00"; // assume single digit hour, we always are

        return dateFormat.format(calendar.getTime()) + 'T' +
            timeFormat.format(calendar.getTime()) + tzID;
    }

    /**
     * Calls {@link #xsDateTimeFormatMountain} with a new calendar object,
     * representing "now"
     *
     * @return the formatted date in xs:date format. e.g. 2009-07-30T10:53:36-06:00
     */
    public static String xsDateTimeFormatMountainNow()
    {
        return xsDateTimeFormatMountain(Calendar.getInstance());
    }

    /**
     * Generates a new document using the XML factory builder stuff.
     *
     * @return the new Document, never supposed to be null
     *
     * @throws ParserConfigurationException if a dom configuration error
     *                                      occurs.
     */
    public static Document createDocument() throws ParserConfigurationException
    {
        final DocumentBuilderFactory factory;
        final DocumentBuilder builder;
        final Document document;
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        return document;
    }

    /**
     * Converts the given document to string format by passing it through a
     * transformer.
     *
     * @param document the DOM document to process
     *
     * @return the XML string result
     *
     * @throws TransformerException if a transformation error occurs
     */
    public static String documentToString(final Document document)
        throws TransformerException
    {
        final TransformerFactory transformerFactory;
        final Transformer transformer;
        final DOMSource source;
        final StreamResult result;
        final StringWriter writer;

        writer = new StringWriter(BUFFER_CAPACITY);
        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        source = new DOMSource(document);
        result = new StreamResult(writer);
        transformer.transform(source, result);

        return writer.toString();
    }

    /**
     * Converts a map to XML, where parent is the parent element, and every
     * other element is of the form <key>value</key>.
     * <p/>
     * We assume that every key is an actual XML compatible element name.  The
     * values will be XML encoded automatically.
     *
     * @param parameters    the map to process
     * @param parentElement the parent element to append the new child to
     * @param document      the XML document to create new elements in
     *
     * @throws ParserConfigurationException if a configuration error occurs
     */
    public static void mapToNode(
        final Map parameters, final Element parentElement,
        final Document document)
        throws ParserConfigurationException
    {
        final Iterator it = parameters.keySet().iterator();
        Element tmp;

        while (it.hasNext())
        {
            final String key = (String) it.next();
            final Object value;

            value = parameters.get(key);
            if (value instanceof Map)
            {
                tmp = document.createElement(key);
                mapToNode((Map) value, tmp, document);
                parentElement.appendChild(tmp);
            }
            else
            {
                tmp = document.createElement(key);
                if (value != null)
                {   // null elements don't get in
                    tmp.appendChild(document.createTextNode((String) value));
                    parentElement.appendChild(tmp);
                }
            }
        }
    }

    /**
     * Converts a Map's keys and values to an XML document where the keys are
     * the elments, and the values are the textnodes (where value is String) or
     * subelements (where value is Map)
     * <p/>
     * The following code, will produce the XML below it.
     * <pre>
     * final Map parameters;
     * final Map subElements;
     * final Map innerElements;
     * parameters = new HashMap();
     * subElements = new HashMap();
     * innerElements = new HashMap();
     * parameters.put("element1", "value1");
     * parameters.put("element2", "value2");
     * parameters.put("element3", "value3");
     * parameters.put("element4", subElements);
     * subElements.put("subelement1", "value1");
     * subElements.put("subelement2", "value2");
     * subElements.put("innermost", innerElements);
     * innerElements.put("innerelement1", "innervalue1");
     * innerElements.put("innerelement2", "innervalue2");
     * System.out.println(XMLUtil.mapToXML("root", parameters));
     * </pre>
     * <pre>
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
     * &lt;root&gt;
     *   &lt;element4&gt;
     *     &lt;subelement2&gt;value2&lt;/subelement2&gt;
     *     &lt;subelement1&gt;value1&lt;/subelement1&gt;
     *     &lt;innermost&gt;
     *       &lt;innerelement1&gt;innervalue1&lt;/innerelement1&gt;
     *       &lt;innerelement2&gt;innervalue2&lt;/innerelement2&gt;
     *     &lt;/innermost&gt;
     *   &lt;/element4&gt;
     *   &lt;element2&gt;value2&lt;/element2&gt;
     *   &lt;element3&gt;value3&lt;/element3&gt;
     *   &lt;element1&gt;value1&lt;/element1&gt;
     * &lt;/root&gt;
     * </pre>
     *
     * @param rootElementName the name that you want the root element to have
     * @param parameters      the parameters to become the XML document
     *
     * @return the string representation of the XML document
     *
     * @throws TransformerException         if an XSL transformation exception
     *                                      occurs
     * @throws ParserConfigurationException if a JAXP configuration error
     *                                      occurs
     */
    public static String mapToXML(final String rootElementName,
        final Map parameters)
        throws TransformerException, ParserConfigurationException
    {
        final Document mapDoc;
        final Element parent;

        mapDoc = createDocument();
        parent = mapDoc.createElement(rootElementName);
        mapDoc.appendChild(parent);

        mapToNode(parameters, parent, mapDoc);
        return documentToString(mapDoc);
    }
}
