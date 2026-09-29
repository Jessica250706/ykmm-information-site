package com.xq.service.impl;

import com.xq.constant.JwtClaimsConstant;
import com.xq.properties.JwtProperties;
import com.xq.service.TokenService;
import com.xq.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtProperties jwtProperties;

    @Override
    public String createToken(Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.ROLE, role);
        return JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims);
    }
}
