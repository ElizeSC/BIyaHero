package com.biyahero.service; // This must match the folder: src/main/java/com.biyahero.service/

import com.biyahero.util.DBUtil;
import java.sql.Connection;

public class AuthService {
    public boolean authenticate(String user, String pass) {
        return DBUtil.testConnection(user, pass);
    }
}