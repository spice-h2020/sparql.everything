package com.github.spiceh2020.sparql.anything.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterDefaulting;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.spiceh2020.sparql.anything.facadeiri.FacadeIRIParser;
import com.github.spiceh2020.sparql.anything.metadata.MetadataTriplifier;
import com.github.spiceh2020.sparql.anything.model.IRIArgument;
import com.github.spiceh2020.sparql.anything.model.Triplifier;

public class FacadeXOpExecutor extends OpExecutor {

	private TriplifierRegister triplifierRegister;

	private static final Logger logger = LoggerFactory.getLogger(FacadeXOpExecutor.class);
	private MetadataTriplifier metadataTriplifier = new MetadataTriplifier();
	public static String METADATA_GRAPH_IRI = "facade-x:metadata";
	public static String FACADE_X_NAMESPACE_IRI = "urn:facade-x:ns#";

	public FacadeXOpExecutor(ExecutionContext execCxt) {
		super(execCxt);
		triplifierRegister = TriplifierRegister.getInstance();
	}

	protected QueryIterator execute(final OpService opService, QueryIterator input) {
		logger.trace("SERVICE uri: {}", opService.getService());
		if (opService.getService().isURI()) {
			logger.trace("is uri: {}", opService.getService());
			if (isFacadeXURI(opService.getService().getURI())) {
				logger.trace("Facade-X uri: {}", opService.getService());
				try {
					Triplifier t;
					Properties p = getProperties(opService.getService().getURI());

					logger.trace("Properties extracted " + p.toString());

					String urlLocation = p.getProperty(IRIArgument.LOCATION.toString());

					if (p.containsKey(IRIArgument.TRIPLIFIER.toString())) {
						logger.trace("Triplifier enforced");
						t = (Triplifier) Class.forName(p.getProperty(IRIArgument.TRIPLIFIER.toString()))
								.getConstructor().newInstance();
					} else if (p.containsKey(IRIArgument.MEDIA_TYPE.toString())) {
						logger.trace("MimeType enforced");
						t = triplifierRegister
								.getTriplifierForMimeType(p.getProperty(IRIArgument.MEDIA_TYPE.toString()));
					} else {
						logger.trace(
								"Guess triplifier using file extension " + FilenameUtils.getExtension(urlLocation));
						t = triplifierRegister.getTriplifierForExtension(FilenameUtils.getExtension(urlLocation));
					}
					// If triplifier is null, return an empty graph
					DatasetGraph dg;

					URL url;
					try {
						url = new URL(urlLocation);
					} catch (MalformedURLException u) {
						logger.trace("Malformed url interpreting as file");
						url = new File(urlLocation).toURI().toURL();
					}

					if (t != null) {
						dg = t.triplify(url, p);
					} else {
						logger.error("No triplifier available for the input format!");
						dg = DatasetFactory.create().asDatasetGraph();
					}
					if (triplifyMetadata(p)) {
						dg.addGraph(NodeFactory.createURI(METADATA_GRAPH_IRI),
								metadataTriplifier.triplify(url, p).getDefaultGraph());
					}

					return QC.execute(opService.getSubOp(), input, new ExecutionContext(execCxt.getContext(),
							dg.getDefaultGraph(), dg, execCxt.getExecutor()));
				} catch (IllegalArgumentException | SecurityException | IOException | InstantiationException
						| IllegalAccessException | InvocationTargetException | NoSuchMethodException
						| ClassNotFoundException e) {
					logger.error("An error occurred", e);
				}
			} else {
				// Pass to parent
				logger.trace("not a facade-x uri: {}", opService.getService());
				return super.execute(opService, input);
			}
		} else if (opService.getService().isVariable()) {
			logger.trace("is variable: {}", opService.getService());
			// Postpone to next iteration
			return new QueryIterRepeatApply(input, execCxt) {

				@Override
				protected QueryIterator nextStage(Binding binding) {
					Op op2 = QC.substitute(opService, binding);
					QueryIterator thisStep = QueryIterSingleton.create(binding, this.getExecContext());
					QueryIterator cIter = QC.execute(op2, thisStep, super.getExecContext());
					cIter = new QueryIterDefaulting(cIter, binding, this.getExecContext());
					return cIter;
				}
			};
		}
		logger.trace("Not a Variable and not a IRI: {}", opService.getService());
		return super.execute(opService, input);
	}

	private Properties getProperties(String url) {
		FacadeIRIParser p = new FacadeIRIParser(url);
		Properties properties = p.getProperties();
		// Setting defaults

		// namespace <urn:facade-x/ns#>
		if (!properties.containsKey(IRIArgument.NAMESPACE.toString())) {
			logger.trace("Setting default value for namespace: {}", FACADE_X_NAMESPACE_IRI);
			properties.setProperty(IRIArgument.NAMESPACE.toString(), FACADE_X_NAMESPACE_IRI);
		}
		return properties;
	}

	private boolean triplifyMetadata(Properties p) {
		boolean result = false;
		if (p.containsKey(IRIArgument.METADATA.toString())) {
			try {
				result = Boolean.parseBoolean(p.getProperty(IRIArgument.METADATA.toString()));
			} catch (Exception e) {
				result = false;
			}
		}
		return result;
	}

	protected boolean isFacadeXURI(String uri) {
		if (uri.startsWith("facade-x:")) {
			return true;
		}
		return false;
	}

}
