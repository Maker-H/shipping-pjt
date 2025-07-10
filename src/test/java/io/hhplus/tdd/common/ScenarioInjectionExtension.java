package io.hhplus.tdd.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioInjectionExtension implements BeforeAllCallback, ParameterResolver {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Scenario> scenarioMap = new HashMap<>();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        Class<?> testClazz = extensionContext.getRequiredTestClass();

        LoadJsonScenarios loadJsonScenariosAnnotation = testClazz.getAnnotation(LoadJsonScenarios.class);
        if (loadJsonScenariosAnnotation == null) {
            throw new IllegalArgumentException("InjectJson annotation, scenario annotation is not found");
        }

        String resourceFileName = loadJsonScenariosAnnotation.value();
        try (InputStream inputStream = testClazz.getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("InjectJson file not found - " + loadJsonScenariosAnnotation.value());
            }

            List<Scenario> scenarios = mapper.readValue(inputStream, new TypeReference<>() {});
            for (Scenario scenario : scenarios) {
                scenario.validate(resourceFileName);
                scenarioMap.put(scenario.key(), scenario.toScenario());
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read or parse file: " + loadJsonScenariosAnnotation.value(), e);
        }

    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(Scenario.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Method testMethod = extensionContext.getRequiredTestMethod();
        ScenarioKey keyAnnotation = testMethod.getAnnotation(ScenarioKey.class);

        if (keyAnnotation == null) {
            throw new IllegalArgumentException("@ScenarioKey is missing on method: " + testMethod.getName());
        }

        String key = keyAnnotation.value();
        Scenario scenario = scenarioMap.get(key);

        if (scenario == null) {
            throw new IllegalArgumentException("No scenario found for key: " + key);
        }

        return scenario;
    }
}
