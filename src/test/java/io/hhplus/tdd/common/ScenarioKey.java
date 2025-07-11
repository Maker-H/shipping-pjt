package io.hhplus.tdd.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 테스트 메서드에 사용할 시나리오의 키 값을 지정
 *
 * <p>
 * {@link LoadScenarios}로 로드된 시나리오 목록 중,
 * 해당 키({@code value})에 해당하는 시나리오 객체가 테스트 메서드에 주입 됨
 *
 * <p>
 * {@code value}는 시나리오 JSON 내의 {@code "key"} 필드와 매칭되어야 함
 *
 * @see LoadScenarios
 * @see ScenarioInjectionExtension
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScenarioKey {
    String value();
}
