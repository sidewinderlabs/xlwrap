package at.jku.xlwrap.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.XLWrapMaterializer;
import at.jku.xlwrap.map.MappingParser;
import at.jku.xlwrap.map.XLWrapMapping;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NsIterator;

/**
 * Small utility to convert <code>.trig</code> files to RDF/XML.
 * 
 * Create an instance and call {@link #run()}.
 * 
 * @author Moritz Hoffmann <moritz@antiguru.de>
 * 
 */
public class Driver {

	private static final Logger log = LoggerFactory.getLogger(Driver.class);

    private final String[] args;

    /**
     * @param args
     *            Command line arguments.
     * 
     */
    public Driver(String[] args) {
        super();
        this.args = args;
    }

    /**
     * Start working.
     * 
     * @throws XLWrapException
     * @throws FileNotFoundException
     */
    public void run() throws XLWrapException, FileNotFoundException {
        if (args.length == 0) {
            System.err.println("Missing input file name.\n"
                    + "Syntax: input [output] [options] [var1=val1] [var2=val2] ...\n"
                    + "Options:\n"
            		+ "\t-lang=[language]\tDefines the output serialization language. Can be RDF/XML (default), RDF/XML-ABBREV, N-TRIPLE, TURTLE, TTL or N3");
            return;
        }

        // Read the input file.
        String url = args[0];
        String outfile = null;

        // parse options
        String lang = "RDF/XML";
    	Map<String, String> variables = new HashMap<String, String>();
    	for (int i=1; i<args.length; i++) {
    		if (args[i].indexOf("=") < 0) {
    			outfile = args[i];
    		} else {
	    		String[] var = args[i].split("=");
	    		String varName = var[0];
	    		String varValue = var[1].replace("\"", "");
	    		if ("-lang".equals(varName)) {
	    			lang = varValue;
	    		} else {
	    			variables.put(varName, varValue);
	    		}
    		}
    	}
    	// replace variables (if any)
    	if (! variables.isEmpty())
    		url = replaceVariables(url, variables);

        XLWrapMapping map = MappingParser.parse(url);
        XLWrapMaterializer mat = new XLWrapMaterializer();
        Model m = mat.generateModel(map);

        // do some RDF prettifying.
        prettifyNamespaces(m);

        // get or compute the output file name
        if (outfile == null) {
            File f = new File(args[0]);
            outfile = f.getName();
            int dotPos = outfile.lastIndexOf('.');
            if (dotPos > 0) {
                outfile = outfile.substring(0, dotPos);
            }
            outfile = outfile + ".rdf";
        }

        // Write it!
        m.write(new FileOutputStream(outfile), lang);
    }

    /**
     * @param args
     *            The command line arguments. First one is the input
     *            <code>.trig</code>, followed by an optional output file name.
     *            The output file is otherwise the input file's base name with
     *            <code>.rdf</code> appended to it.
     * @throws XLWrapException
     *             Forwarded from xlwrap.
     * @throws FileNotFoundException
     *             Forwarded from {@link FileOutputStream}.
     */
    public static void main(String[] args) throws XLWrapException,
            FileNotFoundException {
        Driver driver = new Driver(args);
        driver.run();
    }

    /**
     * Convert the name spaces to some abbreviations in order to produce better
     * readable RDF.
     * 
     * @param model
     *            The model to work on. Note that the name space prefixes are
     *            only changed for
     */
    private void prettifyNamespaces(Model model) {
        NsIterator nameSpaces = model.listNameSpaces();
        nameSpaces: while (nameSpaces.hasNext()) {
            String namespace = nameSpaces.next();
            String originalNamespace = namespace;
            if (model.getNsURIPrefix(namespace) != null) {
                continue nameSpaces;
            }
            namespace = namespace.replaceAll("[\\-#0-9.]", "");
            String[] parts = namespace.split("/");
            for (int i = parts.length - 1; i > 0; i = i - 1) {
                if (model.getNsPrefixURI(parts[i]) == null) {
                    model.setNsPrefix(parts[i], originalNamespace);
                    continue nameSpaces;
                }
            }
        }

    }
    
    /**
     * Replaces variables in a TRIG file and returns the URL of the merged file
     * @param url the URL to the input TRIG file
     * @throws XLWrapException
     */
    private String replaceVariables(String url, Map<String, String> variables) throws XLWrapException {
    	try {
    	if (url.indexOf(":") < 0)
    		url =  "file:" + url;
    	URL inputURL = new URL(url);
    	BufferedReader br = null;
    	BufferedWriter bw = null;
    	try {
    		String newLine = System.getProperty("line.separator");
    	    File merged = File.createTempFile("xlwrap_merged_", ".trig");
    	    merged.deleteOnExit();
    	    if (log.isDebugEnabled())
    	    	log.debug("Replacing variables " + variables +" in temporary file: " + merged.getAbsolutePath());
    		
    		URLConnection urlConnection = inputURL.openConnection();
    		br = new BufferedReader(
                    new InputStreamReader(
                    		urlConnection.getInputStream()));
    		bw = new BufferedWriter(
    				new FileWriter(merged));

    		String inputLine;
    		while ((inputLine = br.readLine()) != null) {
    			String mergedLine = inputLine;
    			for (Map.Entry<String, String> var : variables.entrySet()) 
    				mergedLine = mergedLine.replace("${"+var.getKey()+"}", var.getValue());			
    			bw.write(mergedLine + newLine);
    		}
    		bw.flush();
            return merged.toURI().toString();
    	} finally {
    		if (br != null) {
    			try {
    				br.close();
    			} catch (IOException ignore) {}
    		}
    		if (bw != null) {
    			try {
    				bw.close();
    			} catch (IOException ignore) {}
    		}
    	}
    	} catch (Exception exc) {
    		throw new XLWrapException("Unable to merge variables into TRIG file: " + exc.getMessage());
    	}
    }
}
