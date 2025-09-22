package com.task.submission;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class SubmissionApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // create simple HTTP client and JSON parser
        RestTemplate rest = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // API to generate webhook + token
        String genUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        // body with my details (keep in one line to avoid hidden spaces)
        String bodyJson = "{ \"name\": \"Aishwarya Satija\", \"regNo\": \"0101EC221020\", \"email\": \"satijaaishwarya04@gmail.com\" }";

        // send POST to generate webhook
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        ResponseEntity<String> genResp = rest.exchange(genUrl, HttpMethod.POST, entity, String.class);

        System.out.println("Generate API Response: " + genResp.getBody());

        // parse response to get webhook + token
        JsonNode json = mapper.readTree(genResp.getBody());
        String webhook = json.get("webhook").asText();
        String token = json.get("accessToken").asText();

        // SQL query (single line so JSON stays valid)
        String finalQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, "
                + "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT "
                + "FROM EMPLOYEE e1 "
                + "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID "
                + "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT "
                + "AND e2.DOB > e1.DOB "
                + "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME "
                + "ORDER BY e1.EMP_ID DESC;";

        // prepare JSON with the query
        String answerJson = "{ \"finalQuery\": \"" + finalQuery + "\" }";

        // send query to webhook
        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        headers2.set("Authorization", token);
        HttpEntity<String> entity2 = new HttpEntity<>(answerJson, headers2);
        ResponseEntity<String> submitResp = rest.exchange(webhook, HttpMethod.POST, entity2, String.class);

        System.out.println("Submit API Response: " + submitResp.getBody());
    }
}
