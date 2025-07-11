package io.hhplus.tdd.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.transform.ToString;
import io.restassured.RestAssured;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.specification.Argument;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RequestPredicates;

import java.util.*;
import java.util.stream.IntStream;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.web.servlet.function.RequestPredicates.path;

public record Scenario(
    String key,
    List<Table> tables,
    Map<String, String> pathParamsDescription,
    Map<String, String> queriesDescription,
    Map<String, String> bodyDescription,
    Map<String, String> responseDescription,
    Map<String, Object> pathParams,
    Map<String, Object> queries,
    Map<String, Object> body,
    Map<String, Object> response
) {

    public void validate(String resourceName) {

        if (key == null) {
            throw new IllegalArgumentException("Scenario must have a 'key' field");
        }

        if (pathParams != null) {
            if (pathParams.size() != pathParamsDescription.size()) {
                throw new IllegalArgumentException("[fileName:" + resourceName + "] [Scenario:" + key + "] mismatched sizes between pathParams and pathParamsDescription");
            }
            validateHasSameKeys(resourceName, pathParams, pathParamsDescription);
        }


        if (queries != null) {
            if (queries.size() != queriesDescription.size()) {
                throw new IllegalArgumentException("[fileName:" + resourceName + "] [Scenario:" + key + "] mismatched sizes between queries and queriesDescription");
            }
            validateHasSameKeys(resourceName, queries, queriesDescription);
        }


        if (body != null) {
            if (body.size() != bodyDescription.size()) {
                throw new IllegalArgumentException("[fileName:" + resourceName + "] [Scenario:" + key + "] mismatched sizes between body and bodyDescription");
            }
            validateHasSameKeys(resourceName, body, bodyDescription);
        }


        if (response != null) {
            if (response.size() != responseDescription.size()) {
                throw new IllegalArgumentException("[fileName:" + resourceName + "] [Scenario:" + key + "] mismatched sizes between response and responseDescription");
            }
            validateHasSameKeys(resourceName, response, responseDescription);
        }

    }

    private void validateHasSameKeys(String resourceName, Map<String, Object> sourceMap, Map<String, String> descriptionMap) {
        String[] sourceArray = sourceMap.keySet().stream().toArray(String[]::new);
        String[] descriptionArray = descriptionMap.keySet().stream().toArray(String[]::new);

        Arrays.sort(sourceArray);
        Arrays.sort(descriptionArray);
        IntStream.range(0, sourceArray.length).forEach(i -> {
            if (!sourceArray[i].equals(descriptionArray[i])) {
                throw new IllegalArgumentException("[fileName:" + resourceName + "] [Scenario:" + key + "] [sourceKey:" + sourceArray[i] + "] [descriptionKey:" + descriptionArray[i] + "] mismatch");
            }
        });
    }

    public Scenario toScenario() {
        return new Scenario(key, tables, pathParamsDescription, queriesDescription, bodyDescription, responseDescription, pathParams, queries, body, response);
//        return new Scenario(key, tables, pathParamsDescription, queriesDescription, bodyDescription, pathParams, queries, body, response);
    }

    public Snippet asPathSnippet() {
        List<ParameterDescriptor> pathDescription = pathParamsDescription
                .entrySet()
                .stream()
                .map(entry -> RequestDocumentation.parameterWithName(entry.getKey()).description(entry.getValue()))
                .toList();

        return RequestDocumentation.pathParameters(pathDescription);
    }

    public Snippet asQuerySnippet() {

        List<ParameterDescriptor> queryDescription = this.queriesDescription
                .entrySet()
                .stream()
                .map(entry -> RequestDocumentation.parameterWithName(entry.getKey()).description(entry.getValue()))
                .toList();

        return RequestDocumentation.queryParameters(queryDescription);
    }

    public Snippet asBodySnippet() {

        List<FieldDescriptor> fieldDescriptors = bodyDescription
                .entrySet()
                .stream()
                .map(entry -> PayloadDocumentation.fieldWithPath(entry.getKey()).description(entry.getValue()))
                .toList();

        return PayloadDocumentation.requestFields(fieldDescriptors);
    }

    public Snippet asResponseSnippet() {
        List<ParameterDescriptor> responseDescription = this.responseDescription
                .entrySet()
                .stream()
                .map(entry -> RequestDocumentation.parameterWithName(entry.getKey()).description(entry.getValue()))
                .toList();

        return RequestDocumentation.formParameters(responseDescription);
    }

    public Matcher<?> asResponseMatcher() {
        return new CustomMatcher(response);
    }


    @ToString
    public static class Table {

        private final String tableName;
        private final List<Map<String, Object>> rows;

        @JsonCreator
        public Table(
                @JsonProperty("table") String tableName,
                @JsonProperty("rows") List<Map<String, Object>> rows
        ) {
            this.tableName = tableName;
            this.rows = rows;
        }

        public String getTableName() {
            return tableName;
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }
    }

    private static class CustomMatcher extends BaseMatcher {

        private Map<String, Object> response;
        public CustomMatcher(Map<String, Object> response) {
            this.response = response;
        }
        private ObjectMapper mapper = new ObjectMapper();

            @Override
            public boolean matches(Object actual) {

                Map<String, Object> actualMap;

                try {
                    actualMap = mapper.readValue((String) actual, new TypeReference<>(){});
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("wrong format of response in json file");
                }

                for (Map.Entry<String, Object> expected : this.response.entrySet()) {
                    String key = expected.getKey();
                    Object expectedValue = expected.getValue();
                    Object actualValue = actualMap.get(key);

                    if (actualValue == null) return false;

                    if (expectedValue instanceof Number && actualValue instanceof Number) {
                        if (((Number) expectedValue).longValue() != ((Number) actualValue).longValue()) {
                            return false;
                        }
                    } else if (!String.valueOf(expectedValue).equals(String.valueOf(actualValue))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(response.toString());
            }
    }
}
