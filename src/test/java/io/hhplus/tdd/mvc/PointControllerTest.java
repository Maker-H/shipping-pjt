package io.hhplus.tdd.mvc;

import io.hhplus.tdd.common.LoadJsonScenarios;
import io.hhplus.tdd.common.Scenario;
import io.hhplus.tdd.common.ScenarioKey;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

/**
 * <p>
[예외 테스트] <br/>
1. point에서 0이하의 포인트 충전시 CHARGE_AMOUNT_TOO_LOW <br/>
2. point 사용에서 0이하의 포인트 사용시 USE_AMOUNT_TOO_LOW <br/>
3. point 사용에서 잔액보다 많은 포인트 사용시  USE_AMOUNT_TOO_MUCH <br/>
4. 5초 내로 같은 주문시 ALREADY_ORDERED <br/>
 </p>
 <p>
[서비스 로직 테스트] <br/>
1. 서비스 history 조회시 업데이트 역순 정렬 반환 테스트 <br/>
 </p>
 */

@Profile("test")
@ActiveProfiles("test")
@LoadJsonScenarios("/test/point.json")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs(uriHost = "api.99", uriPort = 80)
public class PointControllerTest {

    private RequestSpecification spec;

    @Test
    @ScenarioKey("charge_success")
    void test(Scenario scenario) {

        RestAssured.given(this.spec)
                .filter(document(scenario.key(), scenario.asQuerySnippet(), scenario.asRequestSnippet()))
                .contentType(ContentType.JSON)
                .queryParams(scenario.queries())
                .body(scenario.body())
                .when()
                .post("/point/check-account")
                .then()
                .statusCode(200);
    }

    @LocalServerPort
    int port;

    @BeforeEach
    void setup(RestDocumentationContextProvider provider) {
        this.spec = new RequestSpecBuilder()
                .setPort(port)
                .addFilter(documentationConfiguration(provider)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }
}