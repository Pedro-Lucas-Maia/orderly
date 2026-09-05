package bti.pds.dinner.inventory.application.input;

import bti.pds.dinner.inventory.domain.MovementType;

public record RegisterMovementInput(
        MovementType type,
        int quantity,
        String reason
) {
}
