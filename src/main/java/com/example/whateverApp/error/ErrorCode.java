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
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "심부름 완료 장소에서 500m 이내에서만 심부름을 완료할 수 있습니다"),
    OTHER_WORK_IS_PROCEEDING(HttpStatus.BAD_REQUEST, "이미 진행되고 있는 일이 있습니다"),
    UNVERIFIED_REWARD_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 다릅니다"),
    REFUND_AMOUNT_IS_DIFFERENT(HttpStatus.BAD_REQUEST, "환불가능 금액과 결제했던 금액이 일치하지 않습니다"),
    AMOUNT_IS_MORE_THAN_REWARD(HttpStatus.BAD_REQUEST, "출금 금액이 reward 보다 많습니다"),
    PARTICIPATOR_ACCOUNT_WILL_BAN(HttpStatus.BAD_REQUEST, "상대방이 대화할 수 없는 상태입니다"),
    ALREADY_REPORT_THIS_WORK(HttpStatus.BAD_REQUEST, "이미 신고했던 심부름입니다. 신고내역을 확인해주세요"),
    CANT_FINISH_REPORTED_WORK(HttpStatus.BAD_REQUEST, "신고된 심부름을 검증할 수 없습니다"),
    ALREADY_REWARD_TO_HELPER(HttpStatus.BAD_REQUEST, "이미 보상이 전송되었으므로 환불할 수 없습니다"),

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "현재 내 계정 정보가 존재하지 않습니다"),
    /* 403 FORBIDDEN : 권한이 없는 사용자 */

    WILL_BANNED_ACCOUNT(HttpStatus.FORBIDDEN, "현재 상태로는 서비스 이용이 제한됩니다"),
    PERMANENT_BANNED_ACCOUNT(HttpStatus.FORBIDDEN, "영구 정지 당한 계정입니다"),
    BANNED_ACCOUNT(HttpStatus.FORBIDDEN, "정지 당한 계정입니다"),
    MISMATCH_PASSWORD(HttpStatus.FORBIDDEN, "비밀번호가 일치하지 않습니다"),
    UNAUTHORIZED_ADMIN(HttpStatus.FORBIDDEN, "관리자가 아닙니다"),

    /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "로그아웃 된 사용자입니다"),
    WORK_NOT_FOUND(HttpStatus.NOT_FOUND, "심부름 정보를 찾을 수 없습니다"),
    HELPER_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "헬퍼 위치정보를 찾을 수 없습니다"),
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다"),
    /* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "데이터가 이미 존재합니다"),
    DUPLICATE_USER(HttpStatus.CONFLICT, "존재하는 userId 입니다"),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이미 작성한 Review 입니다."),
    FINISHED_CONVERSATION(HttpStatus.CONFLICT, "심부름이 종료되어서 대화창을 불러올 수 없습니다"),
    /* 500 */
    TRANSFER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "송금 오류"),
    ;

    private final HttpStatus httpStatus;
    private final String detail;
}
