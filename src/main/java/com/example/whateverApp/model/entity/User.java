package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.document.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity
 * bankAccount -> 계좌번호. 금융결제원의 오픈 api사용을 허가받는 절차가 지금 현재 상황으론 거의 불가능하므로 테스트용 농협은행 어카운트 계좌를 default로 사용.
 */

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
    private Integer reward = 1000000;
    private Long avgReactTime = 1000000000L;//평균 첫 응답 속도
    @OneToMany(mappedBy = "customer")
    private List<Work> purchaseList = new ArrayList<>();
    @OneToMany(mappedBy = "helper")
    private List<Work> sellList = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Alarm> alarmList = new ArrayList<>();
    @OneToMany(mappedBy = "reportUser")
    private List<Report> reportList = new ArrayList<>();
    @OneToMany(mappedBy = "reportedUser")
    private List<Report> reportedList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Review> reviewList = new ArrayList<>();


    private UUID imageFileName;
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    private boolean proceedingWork = false;

    private String refreshToken;

    private Double latitude = 0.0;

    private Double longitude = 0.0;
    private String notificationToken = "";

    private Boolean notification = true;
    private Long bankAccount = 3020000008694L;

    @Enumerated(EnumType.ORDINAL)
    private AccountStatus accountStatus;
    private LocalDateTime accountReleaseTime = LocalDateTime.now();

    @OneToOne
    private Report punishingDetail;

    public User updateUserInfo(UserDto user){
        this.name = user.getName();
        this.introduce = user.getIntroduce();
        this.bankAccount = user.getBankAccount();
        return this;
    }

    public User updateLocation(Location location){
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        return this;
    }

    public List<Review> addReview(Review review){
        this.getReviewList().add(review);
        return this.reviewList;
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
        if(accountStatus.equals(AccountStatus.BAN) || accountStatus.equals(AccountStatus.PERMANENT_BAN))
            return false;

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
