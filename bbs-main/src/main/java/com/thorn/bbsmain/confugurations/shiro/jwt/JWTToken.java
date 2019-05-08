package com.thorn.bbsmain.confugurations.shiro.jwt;

import lombok.ToString;
import org.apache.shiro.authc.AuthenticationToken;

@ToString
public class JWTToken implements AuthenticationToken {

    private String token;

    public JWTToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}