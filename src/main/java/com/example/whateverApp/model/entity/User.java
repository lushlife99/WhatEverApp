package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.document.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;
    private String password;
    private String name;
    private String introduce;
    private Double rating;
    private Integer reward;
    private Long avgReactTime;//평균 첫 응답 속도
    @OneToMany(mappedBy = "customer")
    private List<Work> purchaseList = new ArrayList<>();
    @OneToMany(mappedBy = "helper")
    private List<Work> sellList = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Alarm> alarmList = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Report> reportList = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Review> reviewList = new ArrayList<>();

    private UUID imageFileName;
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    private String refreshToken;

    private Double latitude = 0.0;

    private Double longitude = 0.0;
    private String notificationToken;

    private Boolean notification = true;


    public User updateUserInfo(UserDto user){
        this.password = user.getPassword();
        this.name = user.getName();
        this.introduce = user.getIntroduce();
        return this;
    }

    public User updateLocation(Location location){
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        return this;
    }
    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
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
