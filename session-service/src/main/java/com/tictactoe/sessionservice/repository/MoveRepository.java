package com.tictactoe.sessionservice.repository;

import com.tictactoe.sessionservice.model.MoveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<MoveEntity, Long> {
    List<MoveEntity> findBySessionIdOrderByMoveNumberAsc(String sessionId);
}
