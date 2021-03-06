package com.graphaware.es.gas.domain;

import com.graphaware.es.gas.cypher.CypherEndPoint;
import com.graphaware.es.gas.cypher.CypherEndPointBuilder;
import com.graphaware.es.gas.cypher.CypherResult;
import com.graphaware.es.gas.cypher.ResultRow;
import com.graphaware.integration.neo4j.test.EmbeddedGraphDatabaseServer;
import java.io.File;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static java.lang.System.getProperty;

public class CypherEndpointBoltTest {

    
    private CypherEndPoint cypherEndPoint;

    protected EmbeddedGraphDatabaseServer server;
    protected static final String BOLT_URL = "bolt://localhost:7687";
    protected static final String HTTP_URL = "http://localhost:7474";

    private static final File DEFAULT_KNOWN_HOSTS = new File(getProperty("user.home"),
            ".neo4j" + File.separator + "known_hosts");

    @Before
    public void setUp() {
        server = new EmbeddedGraphDatabaseServer();
        Map<String, Object> serverParams = new HashMap<>();
        serverParams.put("dbms.connector.0.enabled", true);
        server.start(serverParams);
        cypherEndPoint = new CypherEndPointBuilder(CypherEndPointBuilder.CypherEndPointType.BOLT)
                .neo4jHostname(HTTP_URL)
                .neo4jBoltHostname(BOLT_URL)
                .settings(Settings.EMPTY)
                .encryption(false)
                .build();
    }
    
    @After
    public void clear() {
        deleteDefaultKnownCertFileIfExists();
    }
    
    public static void deleteDefaultKnownCertFileIfExists() {
        if (DEFAULT_KNOWN_HOSTS.exists()) {
            DEFAULT_KNOWN_HOSTS.delete();
        }
    }

    @Test
    public void testExecuteCypher() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        cypherEndPoint.executeCypher("UNWIND range(1, 10) as x CREATE (n:Test) SET n.id = x", params);
        String query = "MATCH (n) RETURN n.id as id";
        CypherResult result = cypherEndPoint.executeCypher(query, params);
        assertEquals(10, result.getRows().size());
        int i = 0;
        for (ResultRow resultRow : result.getRows()) {
            assertTrue(resultRow.getValues().containsKey("id"));
            assertEquals(String.valueOf(++i), String.valueOf(resultRow.getValues().get("id")));
        }
    }

    @After
    public void tearDown() {
        server.stop();
    }
}
