package io.hhplus.tdd.mvc;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.get(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.history(id);
    }

    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.charge(id, amount);
    }

    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.use(id, amount);
    }

    @PostMapping("check-amount")
    public ResponseEntity<Map<String, Object>> charge(
            @RequestParam Long userId,
            @RequestBody ChargeRequest request
    ) {
        // 유효하지 않은 금액 처리 (ex. 10,000 이상이면 실패)
        if (request.amount() >= 10000) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "code", 500,
                            "message", "홍길동fail"
                    )
            );
        }

        // 성공 응답 예시 (실제로는 비즈니스 로직 처리 필요)
        return ResponseEntity.ok(
                Map.of(
                        "code", 200,
                        "message", "홍길동success"
                )
        );
    }

    public record ChargeRequest(String accountNo, int amount) {}
}
