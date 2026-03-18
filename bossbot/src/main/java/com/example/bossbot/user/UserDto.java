package com.example.bossbot.user;

public record UserDto(
        Long id,
        String username

) {
    public UserDto (User user){
        this(
                user.getId(),
                user.getUsername());
    }
}