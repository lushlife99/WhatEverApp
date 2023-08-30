package com.example.whateverApp.model;

/**
 * USING -> 이용할 수 있는 계정
 * WILL_BAN -> 정지를 시켜야 하지만 만약 심부름이 진행중이라면 Status가 Using -> WILL_BAN으로 바뀐다.
 * 이 상태에서는 심부름을 더이상 추가적으로 진행할 수 없고, 만약 진행중인 심부름들이 완료된다면 BAN으로 바뀐다.
 * BAN -> 정지 당한 상태.
 * PERMANENT_BAN -> 영구 정지 당한 상태.
 */
public enum AccountStatus {
    USING,
    WILL_BAN,
    BAN,
    PERMANENT_BAN

}
