package com.example.whateverApp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String password;
    private String username;
    private String introduce;
    private Float rating; //1~5사이 별점
    private Integer reward;
    private Integer avgReactTime;//평균 첫 응답 속도
    @OneToMany(mappedBy = "customer")
    private List<Work> purchaseList = new ArrayList<>();
    @OneToMany(mappedBy = "seller")
    private List<Work> sellList = new ArrayList<>();
    private UUID imageFileName;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
    /**
     * 23.03.11 - chan
     *
     * 서버에 사진파일 이름을 저장하고
     * 파일은 다른 디렉토리에 저장.
     * 사진파일 이름으로 매핑이 되기 때문에 이름 = pk값이 되므로 중복 X
     * UUID uuid = UUID.randomUUID(); -> 중복 방지.
     *
     * 로컬에서 개발할때는 내 pc 디렉토리에 파일이 저장되지만
     * 후에 서비스를 확장할경우 aws와 같은 외부 스토리지에 저장해야함
     */


}
