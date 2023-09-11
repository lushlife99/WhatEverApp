package com.example.whateverApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationBody {
    CHAT("새로운 채팅이 도착했습니다."),
    STARTED_WORK("심부름이 수락되었습니다. 진행상황을 확인해보세요"),
    FINISHED_WORK("헬퍼가 심부름을 완료했어요. 완료된 심부름을 마지막으로 검토해주세요"),
    REWARDED_WORK("심부름이 최종 검토되었어요. 리뷰를 써주세요 "),
    REVIEW_UPLOADED("리뷰가 등록되었어요. 리뷰를 확인해보세요"),
    EXECUTED_REPORT("신고가 처리되었어요. 결과를 확인해보세요"),
    WORK_DELETED("마감기한이 지나 심부름이 삭제되었어요."),
    NON_FINISHED_WORK_DELETED("3일동안 일이 진행되지 않아서 일이 삭제되었어요."),

    ;

    private final String detail;
}
