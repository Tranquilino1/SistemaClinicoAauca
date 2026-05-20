package com.clinica.aauca;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        System.out.println("admin123: " + BCrypt.hashpw("admin123", BCrypt.gensalt(12)));
        System.out.println("medico123: " + BCrypt.hashpw("medico123", BCrypt.gensalt(12)));
        System.out.println("recep123: " + BCrypt.hashpw("recep123", BCrypt.gensalt(12)));
    }
}
