package com.factseekerbackend.domain.youtube.controller.dto.response;

public record PlayerOptionsDto(boolean autoplay, boolean mute) {
    public static PlayerOptionsDto from() {
        return new PlayerOptionsDto(false,true);
    }
}
