package com.example.whateverApp.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;


@Data
@Entity
public class LocationConnection {

    /**
     * 23/03/11 chan
     * Work와 Location과의 관계.
     *
     * Work는 rdbms에 저장되지만 Location은 Nosql에 저장됨.
     * Location은 1시간 이내 서비스일 때 5분단위로 저장됨.
     * 결론 : Location이 생성된다면 Work와 1대1 관계임.
     */
    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private Work work;
    private String helperLocationId;
}
