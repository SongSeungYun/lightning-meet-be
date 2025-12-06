package com.example.lightning_meet_be.domain.user.controller

import com.example.lightning_meet_be.domain.user.dto.*
import com.example.lightning_meet_be.domain.user.service.UserService
import com.example.lightning_meet_be.global.response.ResponseUtils
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) =
        ResponseUtils.success(data = userService.getUser(id))

    @GetMapping
    fun getAllUsers() =
        ResponseUtils.success(data = userService.getAllUsers())

    @PostMapping
    fun createUser(@RequestBody req: UserCreateRequest) =
        ResponseUtils.success(data = userService.createUser(req))

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody req: UserUpdateRequest) =
        ResponseUtils.success(data = userService.updateUser(id, req))

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) =
        ResponseUtils.success(message = "deleted").also {
            userService.deleteUser(id)
        }
}
