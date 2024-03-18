package dev.yerokha.cookscorner.controller;

import dev.yerokha.cookscorner.service.ActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.yerokha.cookscorner.service.TokenService.getUserIdFromAuthToken;

@RestController
@RequestMapping("/v1/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @Operation(summary = "Interaction", description = "Send code to interact with objects in app", tags = {"action", "put"},
            parameters = {
                    @Parameter(name = "actionId", description = "like(1), dislike(10), save(2) or remove(20)", in = ParameterIn.PATH),
                    @Parameter(name = "objectTypeId", description = "comment(1), recipe(2)", in = ParameterIn.PATH),
                    @Parameter(name = "objectId", description = "ID of the comment or recipe", in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Action is successful"),
                    @ApiResponse(responseCode = "400", description = "Wrong action id or type id is passed"),
                    @ApiResponse(responseCode = "401", description = "User is unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Object or user not found"),
            }
    )
    @PutMapping("/{actionId}/{objectTypeId}/{objectId}")
    public ResponseEntity<String> makeAction(
            @PathVariable byte actionId,
            @PathVariable byte objectTypeId,
            @PathVariable Long objectId,
            Authentication authentication) {

        actionService.interact(actionId, objectTypeId, objectId, getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("Action success");
    }
}


































