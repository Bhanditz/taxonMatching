package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.models.Node;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

//import org.bibalex.eol.handler.PropertiesHandler;
//import org.bibalex.eol.parser.models.HbaseResult;
//import org.bibalex.eol.parser.models.Node;
//import org.bibalex.eol.parser.models.NodeRecord;

/**
 * Created by Amr Morad
 */
public class RestClientHandler {

    public String doConnection(String uri, Object object){
        RestTemplate restTemplate;
        if(!uri.equalsIgnoreCase("")) {
            if(ResourceHandler.getPropertyValue("proxyExists").equalsIgnoreCase("true")) {
                restTemplate = handleProxy(ResourceHandler.getPropertyValue("proxy.url"),
                        Integer.parseInt(ResourceHandler.getPropertyValue("proxy.port")),
                        ResourceHandler.getPropertyValue("proxy.userName"),
                        ResourceHandler.getPropertyValue("proxy.password"));
            }else {
                restTemplate = new RestTemplate();
            }

            //create the json converter
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
            list.add(converter);
            restTemplate.setMessageConverters(list);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            // Pass the object and the needed headers
            ResponseEntity response = null;
            if(object instanceof Node) {
                HttpEntity<Node> entity = new HttpEntity<Node>((Node) object, headers);
                // Send the request as POST
                response = restTemplate.exchange(uri, HttpMethod.POST, entity, Integer.class);
                System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIII");
                System.out.println(response);

                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println(response.getBody());
                    return Integer.toString((Integer)response.getBody());
                } else {
                    System.out.println("returned code(" + response.getStatusCode() + ")");
                }
            }

        }else{
            System.out.println("Empty uri");
        }
        return "";
    }

    private RestTemplate handleProxy(String proxyUrl, int port, String username, String password){
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyUrl, port), new UsernamePasswordCredentials(username, password));
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(new HttpHost(proxyUrl, port));
        clientBuilder.setDefaultCredentialsProvider(credsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        CloseableHttpClient client = clientBuilder.build();
        //set the HTTP client
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(client);
        return new RestTemplate(factory);
    }
}
