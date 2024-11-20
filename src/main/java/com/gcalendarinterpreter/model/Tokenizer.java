package com.gcalendarinterpreter.model;

import com.gcalendarinterpreter.model.exceptions.LexerException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private List<TokenInfo> tokenInfos;
    private List<Token> tokens;

    public Tokenizer() {
        tokenInfos = new ArrayList<>();
        tokens = new ArrayList<>();
    }

    public void add(String regex, int token) {
        tokenInfos.add(new TokenInfo(Pattern.compile("^(" + regex + ")"), token));
    }

    public void tokenize(String str) throws LexerException {
        String s = str.trim();
        tokens.clear();

        int totalLength = s.length();
        int position = 0;

        while (!s.equals("")) {
            boolean match = false;
            for (TokenInfo info : tokenInfos) {
                Matcher m = info.regex.matcher(s);
                if (m.find()) {
                    match = true;
                    String tok = m.group().trim();
                    tokens.add(new Token(info.token, tok, position));
                    position += tok.length();
                    s = m.replaceFirst("").trim();
                    break;
                }
            }

            if (!match) {
                throw new LexerException("Unexpected character in input", position, s.substring(0, Math.min(s.length(), 10)));
            }
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private class TokenInfo {
        public final Pattern regex;
        public final int token;

        public TokenInfo(Pattern regex, int token) {
            this.regex = regex;
            this.token = token;
        }
    }
}