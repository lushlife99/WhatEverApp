package com.example.whateverApp.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 유효하지 않습니다"),
    MISMATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰의 유저 정보가 일치하지 않습니다"),
    ALREADY_PROCEED_WORK(HttpStatus.BAD_REQUEST, "이미 접수됐거나 완료된 심부름입니다"),
    ALREADY_EXECUTED_REPORT(HttpStatus.BAD_REQUEST, "이미 처리된 신고입니다"),
    ALREADY_FINISHED_WORK(HttpStatus.BAD_REQUEST, "이미 종료된 심부름입니다"),
    LACK_REWORD(HttpStatus.BAD_REQUEST, "리워드가 충분하지 않습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 접근입니다"),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "잘못된 위치 정보입니다"),

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰입니다"),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "현재 내 계정 정보가 존재하지 않습니다"),
    UNAUTHORIZED_ADMIN(HttpStatus.UNAUTHORIZED, "관리자가 아닙니다"),
    UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰 형식입니다"),


    /* 403 FORBIDDEN : 권한이 없는 사용자 */
    TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "만료된 토큰입니다"),
    JWT_CLAIM_EMPTY(HttpStatus.BAD_REQUEST, "Jwt empty"),

    /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "로그아웃 된 사용자입니다"),
    WORK_NOT_FOUND(HttpStatus.NOT_FOUND, "심부름 정보를 찾을 수 없습니다"),
    HELPERLOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "헬퍼 위치정보를 찾을 수 없습니다"),
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다"),

    /* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "데이터가 이미 존재합니다"),
    DUPLICATE_USER(HttpStatus.CONFLICT, "존재하는 userId 입니다"),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이미 작성한 Review 입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String detail;
}
