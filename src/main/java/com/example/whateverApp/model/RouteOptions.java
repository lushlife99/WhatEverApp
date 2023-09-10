package com.example.whateverApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RouteOptions {
    CONVERSATION_VIEW("ConversationView"),
    NEARBY_WORK_VIEW("NearByWorkView"),
    FINISH_WORK_VIEW("FinishWorkView"),
    MY_REVIEW_VIEW("MyReviewView"),
    REPORT_VIEW("ReportView"),
    MAIN_VIEW("MainView")
    ;

    private final String detail;
}
