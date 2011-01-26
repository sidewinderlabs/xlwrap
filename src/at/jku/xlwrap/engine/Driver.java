package at.jku.xlwrap.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
                    + "Syntax: input [output]");
            return;
        }

        // Read the input file.
        XLWrapMapping map = MappingParser.parse(args[0]);
        XLWrapMaterializer mat = new XLWrapMaterializer();
        Model m = mat.generateModel(map);

        // do some RDF prettifying.
        prettifyNamespaces(m);

        // get or compute the output file name
        String outfile;
        if (args.length == 2) {
            outfile = args[1];
        } else {
            File f = new File(args[0]);
            outfile = f.getName();
            int dotPos = outfile.lastIndexOf('.');
            if (dotPos > 0) {
                outfile = outfile.substring(0, dotPos);
            }
            outfile = outfile + ".rdf";
        }

        // Write it!
        m.write(new FileOutputStream(outfile), "RDF/XML");
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
}
