package dev.yerokha.cookscorner.controller;

import dev.yerokha.cookscorner.dto.Comment;
import dev.yerokha.cookscorner.dto.CreateCommentRequest;
import dev.yerokha.cookscorner.dto.UpdateCommentRequest;
import dev.yerokha.cookscorner.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.cookscorner.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Comment", description = "Controller for comments interaction")
@RestController
@RequestMapping("/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(
            summary = "Add a comment", description = "Create a new parent comment to Recipe (isReply = false) or " +
            "reply to existing comment or leave a new comment in existing branch (isReply = true)",
            tags = {"comment", "post"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comment success"),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Comment or recipe does not exist", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody CreateCommentRequest request,
                                              Authentication authentication) {

        return new ResponseEntity<>(
                commentService.createComment(request, getUserIdFromAuthToken(authentication)),
                HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get comments", description = "Get a paged list of parent comments",
            tags = {"comment", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Object does not exist", content = @Content)
            },
            parameters = @Parameter(
                    name = "objectId",
                    description = "ID of recipe. It's called object because as project grows " +
                            "there possibly can be another objects with comments",
                    required = true,
                    in = ParameterIn.PATH)
    )
    @GetMapping("/{objectId}")
    public ResponseEntity<Page<Comment>> getComments(@PathVariable Long objectId,
                                                     @RequestParam(required = false) Map<String, String> params,
                                                     Authentication authentication) {
        return ResponseEntity.ok(commentService.getComments(
                objectId,
                params,
                getUserIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "Get replies", description = "Get a paged list of comment replies (children comments)",
            tags = {"comment", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Comment does not exist", content = @Content)
            },
            parameters = @Parameter(
                    name = "parentId",
                    description = "ID of parent comment",
                    required = true,
                    in = ParameterIn.PATH)
    )
    @GetMapping("/{parentId}/replies")
    public ResponseEntity<Page<Comment>> getReplies(@PathVariable Long parentId,
                                                    @RequestParam(required = false) Map<String, String> params,
                                                    Authentication authentication) {
        return ResponseEntity.ok(commentService.getReplies(
                parentId,
                params,
                getUserIdFromAuthToken(authentication)));
    }

    @PutMapping
    public ResponseEntity<Comment> updateComment(@RequestBody UpdateCommentRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.ok(commentService.updateComment(request, getUserIdFromAuthToken(authentication)));
    }
}
