package bti.pds.dinner.auth.domain.event;

import bti.pds.dinner.auth.domain.User;

public record OnPasswordResetedEvent(User user, String token) {
}
