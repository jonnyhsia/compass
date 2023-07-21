package com.arch.jonnyhsia.compass.sample;

public class RouteExtras {
    public static final int LOGIN = 0x0001;
    public static final int MEMBER = 0x0010;
    public static final int MEMBER_AND_LOGIN = LOGIN | MEMBER;

    public static boolean loginRequired(int extras) {
        return (extras & LOGIN) == LOGIN;
    }

    public static boolean memberRequired(int extras) {
        return (extras & MEMBER) == MEMBER;
    }
}
