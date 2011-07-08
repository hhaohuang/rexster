package com.tinkerpop.rexster.config;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.impls.sail.impls.MemoryStoreSailGraph;
import com.tinkerpop.blueprints.pgm.impls.sail.impls.NativeStoreSailGraph;
import com.tinkerpop.rexster.Tokens;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.log4j.Logger;

public abstract class AbstractSailGraphConfiguration implements GraphConfiguration {
    private static final Logger logger = Logger.getLogger(AbstractSailGraphConfiguration.class);

    public static final String SAIL_TYPE_MEMORY = "memory";
    public static final String SAIL_TYPE_NATIVE = "native";

    protected String sailType;

    public Graph configureGraphInstance(Configuration properties) throws GraphConfigurationException {
        String graphFile = properties.getString(Tokens.REXSTER_GRAPH_FILE, null);

        // get the <properties> section of the xml configuration
        HierarchicalConfiguration graphSectionConfig = (HierarchicalConfiguration) properties;
        SubnodeConfiguration sailSpecificConfiguration = null;

        try {
            sailSpecificConfiguration = graphSectionConfig.configurationAt(Tokens.REXSTER_GRAPH_PROPERTIES);
        } catch (IllegalArgumentException iae) {
            // it's ok if this is missing.  it is optional depending on the settings
        }

        // graph-file and data-directory must be present for native and neo4j
        if (sailType.equals(SAIL_TYPE_NATIVE) && (graphFile == null || graphFile.trim().length() == 0)) {
            throw new GraphConfigurationException("Check graph configuration. Missing or empty configuration element: " + Tokens.REXSTER_GRAPH_FILE);
        }

        try {

            SailGraph graph = null;

            if (this.sailType.equals(SAIL_TYPE_MEMORY)) {

                if (graphFile != null && !graphFile.isEmpty()) {
                    logger.warn("[" + MemoryStoreSailGraph.class.getSimpleName() + "] doesn't support the graph-file parameter.  It will be ignored.");
                }

                graph = new MemoryStoreSailGraph();

            } else if (this.sailType.equals(SAIL_TYPE_NATIVE)) {
                String configTripleIndices = "";
                if (sailSpecificConfiguration != null) {
                    configTripleIndices = sailSpecificConfiguration.getString("triple-indices", "");
                }

                if (configTripleIndices != null && configTripleIndices.trim().length() > 0) {
                    graph = new NativeStoreSailGraph(graphFile, configTripleIndices);
                } else {
                    graph = new NativeStoreSailGraph(graphFile);
                }
            }

            return graph;
        } catch (Exception ex) {
            throw new GraphConfigurationException(ex);
        }
    }

}
