package com.ssafy.sns.controller;

import com.ssafy.sns.dto.clap.ClapResponseDto;
import com.ssafy.sns.dto.comment.CommentRequestDto;
import com.ssafy.sns.dto.comment.CommentResponseDto;
import com.ssafy.sns.dto.newsfeed.FeedListResponseDto;
import com.ssafy.sns.dto.newsfeed.FeedResponseDto;
import com.ssafy.sns.dto.newsfeed.IndoorRequestDto;
import com.ssafy.sns.dto.newsfeed.IndoorResponseDto;
import com.ssafy.sns.jwt.JwtService;
import com.ssafy.sns.service.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.util.HashMap;

import java.util.Map;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@CrossOrigin(origins = { "*" })
@RestController
@RequestMapping("/indoors")
public class IndoorController {

    public static final Logger logger = LoggerFactory.getLogger(IndoorController.class);
    private final IndoorServiceImpl indoorService;
    private final FeedClapServiceImpl feedClapService;
    private final JwtService jwtService;
    private final CommentServiceImpl commentService;
    private final CommentClapServiceImpl commentClapService;


    // 내가 쓴 게시글 불러오기
    @ApiOperation("해당 유저 작성한 꽃보다집 전체 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetId", value = "사용자 ID", required = true),
            @ApiImplicitParam(name = "startNum", value = "시작 페이지 번호", required = true)
    })
    @GetMapping(value = "/user/{targetId}", produces = "application/json; charset=utf8")
    public ResponseEntity<FeedListResponseDto> getFeedMyList(@PathVariable("targetId") Long targetId,
                                                             @RequestParam("startNum") int startNum,
                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        FeedListResponseDto feedListResponseDto = null;
        try {
            feedListResponseDto = indoorService.findMyList(
                    jwtService.findId(request.getHeader("Authorization")), targetId, startNum);
            logger.info("getFeedMyList = 꽃보다집 내 글 리스트 가져오기 : {}", startNum);
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("getFeedMyList - 꽃보다집 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(feedListResponseDto, status);
    }

    // 꽃보다집 게시글 불러오기
    @ApiOperation("꽃보다집 전체 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startNum", value = "시작 페이지 번호", required = true)
    })
    @GetMapping
    public ResponseEntity<FeedListResponseDto> getFeedList(@RequestParam("startNum") int startNum,
                                                           HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        FeedListResponseDto feedListResponseDto = null;
        try {
            feedListResponseDto = indoorService.readList(
                    jwtService.findId(request.getHeader("Authorization")), startNum);
            logger.info("getFeedList = 꽃보다집 글 리스트 가져오기 : {}", startNum);
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("getFeedList - 꽃보다집 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(feedListResponseDto, status);
    }

    @ApiOperation("꽃보다집 상세 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true)
    })
    @GetMapping(value = "/{feedId}", produces = "application/json; charset=utf8")
    public ResponseEntity<FeedResponseDto> getFeed(@PathVariable("feedId") Long feedId, HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        IndoorResponseDto indoorResponseDto = null;
        try {
            indoorResponseDto = (IndoorResponseDto) indoorService.read(
                    jwtService.findId(request.getHeader("Authorization")), feedId);
            logger.info("getFeed = 꽃보다집 글 가져오기 : {}", indoorResponseDto);
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("getFeed - 꽃보다집 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(indoorResponseDto, status);
    }

    @ApiOperation("꽃보다집 글 작성 (파일 첨부 제외)")
    @PostMapping
    public ResponseEntity<Long> postFeed(@RequestBody IndoorRequestDto indoorRequestDto, HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;
        Long result = null;
        try {
            result = indoorService.write(jwtService.findId(request.getHeader("Authorization")), indoorRequestDto);
            logger.info("postFeed - 꽃보다집 글 작성 : {}", indoorRequestDto);
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("postFeed - 꽃보다집 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(result, status);
    }

    @ApiOperation("꽃보다집 글 파일 첨부")
    @PostMapping("/{feedId}")
    public ResponseEntity<Void> postFiles(@PathVariable("feedId") Long feedId, @RequestPart(name = "file", required = false) MultipartFile file,
                                         HttpServletRequest request) throws IOException {
        HttpStatus status = HttpStatus.ACCEPTED;
        Long result = null;

        String token = request.getHeader("Authorization");
        Long userId = jwtService.findId(token);

        indoorService.uploadFiles(feedId, file);

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 글 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true),
            @ApiImplicitParam(name = "file", value = "첨부파일")
    })
    @PutMapping(value = "/{feedId}")
    public ResponseEntity<Long> putFeed(@PathVariable("feedId") Long feedId,
                                        @RequestBody IndoorRequestDto indoorRequestDto,
                                        HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        Long userId = jwtService.findId(request.getHeader("Authorization"));

        try {
            indoorService.modify(userId, feedId, indoorRequestDto);
            logger.info("putFeed - 꽃보다집 글 수정 : {}", indoorRequestDto);
            status = HttpStatus.OK;
        } catch(NoSuchElementException e) {
            logger.warn("putFeed - 꽃보다집 권한없는 사용자 : {}", userId);
            status = HttpStatus.UNAUTHORIZED;
        } catch (Exception e) {
            logger.warn("putFeed - 꽃보다집 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 글 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true)
    })
    @DeleteMapping(value = "{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable("feedId") Long feedId, HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        Long userId = jwtService.findId(request.getHeader("Authorization"));

        try {
            indoorService.delete(userId, feedId);
            status = HttpStatus.OK;
            logger.info("putFeed - 꽃보다집 글 수정 : {}", feedId);
        } catch (Exception e) {
            logger.warn("deleteFeed - 꽃보다집 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 박수 토글")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true)
    })
    @PostMapping(value = "/{feedId}/claps")
    public ResponseEntity<Void> postClap(@PathVariable("feedId") Long feedId, HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        // 박수 토글
        try {
            feedClapService.changeClap(jwtService.findId(request.getHeader("Authorization")), feedId);
            logger.info("postClap - 꽃보다집 박수 토글");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("postClap - 꽃보다집 박수 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 박수 명단 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true)
    })
    @GetMapping(value = "/{feedId}/claps")
    public ResponseEntity<ClapResponseDto> getClap(@PathVariable("feedId") Long feedId) {
        HttpStatus status = HttpStatus.ACCEPTED;
        Map<String, Object> resultMap = new HashMap<>();
        ClapResponseDto clapResponseDto = null;
        // 박수 불러오기
        try {
            clapResponseDto = feedClapService.clapUserList(feedId);
            logger.info("getClap - 꽃보다집 박수 조회");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("getClap - 꽃보다집 박수 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(clapResponseDto, status);
    }

    @ApiOperation("꽃보다집 댓글 작성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true)
    })
    @PostMapping(value = "/{feedId}/comments")
    public ResponseEntity<Void> postComment(@PathVariable("feedId") Long feedId,
                                            @RequestBody CommentRequestDto commentRequestDto,
                                            HttpServletRequest request) {

        HttpStatus status = HttpStatus.ACCEPTED;

        try {
            commentService.write(commentRequestDto, jwtService.findId(request.getHeader("Authorization")), feedId);
            logger.info("postComment - 꽃보다집 댓글 작성");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("postComment - 꽃보다집 댓글 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 댓글 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true),
            @ApiImplicitParam(name = "commentId", value = "댓글 번호", required = true)
    })
    @PutMapping(value = "/{feedId}/comments/{commentId}")
    public ResponseEntity<Void> putComment(@PathVariable("feedId") Long feedId,
                                             @PathVariable("commentId") Long commentId,
                                             @RequestBody CommentRequestDto commentRequestDto,
                                             HttpServletRequest request) {

        HttpStatus status = HttpStatus.ACCEPTED;

        try {
            commentService.modify(commentRequestDto, jwtService.findId(request.getHeader("Authorization")), feedId, commentId);
            logger.info("putComment - 꽃보다집 댓글 수정");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("puttComment - 꽃보다집 댓글 수정 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 댓글 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true),
            @ApiImplicitParam(name = "commentId", value = "댓글 번호", required = true)
    })
    @DeleteMapping(value = "/{feedId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("feedId") Long feedId,
                                           @PathVariable("commentId") Long commentId,
                                           HttpServletRequest request) {

        HttpStatus status = HttpStatus.ACCEPTED;

        try {
            commentService.remove(jwtService.findId(request.getHeader("Authorization")), feedId, commentId);
            logger.info("putComment - 꽃보다집 댓글 삭제");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("puttComment - 꽃보다집 댓글 삭제 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 댓글 리스트")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "feedId", value = "피드 번호", required = true),
            @ApiImplicitParam(name = "startNum", value = "시작 댓글", required = true)
    })
    @GetMapping(value = "/{feedId}/comments")
    public ResponseEntity<CommentResponseDto> getComment(@PathVariable("feedId") Long feedId,
                                                         @RequestParam("startNum") int startNum,
                                                         HttpServletRequest request) {

        HttpStatus status = HttpStatus.ACCEPTED;
        CommentResponseDto comments = null;
        try {
            comments = commentService.getList(jwtService.findId(request.getHeader("Authorization")), feedId, startNum);
            logger.info("getComment - 꽃보다집 댓글 리스트");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("getComment - 꽃보다집 댓글 리스트 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(comments, status);
    }

    @ApiOperation("꽃보다집 댓글 박수 토글")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "댓글 번호", required = true)
    })
    @PostMapping(value = "/comments/{commentId}/claps")
    public ResponseEntity<Void> postCommentClap(@PathVariable("commentId") Long commentId, HttpServletRequest request) {
        HttpStatus status = HttpStatus.ACCEPTED;

        // 박수 토글
        try {
            commentClapService.changeClap(jwtService.findId(request.getHeader("Authorization")), commentId);
            logger.info("postClap - 꽃보다집 댓글 박수 토글");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("postClap - 꽃보다집 댓글 박수 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(status);
    }

    @ApiOperation("꽃보다집 댓글 박수 명단 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "댓글 번호", required = true)
    })
    @GetMapping(value = "/comments/{commentId}/claps")
    public ResponseEntity<ClapResponseDto> getCommentClap(@PathVariable("commentId") Long commentId) {
        HttpStatus status = HttpStatus.ACCEPTED;
        Map<String, Object> resultMap = new HashMap<>();
        ClapResponseDto clapResponseDto = null;
        // 박수 불러오기
        try {
            clapResponseDto = commentClapService.clapUserList(commentId);
            logger.info("getClap - 꽃보다집 댓글 박수 조회");
            status = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("getClap - 꽃보다집 댓글 박수 에러 : {}", e.getMessage());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(clapResponseDto, status);
    }
}
