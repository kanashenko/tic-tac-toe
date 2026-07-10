package com.tictactoe.sessionservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "moves", indexes = @Index(name = "idx_moves_session_id", columnList = "sessionId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MoveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    /** Zero-based order in which this move was played within its session. */
    private int moveNumber;

    /** "X" or "O". */
    private String player;

    @Column(name = "row_index")
    private int rowIndex;

    @Column(name = "col_index")
    private int colIndex;

    private Instant createdAt;
}
