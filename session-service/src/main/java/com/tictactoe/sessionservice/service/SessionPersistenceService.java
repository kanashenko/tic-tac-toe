package com.tictactoe.sessionservice.service;

import com.tictactoe.sessionservice.exception.SessionNotFoundException;
import com.tictactoe.sessionservice.model.MoveEntity;
import com.tictactoe.sessionservice.model.SessionEntity;
import com.tictactoe.sessionservice.model.SessionStatus;
import com.tictactoe.sessionservice.repository.MoveRepository;
import com.tictactoe.sessionservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * The only class in this service that talks to JPA directly. Kept separate
 * from {@link SessionService} and {@link SimulationService} so those can stay
 * purely reactive, calling into this blocking layer via
 * {@code Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())}.
 */
@Transactional
@Service
@RequiredArgsConstructor
public class SessionPersistenceService {

    private final SessionRepository sessionRepository;
    private final MoveRepository moveRepository;

    public SessionEntity createSession(SessionEntity entity) {
        return sessionRepository.save(entity);
    }

    /**
     * Looks up a session by ID.
     *
     * @throws SessionNotFoundException if {@code sessionId} does not exist
     */
    @Transactional(readOnly = true)
    public SessionEntity findSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    /** Marks a session as errored after its simulation loop fails unrecoverably. Silently no-ops if the session is gone. */
    public void markSessionError(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(entity -> {
            entity.setStatus(SessionStatus.ERROR);
            entity.setUpdatedAt(Instant.now());
            sessionRepository.save(entity);
        });
    }

    /** Returns a session's recorded moves, oldest first. */
    @Transactional(readOnly = true)
    public List<MoveEntity> findMoves(String sessionId) {
        return moveRepository.findBySessionIdOrderByMoveNumberAsc(sessionId);
    }

    /** Records a move and updates the owning session's status/winner in the same transaction. */
    public void recordMove(MoveEntity move, SessionStatus status, String winner) {
        moveRepository.save(move);
        sessionRepository.findById(move.getSessionId()).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setWinner(winner);
            entity.setUpdatedAt(Instant.now());
            sessionRepository.save(entity);
        });
    }
}
