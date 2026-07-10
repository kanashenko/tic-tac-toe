package com.tictactoe.sessionservice.mapper;

import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.client.dto.GameEngineStatus;
import com.tictactoe.sessionservice.dto.MoveDto;
import com.tictactoe.sessionservice.dto.SessionResponse;
import com.tictactoe.sessionservice.model.MoveEntity;
import com.tictactoe.sessionservice.model.SessionEntity;
import com.tictactoe.sessionservice.model.SessionStatus;
import com.tictactoe.sessionservice.service.MoveGenerator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Maps between JPA entities, the Game Engine's client DTOs, and this service's API DTOs. */
@Mapper(componentModel = "spring")
public interface SessionMapper {

    /**
     * Builds a new {@link SessionEntity} for a freshly created session. The
     * backing game isn't created until simulation starts, so a new session
     * begins {@code IN_PROGRESS} with no winner.
     */
    @Mapping(target = "id", source = "sessionId")
    @Mapping(target = "status", expression = "java(com.tictactoe.sessionservice.model.SessionStatus.IN_PROGRESS)")
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    SessionEntity toNewEntity(String sessionId);

    /** Combines a session's stored record, its move history, and the Game Engine's live state into one API response. */
    @Mapping(target = "sessionId", source = "entity.id")
    @Mapping(target = "status", source = "liveState.status")
    @Mapping(target = "winner", source = "liveState.winner")
    @Mapping(target = "board", source = "liveState.board")
    @Mapping(target = "moveHistory", source = "moves")
    SessionResponse toSessionResponse(SessionEntity entity, List<MoveEntity> moves, GameEngineResponse liveState);

    /** Builds the WebSocket push event for a single move just played. */
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "status", source = "updated.status")
    @Mapping(target = "winner", source = "updated.winner")
    @Mapping(target = "board", source = "updated.board")
    @Mapping(target = "moveHistory", expression = "java(java.util.List.of(toDto(latestMove)))")
    SessionResponse toEvent(String sessionId, MoveEntity latestMove, GameEngineResponse updated);

    @Mapping(source = "rowIndex", target = "row")
    @Mapping(source = "colIndex", target = "col")
    MoveDto toDto(MoveEntity entity);

    List<MoveDto> toDtoList(List<MoveEntity> entities);

    /** Builds the entity to persist for a move about to be recorded. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "moveNumber", source = "moveNumber")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "rowIndex", source = "cell.row")
    @Mapping(target = "colIndex", source = "cell.col")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    MoveEntity toEntity(String sessionId, int moveNumber, String player, MoveGenerator.Cell cell);

    SessionStatus toSessionStatus(GameEngineStatus status);

    /**
     * Builds the response for a session whose game has not been initialized yet
     * (created but not simulated): status and winner come from the local
     * record, and the board is empty since the Game Engine has no game to
     * report.
     */
    default SessionResponse toPendingResponse(SessionEntity entity, List<MoveEntity> moves) {
        return new SessionResponse(entity.getId(), entity.getStatus(), entity.getWinner(), emptyBoard(), toDtoList(moves));
    }

    /** A 3x3 board of empty cells ('-'), matching the Game Engine's empty-cell representation. */
    default List<List<Character>> emptyBoard() {
        List<Character> emptyRow = List.of('-', '-', '-');
        return List.of(emptyRow, emptyRow, emptyRow);
    }

    /** Builds the WebSocket push event for a simulation that failed unrecoverably. */
    default SessionResponse toErrorEvent(String sessionId, GameEngineResponse lastKnown) {
        List<List<Character>> board = lastKnown != null ? lastKnown.board() : List.of();
        return new SessionResponse(sessionId, SessionStatus.ERROR, null, board, List.of());
    }
}
