package ca.athabascau.apas.xml;

import ca.athabascau.apas.xml.ExceptionErrorListener;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
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
        return loadXMLFrom(new ByteArrayInputStream(xml.getBytes()));
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
        final ByteArrayOutputStream byteArray;
        final Result result;
        final TransformerFactory transFact;
        final Transformer trans;
        final Set keys;
        final Iterator keyIt;
        final ExceptionErrorListener errorListener;

        errorListener = new ExceptionErrorListener();
        byteArray = new ByteArrayOutputStream(BUFFER_CAPACITY);
        result = new StreamResult(byteArray);
        transFact = TransformerFactory.newInstance();
        transFact.setErrorListener(errorListener);
        final Source xmlSource;
        if (xml instanceof String)
        {
            final Document doc = stringToDocument((String) xml);
            logger.debug(documentToString(doc));
            xmlSource = new DOMSource(doc);
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
        return byteArray.toString();
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
        final ByteArrayOutputStream outputStream;

        outputStream = new ByteArrayOutputStream(BUFFER_CAPACITY);
        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        source = new DOMSource(document);
        result = new StreamResult(outputStream);
        transformer.transform(source, result);

        return outputStream.toString();
    }

    /**
     * Converts a map to XML, where rootElementName is the root element, and
     * every other element is of the form <key>value</key>.
     * <p/>
     * We assume that every key is an actual XML compatible element name.  The
     * values will be XML encoded automatically.
     *
     * @param rootElementName the name of the root element
     * @param parameters      the map to process
     *
     * @return the XML result.
     *
     * @throws ParserConfigurationException if a configuration error occurs
     * @throws TransformerException         if an XML transformation error
     *                                      occurs
     */
    public static String mapToXML(final String rootElementName,
        final Map parameters)
        throws ParserConfigurationException, TransformerException
    {
        final Iterator it = parameters.keySet().iterator();
        final Document mapDoc;
        final Element root;
        Element tmp;

        mapDoc = createDocument();
        root = mapDoc.createElement(rootElementName);
        mapDoc.appendChild(root);
        while (it.hasNext())
        {
            final String key = (String) it.next();
            final String value;

            value = (String) parameters.get(key);
            tmp = mapDoc.createElement(key);
            if (value != null)
            {   // null elements don't get in
                tmp.appendChild(mapDoc.createTextNode(value));
                root.appendChild(tmp);
            }
        }

        return documentToString(mapDoc);
    }
}
