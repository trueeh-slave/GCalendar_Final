package com.gcalendarinterpreter.model;

public class Token {

    public static final int LAMBDA = 0;
    public static final int NEW = 1;
    public static final int EVENTO = 2;
    public static final int DOBLE_DOT = 3;
    public static final int TITULO = 4;
    public static final int FECHAINICIO = 5;
    public static final int FECHAFIN = 6;
    public static final int UBICACION = 7;
    public static final int DESCRIPCION = 8;
    public static final int CORREO = 15;
    public static final int COLOR = 9;
    public static final int STRING = 10;
    public static final int DATE = 11;
    public static final int HOUR = 12;
    public static final int COMMENT = 13;
    public static final int SEPARATOR = 14;
    public static final int STRING_CORREO = 16;


    public final int token;
    public final String lexeme;
    public final int pos;

    public Token(int token, String sequence, int pos) {
        super();
        this.token = token;
        this.lexeme = sequence;
        this.pos = pos;
    }

    public Token clone(){
        return new Token(this.token, this.lexeme, this.pos);
    }

    @Override
    public String toString() {
        return "Token{" +
                "token=" + token +
                ", lexeme='" + lexeme + '\'' +
                ", pos=" + pos +
                '}';
    }

}