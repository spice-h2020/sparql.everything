package com.github.spiceh2020.sparql.anything.engine;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.spiceh2020.sparql.anything.model.Triplifier;
import com.github.spiceh2020.sparql.anything.rdf.RDFTriplifier;

public final class FacadeX {

	private static final Logger log = LoggerFactory.getLogger(FacadeX.class);

	public final static OpExecutorFactory ExecutorFactory = new OpExecutorFactory() {
		@Override
		public OpExecutor create(ExecutionContext execCxt) {
			return new FacadeXOpExecutor(execCxt);
		}
	};

	public final static TriplifierRegister Registry = TriplifierRegister.getInstance();

	static {
		try {
			log.trace("Registering isFacadeXExtension function");

			FunctionRegistry.get().put(Triplifier.FACADE_X_CONST_NAMESPACE_IRI + "isFacadeXExtension",
					IsFacadeXExtension.class);

			log.trace("Registering standard triplifiers");
//            Registry.registerTriplifier(new XMLTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.xml.XMLTriplifier",
					new String[] { "xml" }, new String[] { "application/xml" });
//            Registry.registerTriplifier(new CSVTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.csv.CSVTriplifier",
					new String[] { "csv" }, new String[] { "text/csv" });

//            Registry.registerTriplifier(new HTMLTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.html.HTMLTriplifier",
					new String[] { "html" }, new String[] { "text/html" });

//            Registry.registerTriplifier(new TextTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.text.TextTriplifier",
					new String[] { "txt" }, new String[] { "text/plain" });

			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.zip.TarTriplifier",
					new String[] { "tar" }, new String[] { "application/x-tar" });

			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.zip.ZipTriplifier",
					new String[] { "zip" }, new String[] { "application/zip" });

//            Registry.registerTriplifier(new BinaryTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.binary.BinaryTriplifier",
					new String[] { "bin", "dat" }, new String[] { "application/octet-stream" });

//            Registry.registerTriplifier(new JSONTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.json.JSONTriplifier",
					new String[] { "json" }, new String[] { "application/json" });

//            Registry.registerTriplifier(new SpreadsheetTriplifier());
			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.spreadsheet.SpreadsheetTriplifier",
					new String[] { "xls", "xlsx" }, new String[] { "application/vnd.ms-excel",
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });

			Registry.registerTriplifier(RDFTriplifier.class.getCanonicalName(),
					new String[] { "rdf", "ttl", "nt", "jsonld", "owl", "trig", "nq", "trix", "trdf" },
					new String[] { "application/rdf+thrift", "application/trix+xml", "application/n-quads", "text/trig",
							"application/owl+xml", "text/turtle", "application/rdf+xml", "application/n-triples",
							"application/ld+json" });

			Registry.registerTriplifier("com.github.spiceh2020.sparql.anything.binary.BinaryTriplifier",
					new String[] { "png", "jpeg", "jpg", "bmp", "tiff", "tif", "ico" },
					new String[] { "image/png", "image/jpeg", "image/bmp", "image/tiff", "image/vnd.microsoft.icon" });
			// Common image file types
//            Registry.registerTriplifier(new BinaryTriplifier(){
//                @Override
//                public Set<String> getExtensions() {
//                    return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
//                            "png","jpeg","jpg","bmp","tiff","tif", "ico"
//                    )));
//                }
//
//                @Override
//                public Set<String> getMimeTypes() {
//                    return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
//                            "image/png","image/jpeg","image/bmp", "image/tiff", "image/vnd.microsoft.icon"
//                    )));
//                }
//            });
		} catch (TriplifierRegisterException e) {
			throw new RuntimeException(e);
		}
	}
}
