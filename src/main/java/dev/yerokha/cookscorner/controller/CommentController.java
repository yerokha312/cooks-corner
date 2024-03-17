package dev.yerokha.cookscorner.controller;

import dev.yerokha.cookscorner.dto.Comment;
import dev.yerokha.cookscorner.dto.CreateCommentRequest;
import dev.yerokha.cookscorner.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            summary = "Registration", description = "Create a new user account",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Registration success"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "409", description = "Username or email already taken")
            }
    )
    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody CreateCommentRequest request,
                                              Authentication authentication) {

        return new ResponseEntity<>(
                commentService.createComment(request, getUserIdFromAuthToken(authentication)),
                HttpStatus.CREATED);
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<Page<Comment>> getComments(@PathVariable Long objectId,
                                                     @RequestParam(required = false) Map<String, String> params,
                                                     Authentication authentication) {
        return ResponseEntity.ok(commentService.getComments(
                objectId,
                params,
                getUserIdFromAuthToken(authentication)));
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<Page<Comment>> getReplies(@PathVariable Long parentId,
                                                    @RequestParam(required = false) Map<String, String> params,
                                                    Authentication authentication) {
        return ResponseEntity.ok(commentService.getReplies(
                parentId,
                params,
                getUserIdFromAuthToken(authentication)));
    }
}
