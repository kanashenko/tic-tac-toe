package com.tictactoe.sessionservice.repository;

import com.tictactoe.sessionservice.model.MoveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data repository for {@link MoveEntity}. */
public interface MoveRepository extends JpaRepository<MoveEntity, Long> {
    /** Returns a session's moves in the order they were played. */
    List<MoveEntity> findBySessionIdOrderByMoveNumberAsc(String sessionId);
}
