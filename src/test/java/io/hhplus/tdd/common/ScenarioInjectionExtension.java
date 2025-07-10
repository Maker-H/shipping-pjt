package io.hhplus.tdd.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.extension.*;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 파일에서 테스트 시나리오를 로드하고 주입하는 junit5 확장 클래스
 *
 * <p>
 * - {@link LoadScenarios} 애너테이션이 붙은 테스트 클래스에서 시나리오를 로드  <br/>
 * - {@link ScenarioKey} 애너테이션에 설정된 value를 key로 애너테이션이 붙은 테스트 메서드에 {@link Scenario} 객체를 주입 <br/>
 * - 시나리오에 포함된 테이블 데이터를 메모리 DB에 삽입
 *
 * @see LoadScenarios
 * @see ScenarioKey
 */
public class ScenarioInjectionExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Scenario> scenarioMap = new HashMap<>();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        Class<?> testClazz = extensionContext.getRequiredTestClass();

        LoadScenarios loadScenariosAnnotation = testClazz.getAnnotation(LoadScenarios.class);
        if (loadScenariosAnnotation == null) {
            throw new IllegalArgumentException("InjectJson annotation, scenario annotation is not found");
        }

        String resourceFileName = loadScenariosAnnotation.value();
        try (InputStream inputStream = testClazz.getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("InjectJson file not found - " + loadScenariosAnnotation.value());
            }

            List<Scenario> scenarios = mapper.readValue(inputStream, new TypeReference<>() {});

            for (Scenario scenario : scenarios) {
                scenario.validate(resourceFileName);
                scenarioMap.put(scenario.key(), scenario.toScenario());
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read or parse file: " + loadScenariosAnnotation.value(), e);
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

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method testMethod = context.getRequiredTestMethod();
        ScenarioKey keyAnnotation = testMethod.getAnnotation(ScenarioKey.class);

        if (keyAnnotation == null) {
            throw new IllegalArgumentException("@ScenarioKey is missing on method: " + testMethod.getName());
        }

        String key = keyAnnotation.value();
        List<Scenario.Table> scenarioTables = scenarioMap.get(key).tables();
        if (scenarioTables != null){
            loadTables(context, scenarioTables);
        }
    }

    // TODO: db 붙이면 추후 변화
    private void loadTables(ExtensionContext extensionContext, List<Scenario.Table> tables) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(extensionContext);

        UserPointTable userPointTable = applicationContext.getBean(UserPointTable.class);
        PointHistoryTable pointHistoryTable = applicationContext.getBean(PointHistoryTable.class);

        for (Scenario.Table table : tables) {
            String tableName = table.getTableName();
            List<Map<String, Object>> rows = table.getRows();

            if (tableName.equals("userPoints")) {
                for (Map<String, Object> data : rows) {
                    Long id = Long.parseLong(data.get("id").toString());
                    Long amount = Long.parseLong(data.get("amount").toString());
                    userPointTable.insertOrUpdate(id, amount);
                }
            }

            if (tableName.equals("pointHistories")) {
                for (Map<String, Object> data : rows) {
                    pointHistoryTable.insert(
                            Long.parseLong(data.get("id").toString()),
                            Long.parseLong(data.get("amount").toString()),
                            TransactionType.from(data.get("type").toString()),
                            Long.parseLong(data.get("updateMillis").toString())
                    );
                }
            }
        }

    }

}
