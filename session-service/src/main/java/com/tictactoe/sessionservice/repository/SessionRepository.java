package com.tictactoe.sessionservice.repository;

import com.tictactoe.sessionservice.model.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for {@link SessionEntity}. */
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
}
