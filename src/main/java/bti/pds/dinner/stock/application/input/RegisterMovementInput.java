package bti.pds.dinner.stock.application.input;

import bti.pds.dinner.stock.domain.MovementType;

public record RegisterMovementInput(
        MovementType type,
        int quantity,
        String reason
) {
}
