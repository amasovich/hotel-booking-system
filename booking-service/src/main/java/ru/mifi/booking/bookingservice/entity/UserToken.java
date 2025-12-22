package ru.mifi.booking.bookingservice.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private Instant createdAt;

    protected UserToken() { }

    public UserToken(Long id, String token, User user, Instant createdAt) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.createdAt = createdAt;
    }

    public String getToken() { return token; }
    public User getUser() { return user; }
}
