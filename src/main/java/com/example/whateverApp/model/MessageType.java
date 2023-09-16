package com.example.whateverApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {
    Chat("Chat"),
    Work("Work"),
    Conversation("Conversation"),
    Card("Card"),
    SetConvSeenCount("SetConvSeenCount"),
    RouteType("RouteType"),
    LogOut("LogOut")
    ;
    private final String detail;
}
