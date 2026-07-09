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

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "id", source = "sessionId")
    @Mapping(target = "status", source = "engineResponse.status")
    @Mapping(target = "winner", source = "engineResponse.winner")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    SessionEntity toNewEntity(String sessionId, GameEngineResponse engineResponse);

    @Mapping(target = "sessionId", source = "entity.id")
    @Mapping(target = "status", source = "liveState.status")
    @Mapping(target = "winner", source = "liveState.winner")
    @Mapping(target = "board", source = "liveState.board")
    @Mapping(target = "moveHistory", source = "moves")
    SessionResponse toSessionResponse(SessionEntity entity, List<MoveEntity> moves, GameEngineResponse liveState);

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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "moveNumber", source = "moveNumber")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "rowIndex", source = "cell.row")
    @Mapping(target = "colIndex", source = "cell.col")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    MoveEntity toEntity(String sessionId, int moveNumber, String player, MoveGenerator.Cell cell);

    SessionStatus toSessionStatus(GameEngineStatus status);

    default SessionResponse toErrorEvent(String sessionId, GameEngineResponse lastKnown) {
        List<List<Character>> board = lastKnown != null ? lastKnown.board() : List.of();
        return new SessionResponse(sessionId, SessionStatus.ERROR, null, board, List.of());
    }
}
