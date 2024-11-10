package store.domain;

import java.time.LocalDateTime;

public record Promotion(String promoteDescription, Long promoteQuantity, Long promotePlus,
                        LocalDateTime promoteStartDate, LocalDateTime promoteEndDate) {
}
