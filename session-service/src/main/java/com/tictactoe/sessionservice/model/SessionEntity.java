package com.tictactoe.sessionservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class SessionEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private String winner;

    private Instant createdAt;

    private Instant updatedAt;
}
