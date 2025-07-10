package io.hhplus.tdd.order;

import io.hhplus.tdd.point.TransactionType;
import lombok.RequiredArgsConstructor;

public record OrderKey (
        long userId,
        TransactionType type,
        long amount
) {

}
