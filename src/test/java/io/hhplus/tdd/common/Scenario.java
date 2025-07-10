package io.hhplus.tdd.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import groovy.transform.ToString;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Snippet;

import java.util.List;
import java.util.Map;

public record Scenario(
    String key,
    List<Table> tables,
    Map<String, String> pathParamsDescription,
    Map<String, String> queriesDescription,
    Map<String, String> bodyDescription,
    Map<String, Object> pathParams,
    Map<String, Object> queries,
    Map<String, Object> body
//    Response response
) {

    public void validate(String resourceName) {
        if (key == null) {
            throw new IllegalArgumentException("Scenario must have a 'key' field");
        }

        if (pathParams != null && pathParams.size() != pathParamsDescription.size()) {
            throw new IllegalArgumentException(resourceName + "/Scenario : " + key + " mismatched sizes between pathParams and pathParamsDescription");
        }
        if (queries != null && queries.size() != queriesDescription.size()) {
            throw new IllegalArgumentException(resourceName + "/Scenario : " + key + " mismatched sizes between queries and queriesDescription");
        }
        if (body != null && body.size() != bodyDescription.size()) {
            throw new IllegalArgumentException(resourceName + "/Scenario : " + key + " mismatched sizes between body and bodyDescription");
        }
    }

    public Scenario toScenario() {
        return new Scenario(key, tables, pathParamsDescription, queriesDescription, bodyDescription, pathParams, queries, body);
//        return new Scenario(key, tables, pathParamsDescription, queriesDescription, bodyDescription, pathParams, queries, body, response);
    }

    public Snippet asPathSnippet() {
        List<ParameterDescriptor> pathDescriptors = pathParams
                .entrySet()
                .stream()
                .map(entry ->RequestDocumentation.parameterWithName(entry.getKey()).description("path param: " + entry.getValue()))
                .toList();

        return RequestDocumentation.pathParameters(pathDescriptors);
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

    public static class Response {

        private final int code;
        private final String message;

        @JsonCreator
        public Response(
                @JsonProperty("code") int code,
                @JsonProperty("message") String message
        ) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
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
}
