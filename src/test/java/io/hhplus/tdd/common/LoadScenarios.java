package io.hhplus.tdd.common;

import org.junit.jupiter.api.extension.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * - 테스트 클래스에 JSON 기반 시나리오 파일을 로드하도록 지정하는 애너테이션 <br/>
 * - {@link ScenarioInjectionExtension}과 함께 사용 <br/>
 * - {@code value}는 리소스 경로
 * <p>
 *  @see ScenarioKey
 *  @see ScenarioInjectionExtension
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ScenarioInjectionExtension.class)
public @interface LoadScenarios {

    String[] json();

    String[] sql() default {};

}
