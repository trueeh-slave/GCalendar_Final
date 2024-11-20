package com.gcalendarinterpreter.parser;

import com.gcalendarinterpreter.model.Token;
import com.gcalendarinterpreter.parser.exceptions.ParserException;

import java.util.LinkedList;
import java.util.List;

public class Parser {

    private LinkedList<Token> tokens;
    private Token lookahead;
    private StringBuilder parsingSteps;

    public Parser() {
        parsingSteps = new StringBuilder();
    }

    public String getParsingSteps() {
        return parsingSteps.toString();
    }

    public void parse(List<Token> tokens) {
        this.tokens = new LinkedList<>();
        for (Token token : tokens) {
            this.tokens.add(token.clone());
        }

        lookahead = this.tokens.getFirst();
        parsingSteps.append("Iniciando el análisis...\n");
        evento();

        if (lookahead.token != Token.LAMBDA) {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
        parsingSteps.append("Análisis completado.\n");
    }

    private void nextToken() {
        parsingSteps.append(tokens.getFirst().token).append(") Proximo: ").append(tokens.getFirst().lexeme).append("\n");
        tokens.pop();

        if (tokens.isEmpty()) {
            lookahead = new Token(Token.LAMBDA, "", -1);
        } else {
            lookahead = tokens.getFirst();
        }
    }

    private void evento() {
        if (lookahead.token == Token.NEW) {
            nextToken(); // Consume 'new'
            if (lookahead.token == Token.EVENTO) {
                nextToken(); // Consume 'Evento'
                if (lookahead.token == Token.DOBLE_DOT) {
                    nextToken(); // Consume ':'
                    titulo(); // Procesa título
                    fechaInicio(); // Procesa fecha de inicio
                    fechaFin(); // Procesa fecha de fin
                    ubicacion(); // Procesa ubicación
                    descripcion(); // Procesa descripción
                    correo(); // procsa el correo

                    // Verifica que no haya más tokens después de Descripcion
                    if (lookahead.token != Token.LAMBDA) {
                        throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found after Descripcion");
                    }
                } else {
                    throw new ParserException("Expected ':' after Evento but found " + lookahead.lexeme);
                }
            } else {
                throw new ParserException("Expected 'Evento' but found " + lookahead.lexeme);
            }
        } else {
            throw new ParserException("Expected 'new' but found " + lookahead.lexeme);
        }
    }

    private void titulo() {
        if (lookahead.token == Token.TITULO) {
            nextToken();
            if (lookahead.token == Token.DOBLE_DOT) {
                nextToken();
                stringLiteral(); // Captura el título como string literal
            } else {
                throw new ParserException("Expected ':' after Titulo but found " + lookahead.lexeme);
            }
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void fechaInicio() {
        if (lookahead.token == Token.FECHAINICIO) {
            nextToken();
            if (lookahead.token == Token.DOBLE_DOT) {
                nextToken();
                fechaLiteral(); // Captura la fecha
                if (lookahead.token == Token.SEPARATOR) {
                    nextToken();
                    horaLiteral(); // Captura la hora
                }
            } else {
                throw new ParserException("Expected ':' after FechaInicio but found " + lookahead.lexeme);
            }
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void fechaFin() {
        if (lookahead.token == Token.FECHAFIN) {
            nextToken();
            if (lookahead.token == Token.DOBLE_DOT) {
                nextToken();
                fechaLiteral();
                if (lookahead.token == Token.SEPARATOR) {
                    nextToken();
                    horaLiteral();
                }
            } else {
                throw new ParserException("Expected ':' after FechaFin but found " + lookahead.lexeme);
            }
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void ubicacion() {
        if (lookahead.token == Token.UBICACION) {
            nextToken();
            if (lookahead.token == Token.DOBLE_DOT) {
                nextToken();
                stringLiteral(); // Procesa la ubicación.
            } else {
                throw new ParserException("Expected ':' after Ubicacion but found " + lookahead.lexeme);
            }
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void descripcion() {
        if (lookahead.token == Token.DESCRIPCION) {
            nextToken();
            if (lookahead.token == Token.DOBLE_DOT) {
                nextToken();
                stringLiteral();
            } else {
                // Epsilon: Si no hay descripción, se permite
                parsingSteps.append("No se encontró descripción.\n");
            }
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void correo(){
        if(lookahead.token == Token.CORREO){
            nextToken();
            if(lookahead.token == Token.DOBLE_DOT){
                nextToken();
                stringCorreo();
            } else {
                // Epsilon: Si no hay descripción, se permite
                parsingSteps.append("No se encontró un correo válido.\n");
            }
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void stringLiteral() {
        if (lookahead.token == Token.STRING) {
            nextToken();
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void fechaLiteral() {
        if (lookahead.token == Token.DATE) {
            nextToken();
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void horaLiteral() {
        if (lookahead.token == Token.HOUR) {
            nextToken();
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }

    private void stringCorreo(){
        if(lookahead.token == Token.STRING_CORREO){
            nextToken();
        } else {
            throw new ParserException("Unexpected symbol " + lookahead.lexeme + " found");
        }
    }
}