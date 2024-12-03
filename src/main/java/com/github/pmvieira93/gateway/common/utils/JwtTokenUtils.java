package com.github.pmvieira93.gateway.common.utils;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class JwtTokenUtils {


    public static String search(final String key, final Map<String, Claim> claims) {
        String result = null;
        if (claims != null && !claims.isEmpty() && claims.containsKey(key)) {
            result = claims.get(key).asString();
        }
        return result;
    }

    public static String search(final String key, final DecodedJWT jwt){
        String result = null;
        if (jwt != null) {
            result = search(key, jwt.getClaims());
        }
        return result;
    }
}
