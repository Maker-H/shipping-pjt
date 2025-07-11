package io.hhplus.tdd.mvc;

import io.hhplus.tdd.common.AcceptanceTest;
import io.hhplus.tdd.common.LoadScenarios;
import io.hhplus.tdd.common.Scenario;
import io.hhplus.tdd.common.ScenarioKey;
import io.hhplus.tdd.order.OrderException;
import io.hhplus.tdd.point.PointException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

/**
 *
 [예외 테스트] <br>
 1. point에서 0이하의 포인트 충전시 CHARGE_AMOUNT_TOO_LOW <br>
 2. point 사용에서 0이하의 포인트 사용시 USE_AMOUNT_TOO_LOW <br>
 3. point 사용에서 잔액보다 많은 포인트 사용시  USE_AMOUNT_TOO_MUCH <br>
 4. 5초 내로 같은 주문시 ALREADY_ORDERED <br>
 <p>
 [서비스 로직 테스트] <br>
 1. 서비스 history 조회시 업데이트 역순 정렬 반환 테스트 <br>
 */

@Profile("test")
@ActiveProfiles("test")
@LoadScenarios(
        json = {"/test/point.json"},
        sql = {"/test/example.json"}
)
@DisplayName("포인트 관련 인수 테스트")
public class PointControllerTest extends AcceptanceTest {

    @Test
    @ScenarioKey("CHARGE_AMOUNT_TOO_LOW")
    @DisplayName("포인트 충전 - 금액이 0인 경우 CHARGE_AMOUNT_TOO_LOW 예외")
    void 포인트_충전_금액_0인_예외(Scenario scenario) {

        PointException.PointErrorType errorType = PointException.PointErrorType.CHARGE_AMOUNT_TOO_LOW;

        given(getSpec())
                .filter(document(scenario.key(), scenario.asPathSnippet(), scenario.asBodySnippet()))
                .contentType(ContentType.JSON)
                .body(scenario.body())
            .log().all()
                .when()
                .patch("/point/{id}/charge", scenario.pathParams())
                .then()
            .log().all()
                .statusCode(errorType.getHttpStatus().value())
                .body("message", equalTo(errorType.name()));

//                .body("message", equalTo(scenario.response().message()));

    }

    @Test
    @ScenarioKey("USE_AMOUNT_TOO_LOW")
    @DisplayName("포인트 사용 - 금액이 0인 경우 USE_AMOUNT_TOO_LOW 예외")
    void 포인트_사용_금액_0인경우_예외(Scenario scenario) {

        PointException.PointErrorType errorType = PointException.PointErrorType.USE_AMOUNT_TOO_LOW;

        given(getSpec())
                .filter(document(scenario.key(), scenario.asPathSnippet(), scenario.asBodySnippet()))
                .contentType(ContentType.JSON)
                .body(scenario.body())
            .log().all()
                .when()
                .patch("/point/{id}/use", scenario.pathParams())
                .then()
            .log().all()
                .statusCode(errorType.getHttpStatus().value())
                .body("message", equalTo(errorType.name()));
    }

    @Test
    @ScenarioKey("USE_AMOUNT_TOO_MUCH")
    @DisplayName("포인트 사용 - 잔액 초과 시 USE_AMOUNT_TOO_MUCH 예외")
    void 포인트_사용_잔액_초과_예외(Scenario scenario) {
        PointException.PointErrorType errorType = PointException.PointErrorType.USE_AMOUNT_TOO_MUCH;

        given(getSpec())
                .filter(document(scenario.key(), scenario.asPathSnippet(), scenario.asBodySnippet()))
                .contentType(ContentType.JSON)
                .body(scenario.body())
            .log().all()
                .when()
                .patch("/point/{id}/use", scenario.pathParams())
                .then()
            .log().all()
                .statusCode(errorType.getHttpStatus().value())
                .body("message", equalTo(errorType.name()));
    }

    @Test
    @ScenarioKey("ALREADY_ORDERED")
    @DisplayName("동일 주문 요청 - 5초 내 중복 주문 시 ALREADY_ORDERED 예외")
    void 중복_주문_5초내_요청_예외(Scenario scenario) {
        OrderException.OrderErrorType errorType = OrderException.OrderErrorType.ALREADY_ORDERED;

        // 첫번째 주문
        given(getSpec())
                .filter(document(scenario.key(), scenario.asPathSnippet(), scenario.asBodySnippet()))
                .contentType(ContentType.JSON)
                .body(scenario.body())
            .log().all()
                .when()
                .patch("/point/{id}/use", scenario.pathParams())
                .then()
            .log().all();


        // 두번째 주문
        given(getSpec())
                .filter(document(scenario.key(), scenario.asPathSnippet(), scenario.asBodySnippet()))
                .contentType(ContentType.JSON)
                .body(scenario.body())
            .log().all()
                .when()
                .patch("/point/{id}/use", scenario.pathParams())
                .then()
            .log().all()
                .statusCode(errorType.getHttpStatus().value())
                .body("message", equalTo(errorType.name()));
    }


    @Test
    @ScenarioKey("CHARGE_SUCCESS")
    @DisplayName("포인트 충전 - 성공")
    void 포인트_충전_성공(Scenario scenario) {


        given(getSpec())
                .filter(document(scenario.key(), scenario.asPathSnippet(), scenario.asBodySnippet()))
                .contentType(ContentType.JSON)
                .body(scenario.body())
                .log().all()
                .when()
                .patch("/point/{id}/charge", scenario.pathParams())
                .then()
                .log().all()
                .statusCode(200)
                .body(scenario.asResponseMatcher());

//                .body(
//                        "id", Matchers.equalTo(scenario.response().get("id")),
//                        "id", Matchers.equalTo(scenario.response().get("id"))
//                );

//                .body("message", equalTo(scenario.response().message()));

    }

}