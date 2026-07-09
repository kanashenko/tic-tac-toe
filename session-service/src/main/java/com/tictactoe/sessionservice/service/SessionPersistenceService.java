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

@Transactional
@Service
@RequiredArgsConstructor
public class SessionPersistenceService {

    private final SessionRepository sessionRepository;
    private final MoveRepository moveRepository;

    public SessionEntity createSession(SessionEntity entity) {
        return sessionRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public SessionEntity findSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    public void markSessionError(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(entity -> {
            entity.setStatus(SessionStatus.ERROR);
            entity.setUpdatedAt(Instant.now());
            sessionRepository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public List<MoveEntity> findMoves(String sessionId) {
        return moveRepository.findBySessionIdOrderByMoveNumberAsc(sessionId);
    }

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