package org.metalscraps.eso.lang.lib.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.metalscraps.eso.lang.lib.util.Utils.getBodyFromHTTPsRequest;

public class UtilsTest {

    @Test
    public void getBodyFromHTTPsRequestTest() {
        String projectName = "ESO-Book";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://www.dostream.com/zanata/rest/projects/p/"+projectName))
                .header("Accept","application/json")
                .build();
        JsonNode jsonNode = getBodyFromHTTPsRequest(request);
        System.out.println(jsonNode);
        System.out.println("===================================================================");
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://www.dostream.com/zanata/rest/projects/p/"+projectName+"/iterations/i/1.0/r"))
                .header("Accept","application/json")
                .build();
        jsonNode = getBodyFromHTTPsRequest(request);
        System.out.println(jsonNode);


        for (Iterator<JsonNode> it = jsonNode.elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            System.out.println(node.get("name"));
        }

    }
}