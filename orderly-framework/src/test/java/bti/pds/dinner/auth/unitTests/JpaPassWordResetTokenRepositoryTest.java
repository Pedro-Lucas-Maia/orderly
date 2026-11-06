package bti.pds.dinner.auth.unitTests;

import bti.pds.dinner.auth.domain.PasswordResetToken;
import bti.pds.dinner.auth.domain.PasswordResetTokenId;
import bti.pds.dinner.auth.domain.UserId;
import bti.pds.dinner.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import bti.pds.dinner.auth.infrastructure.persistence.repository.JpaPassWordResetTokenRepository;
import bti.pds.dinner.auth.infrastructure.persistence.repository.PasswordResetTokenEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaPassWordResetTokenRepositoryTest {

    @Mock
    private PasswordResetTokenEntityRepository entityRepository;

    @InjectMocks
    private JpaPassWordResetTokenRepository jpaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should persist entity in database via entityRepository.save")
    void testSavePersistsToDatabase() {
        UUID userId = UUID.randomUUID();
        String tokenStr = "test-token-123";
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        PasswordResetToken domainToken = new PasswordResetToken(tokenStr, new UserId(userId), expiry);

        when(entityRepository.save(any(PasswordResetTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetToken result = jpaRepository.save(domainToken);

        ArgumentCaptor<PasswordResetTokenEntity> captor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(entityRepository, times(1)).save(captor.capture());

        PasswordResetTokenEntity savedEntity = captor.getValue();
        assertEquals(tokenStr, savedEntity.getToken());
        assertEquals(userId, savedEntity.getUserId());
        assertEquals(domainToken.getId().uuid(), savedEntity.getId());
        assertEquals(domainToken.getToken(), result.getToken());
    }

    @Test
    @DisplayName("Should find token by token string from entityRepository")
    void testFindByToken() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity(id, "token-abc", userId, expiry);

        when(entityRepository.findByToken("token-abc")).thenReturn(Optional.of(entity));

        Optional<PasswordResetToken> result = jpaRepository.findByToken("token-abc");

        assertTrue(result.isPresent());
        assertEquals("token-abc", result.get().getToken());
        assertEquals(userId, result.get().getUserId().uuid());
    }

    @Test
    @DisplayName("Should delete token by user id")
    void testDeleteByUserId() {
        UUID userId = UUID.randomUUID();

        jpaRepository.deleteByUserId(new UserId(userId));

        verify(entityRepository, times(1)).deleteByUserId(userId);
    }
}
