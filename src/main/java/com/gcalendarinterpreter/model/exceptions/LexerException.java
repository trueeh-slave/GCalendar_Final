package com.gcalendarinterpreter.model.exceptions;

public class LexerException extends Exception {
    private int position;
    private String lexeme;

    public LexerException(String message, int position, String lexeme) {
        super(message + " at position " + position + " with lexeme: " + lexeme);
        this.position = position;
        this.lexeme = lexeme;
    }

    public int getPosition() {
        return position;
    }

    public String getLexeme() {
        return lexeme;
    }
}