package com.tictactoe.sessionservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** JPA entity backing the {@code sessions} table (H2, in-memory). */
@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class SessionEntity {

    /** The session ID, which doubles as the Game Engine's game ID. */
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private String winner;

    private Instant createdAt;

    private Instant updatedAt;
}
