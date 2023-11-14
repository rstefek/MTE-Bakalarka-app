package info.stefkovi.studium.mte_bakalarka.helpers;

import com.auth0.android.jwt.JWT;

public class JwtHelper {

    public static boolean isTokenExpired(String token) {
        JWT jwt = new JWT(token);
        return jwt.isExpired(3);
    }

    public static int getUserId(String token) {
        JWT jwt = new JWT(token);
        return jwt.getClaim("id").asInt();
    }

}
