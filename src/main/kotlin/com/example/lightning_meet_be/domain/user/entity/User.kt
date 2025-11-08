package com.example.lightning_meet_be.domain.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val loginId: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var nickname: String,

    var region: String? = null,
    var interests: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
)

enum class Role { USER, ADMIN }
