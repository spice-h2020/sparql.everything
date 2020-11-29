package com.github.spiceh2020.sparql.anything.json;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.spiceh2020.json2rdf.transformers.JSONTransformer;
import com.github.spiceh2020.sparql.anything.model.Triplifier;

public class JSONTriplifier implements Triplifier {

	public final static String propertyPrefix = "propertyPrefix", uriRoot = "uriRoot";
	
	private static Logger logger = LogManager.getLogger(JSONTriplifier.class);

	@Override
	public DatasetGraph triplify(URL url, Properties properties) throws IOException {
		logger.trace("Triplifying "+url.toString());
		JSONTransformer jt;
		if (properties.containsKey(propertyPrefix)) {
			logger.trace("Property prefix provided");
			jt = new JSONTransformer(properties.getProperty(propertyPrefix));
		} else {
			logger.trace("Property prefix not provided");
			jt = new JSONTransformer(url.toString() + "/");
		}
		if (properties.containsKey(uriRoot)) {
			jt.setURIRoot(properties.getProperty(uriRoot));
		}

		Model m = jt.transformJSONFromURL(url);
		logger.trace("Number of triples "+m.size());
		DatasetGraph dg = DatasetFactory.create(m).asDatasetGraph();
		dg.addGraph(NodeFactory.createURI(url.toString()), dg.getDefaultGraph());
		return dg;
	}

	@Override
	public Set<String> getMimeTypes() {
		return Sets.newHashSet("application/json");
	}

	@Override
	public Set<String> getExtensions() {
		return Sets.newHashSet("json");
	}
}