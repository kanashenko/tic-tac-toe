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

    private int moveNumber;

    private String player;

    @Column(name = "row_index")
    private int rowIndex;

    @Column(name = "col_index")
    private int colIndex;

    private Instant createdAt;
}
