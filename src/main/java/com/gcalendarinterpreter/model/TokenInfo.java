package com.gcalendarinterpreter.model;

import java.util.regex.Pattern;

public class TokenInfo {

    public final Pattern regex;
    public final int token;

    public TokenInfo(Pattern regex, int token) {
        super();
        this.regex = regex;
        this.token = token;
    }
}