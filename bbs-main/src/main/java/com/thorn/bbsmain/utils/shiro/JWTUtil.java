package com.thorn.bbsmain.utils.shiro;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Slf4j
public class JWTUtil {

    public static final int REFRESHLIMIT = 60 * 1000;
    /**
     * 过期时间60分钟
     */
    private static final long EXPIRE_TIME = 60 * 60 * 1000;

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, String email, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("email", email)
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static String getEmail(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("email").asString();
        } catch (JWTDecodeException e) {
            log.error("token解析失败:" + e.getMessage());
            return null;
        }
    }

    /**
     * 生成签名,5min后过期
     *
     * @param email  邮箱
     * @param secret 用户的密码
     * @return 加密的token
     */
    public static String sign(String email, String secret) {
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            // 附带email信息
            return JWT.create()
                    .withClaim("email", email)
                    .withExpiresAt(date)
                    .sign(algorithm);

        } catch (UnsupportedEncodingException e) {
            log.error("不支持生成token" + e.getMessage());
            return null;
        }
    }

    public static boolean isTokenNeedRefresh(String jwtToken) {
        long expires = JWT.decode(jwtToken).getExpiresAt().getTime();
        long now = System.currentTimeMillis();
        if (expires > now && expires - now < REFRESHLIMIT) {
            return true;
        }
        return false;
    }


    public static boolean isTokenExpired(String jwtToken) {
        return JWT.decode(jwtToken).getExpiresAt().before(new Date());
    }
}