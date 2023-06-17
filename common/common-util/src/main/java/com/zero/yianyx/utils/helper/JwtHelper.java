package com.zero.yianyx.utils.helper;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;
import java.util.Date;

/**
 * @description: JWT工具类
 * JWT（Json Web Token）是为了在网络应用环境间传递声明而执行的一种基于JSON的开放标准。
 * JWT的声明一般被用来在身份提供者和服务提供者间传递被认证的用户身份信息，以便于从资源服务器获取资源。比如用在用户登录上
 * JWT最重要的作用就是对 token信息的防伪作用。
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */

public class JwtHelper {

    /**
     * token过期时间：24小时
     */
    private  static long tokenExpiration = 24*60*60*1000;
    /**
     * 密钥
     */
    private static String tokenSignKey = "yianyx";

    /**
     * 根据userId + userName 生成token字符串
     * @param userId
     * @param userName
     * @return
     */
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("yianyx-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        //根据密钥进行解码
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
        // return 1L;
    }

    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)){
            return "";
        }

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }

    public static void removeToken(String token) {
        //jwttoken无需删除，客户端扔掉即可。
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(7L, "admin");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUserName(token));
    }
}
