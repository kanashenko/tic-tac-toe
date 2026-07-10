package com.tictactoe.sessionservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class SessionEntity {

    /** The session ID, which doubles as the Game Engine's game ID. */
    @Id
    private String id;

    /** Mutated in place during simulation, so it keeps a setter. */
    @Setter
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    /** Mutated in place during simulation, so it keeps a setter. */
    @Setter
    private String winner;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
