package com.tictactoe.gameengine.controller;

import com.tictactoe.gameengine.domain.Game;
import com.tictactoe.gameengine.domain.Player;
import com.tictactoe.gameengine.exception.GameAlreadyExistsException;
import com.tictactoe.gameengine.exception.GameNotFoundException;
import com.tictactoe.gameengine.exception.InvalidMoveException;
import com.tictactoe.gameengine.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test: verifies HTTP status codes, JSON shape, and that
 * {@link com.tictactoe.gameengine.exception.GlobalExceptionHandler} maps each
 * domain exception to the status code the assignment calls for.
 */
@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Test
    void createGameReturns201WithTheNewGameState() throws Exception {
        when(gameService.createGame("g1")).thenReturn(Game.start("g1"));

        mockMvc.perform(post("/games/{gameId}", "g1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value("g1"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void creatingAGameThatAlreadyExistsReturns409() throws Exception {
        when(gameService.createGame("g1")).thenThrow(new GameAlreadyExistsException("g1"));

        mockMvc.perform(post("/games/{gameId}", "g1"))
                .andExpect(status().isConflict());
    }

    @Test
    void moveReturnsTheUpdatedGameState() throws Exception {
        Game afterMove = Game.start("g1").applyMove(Player.X, 0, 0);
        when(gameService.makeMove(eq("g1"), any())).thenReturn(afterMove);

        mockMvc.perform(post("/games/{gameId}/move", "g1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player": "X", "row": 0, "col": 0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board[0][0]").value("X"));
    }

    @Test
    void moveWithValuesThatFailValidationReturns400() throws Exception {
        mockMvc.perform(post("/games/{gameId}/move", "g1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player": "Z", "row": 9, "col": 0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveWithUnreadableJsonReturns400AsProblemDetail() throws Exception {
        mockMvc.perform(post("/games/{gameId}/move", "g1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void anInvalidMoveOnAnExistingGameReturns400() throws Exception {
        when(gameService.makeMove(eq("g1"), any()))
                .thenThrow(new InvalidMoveException("Cell (0,0) is already occupied"));

        mockMvc.perform(post("/games/{gameId}/move", "g1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player": "X", "row": 0, "col": 0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGameReturns404WhenTheGameIsUnknown() throws Exception {
        when(gameService.getGame("missing")).thenThrow(new GameNotFoundException("missing"));

        mockMvc.perform(get("/games/{gameId}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGameReturnsTheCurrentBoardAndStatus() throws Exception {
        when(gameService.getGame("g1")).thenReturn(Game.start("g1"));

        mockMvc.perform(get("/games/{gameId}", "g1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board").isArray())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
