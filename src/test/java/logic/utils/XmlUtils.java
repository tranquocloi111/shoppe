package logic.utils;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import framework.utils.Log;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringReader;

import static com.sun.org.apache.xml.internal.security.Init.init;

import static org.junit.Assert.assertEquals;

public class XmlUtils {
    static {
        init();
    }

    public static String toCanonicalXml(String xml)  {
        try {
            Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
            byte canonXmlBytes[] = canon.canonicalize(xml.getBytes());
            return new String(canonXmlBytes);
        }catch (Throwable ex){
            Log.error(ex.getMessage());
        }
        return null;
    }

    public static String prettyFormat(String input) {
        try {
            InputSource src = new InputSource(new StringReader(input));
            Element document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
            Boolean keepDeclaration = input.startsWith("<?xml");
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);
            return writer.writeToString(document);
        } catch (Throwable ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    public static void assertXMLEqual(String expected, String actual){
        String canonicalExpected = prettyFormat(toCanonicalXml(expected));
        String canonicalActual = prettyFormat(toCanonicalXml(actual));
        assertEquals(canonicalExpected, canonicalActual);
    }
}
