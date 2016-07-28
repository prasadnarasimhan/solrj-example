package com.github.mosuka.apache.solr.example.cmd;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.mosuka.apache.solr.example.utils.SolrJExampleUtil;

public class AddCommand implements Command {

  @Override
  public void execute(Map<String, Object> attrs) {
    Map<String, Object> responseMap = new LinkedHashMap<String, Object>();

    String responseJSON = null;

    SolrClient solrClient = null;
    try {
      if (attrs.containsKey("solr_url")) {
        String solrUrl = (String) attrs.get("solr_url");

        HttpSolrClient httpSolrClient = new HttpSolrClient.Builder(solrUrl).build();

        solrClient = httpSolrClient;
      } else {
        String zooKeeperHost = (String) attrs.get("zookeeper_host");
        String zooKeeperChroot = (String) attrs.get("zookeeper_chroot");
        String collection = (String) attrs.get("collection");

        CloudSolrClient cloudSolrClient = new CloudSolrClient.Builder().withZkHost(zooKeeperHost)
            .withZkChroot(zooKeeperChroot).build();
        cloudSolrClient.setDefaultCollection(collection);

        solrClient = cloudSolrClient;
      }

      SolrInputDocument document = SolrJExampleUtil.createDocument((String) attrs.get("data"));

      UpdateResponse response = solrClient.add(document);
      if (response.getStatus() == 0) {
        response = solrClient.commit();
      }

      responseMap.put("status", response.getStatus());
      responseMap.put("message", "OK");
    } catch (IOException e) {
      responseMap.put("status", 1);
      responseMap.put("message", "NG");
    } catch (SolrServerException e) {
      responseMap.put("status", 1);
      responseMap.put("message", "NG");
    } finally {
      if (solrClient != null) {
        try {
          solrClient.close();
        } catch (IOException e) {
          responseMap.put("status", 1);
          responseMap.put("message", e.getMessage());
        }
      }
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      responseJSON = mapper.writeValueAsString(responseMap);
    } catch (IOException e) {
      responseJSON = String.format("{\"status\":1, \"message\":\"%s\"}", e.getMessage());
    }
    System.out.println(responseJSON);
  }

}
