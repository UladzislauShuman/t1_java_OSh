package ru.t1.java.demo.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlackListResponseDto {
    private UUID clientId;
    private boolean isBlackListed;
}
