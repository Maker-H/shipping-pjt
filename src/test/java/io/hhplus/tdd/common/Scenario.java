package io.hhplus.tdd.common;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Snippet;

import java.util.List;
import java.util.Map;

public record Scenario(
    String key,
    Map<String, String> pathParamsDescription,
    Map<String, String> queriesDescription,
    Map<String, String> bodyDescription,
    Map<String, Object> pathParams,
    Map<String, Object> queries,
    Map<String, Object> body,
    Map<String, Object> response
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
        return new Scenario(key, pathParamsDescription, queriesDescription, bodyDescription, pathParams, queries, body, response);
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

    public Snippet asRequestSnippet() {

        List<FieldDescriptor> fieldDescriptors = bodyDescription
                .entrySet()
                .stream()
                .map(entry -> PayloadDocumentation.fieldWithPath(entry.getKey()).description(entry.getValue()))
                .toList();

        return PayloadDocumentation.requestFields(fieldDescriptors);
    }
}
