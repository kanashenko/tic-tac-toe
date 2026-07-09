package com.tictactoe.sessionservice.mapper;

import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.client.dto.GameEngineStatus;
import com.tictactoe.sessionservice.dto.MoveDto;
import com.tictactoe.sessionservice.dto.SessionResponse;
import com.tictactoe.sessionservice.model.MoveEntity;
import com.tictactoe.sessionservice.model.SessionEntity;
import com.tictactoe.sessionservice.model.SessionStatus;
import com.tictactoe.sessionservice.service.MoveGenerator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T22:50:01+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class SessionMapperImpl implements SessionMapper {

    @Override
    public SessionEntity toNewEntity(String sessionId, GameEngineResponse engineResponse) {
        if ( sessionId == null && engineResponse == null ) {
            return null;
        }

        SessionEntity.SessionEntityBuilder sessionEntity = SessionEntity.builder();

        if ( engineResponse != null ) {
            sessionEntity.status( toSessionStatus( engineResponse.status() ) );
            sessionEntity.winner( engineResponse.winner() );
        }
        sessionEntity.id( sessionId );
        sessionEntity.createdAt( java.time.Instant.now() );
        sessionEntity.updatedAt( java.time.Instant.now() );

        return sessionEntity.build();
    }

    @Override
    public SessionResponse toSessionResponse(SessionEntity entity, List<MoveEntity> moves, GameEngineResponse liveState) {
        if ( entity == null && moves == null && liveState == null ) {
            return null;
        }

        String sessionId = null;
        if ( entity != null ) {
            sessionId = entity.getId();
        }
        SessionStatus status = null;
        String winner = null;
        List<List<Character>> board = null;
        if ( liveState != null ) {
            status = toSessionStatus( liveState.status() );
            winner = liveState.winner();
            List<List<Character>> list = liveState.board();
            if ( list != null ) {
                board = new ArrayList<List<Character>>( list );
            }
        }
        List<MoveDto> moveHistory = null;
        moveHistory = toDtoList( moves );

        SessionResponse sessionResponse = new SessionResponse( sessionId, status, winner, board, moveHistory );

        return sessionResponse;
    }

    @Override
    public SessionResponse toEvent(String sessionId, MoveEntity latestMove, GameEngineResponse updated) {
        if ( sessionId == null && latestMove == null && updated == null ) {
            return null;
        }

        SessionStatus status = null;
        String winner = null;
        List<List<Character>> board = null;
        if ( updated != null ) {
            status = toSessionStatus( updated.status() );
            winner = updated.winner();
            List<List<Character>> list = updated.board();
            if ( list != null ) {
                board = new ArrayList<List<Character>>( list );
            }
        }
        String sessionId1 = null;
        sessionId1 = sessionId;

        List<MoveDto> moveHistory = java.util.List.of(toDto(latestMove));

        SessionResponse sessionResponse = new SessionResponse( sessionId1, status, winner, board, moveHistory );

        return sessionResponse;
    }

    @Override
    public MoveDto toDto(MoveEntity entity) {
        if ( entity == null ) {
            return null;
        }

        int row = 0;
        int col = 0;
        int moveNumber = 0;
        String player = null;

        row = entity.getRowIndex();
        col = entity.getColIndex();
        moveNumber = entity.getMoveNumber();
        player = entity.getPlayer();

        MoveDto moveDto = new MoveDto( moveNumber, player, row, col );

        return moveDto;
    }

    @Override
    public List<MoveDto> toDtoList(List<MoveEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<MoveDto> list = new ArrayList<MoveDto>( entities.size() );
        for ( MoveEntity moveEntity : entities ) {
            list.add( toDto( moveEntity ) );
        }

        return list;
    }

    @Override
    public MoveEntity toEntity(String sessionId, int moveNumber, String player, MoveGenerator.Cell cell) {
        if ( sessionId == null && player == null && cell == null ) {
            return null;
        }

        MoveEntity.MoveEntityBuilder moveEntity = MoveEntity.builder();

        if ( cell != null ) {
            moveEntity.rowIndex( cell.row() );
            moveEntity.colIndex( cell.col() );
        }
        moveEntity.sessionId( sessionId );
        moveEntity.moveNumber( moveNumber );
        moveEntity.player( player );
        moveEntity.createdAt( java.time.Instant.now() );

        return moveEntity.build();
    }

    @Override
    public SessionStatus toSessionStatus(GameEngineStatus status) {
        if ( status == null ) {
            return null;
        }

        SessionStatus sessionStatus;

        switch ( status ) {
            case IN_PROGRESS: sessionStatus = SessionStatus.IN_PROGRESS;
            break;
            case WIN: sessionStatus = SessionStatus.WIN;
            break;
            case DRAW: sessionStatus = SessionStatus.DRAW;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + status );
        }

        return sessionStatus;
    }
}
