
package com.gcalendarinterpreter.controller;

import com.gcalendarinterpreter.model.Token;
import com.gcalendarinterpreter.model.Tokenizer;
import com.gcalendarinterpreter.model.exceptions.LexerException;
import com.gcalendarinterpreter.parser.Parser;
import com.gcalendarinterpreter.view.LexicalAnalyzerView;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.gcalendarinterpreter.model.Token.*;

public class LexicalAnalyzerController {
    private LexicalAnalyzerView view;
    private Tokenizer tokenizer;
    private Parser parser;
    private com.google.api.services.calendar.Calendar calendar;


    public LexicalAnalyzerController(LexicalAnalyzerView view, com.google.api.services.calendar.Calendar service) {
        this.view = view;
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
        this.calendar = service;
        configureTokenizer();
        initController();
    }


    private void configureTokenizer() {
        tokenizer.add("\\bnew\\b", Token.NEW);
        tokenizer.add("\\bEvento\\b", Token.EVENTO);
        tokenizer.add("\\bTarea\\b", Token.EVENTO);
        tokenizer.add("\\bTitulo\\b", TITULO);
        tokenizer.add("\\bFechaInicio\\b", Token.FECHAINICIO);
        tokenizer.add("\\bFechaFin\\b", Token.FECHAFIN);
        tokenizer.add("\\bUbicacion\\b", UBICACION);
        tokenizer.add("\\bDescripcion\\b", DESCRIPCION);
        tokenizer.add("\\bCorreo\\b", Token.CORREO);
        tokenizer.add(":", Token.DOBLE_DOT);
        tokenizer.add("(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}", Token.DATE);
        tokenizer.add("(?:[01]\\d|2[0-3]):[0-5]\\d", Token.HOUR);
        tokenizer.add("\"[^\"]*\"", Token.STRING);
        tokenizer.add("[|]", Token.SEPARATOR);
        tokenizer.add("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b(?:\\s*,\\s*\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b)*", Token.STRING_CORREO);
    }

    private void initController() {
        view.compileButton.addActionListener(e -> compileText());

        view.uploadButton.addActionListener(e -> uploadFile());
    }

    private void compileText() {
        String text = view.textPane.getText();
        StyledDocument doc = view.textPane.getStyledDocument();
        doc.setCharacterAttributes(0, text.length(), new SimpleAttributeSet(), true);

        try {
            tokenizer.tokenize(text);
            view.resultLabel.setText("Sintaxis correcta");
            parseTokens();
        } catch (LexerException | IOException ex) {
            view.resultLabel.setText("Sintaxis incorrecta: " + ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }



    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll("##.*?##", "");
                    content.append(line).append("\n");
                }
                view.textPane.setText(content.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    private Calendar service;

    private void parseTokens() throws IOException {
        List<Token> tokens = tokenizer.getTokens();
        List<Map<String, String>> events = new ArrayList<>();
        Map<String, String> currentEvent = null;
        String currentField = null;
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.token == Token.NEW && i + 1 < tokens.size() && tokens.get(i + 1).token == Token.EVENTO) {
                // Detectar el inicio de un nuevo evento
                if (currentEvent != null) {
                    events.add(currentEvent); // Guardar el evento anterior
                }
                currentEvent = new HashMap<>();
                i++; // Saltar el token 'Evento'
                continue;
            }

            if (currentEvent == null) {
                continue; // Ignorar datos fuera de un bloque 'new Evento'
            }

            switch (token.token) {
                case TITULO:
                    currentField = "Titulo";
                    break;
                case FECHAINICIO:
                    currentField = "FechaInicio";
                    break;
                case FECHAFIN:
                    currentField = "FechaFin";
                    break;
                case UBICACION:
                    currentField = "Ubicacion";
                    break;
                case DESCRIPCION:
                    currentField = "Descripcion";
                    break;
                case CORREO:
                    currentField = "Correo";
                    break;
            }

            if (currentField != null && i + 2 < tokens.size()
                    && tokens.get(i + 1).token == Token.DOBLE_DOT) {

                if (currentField.equals("FechaInicio") || currentField.equals("FechaFin")) {
                    // Manejar fecha y hora con formato ISO 8601
                    String date = tokens.get(i + 2).lexeme; // Fecha
                    String time = (i + 4 < tokens.size() && tokens.get(i + 3).token == Token.SEPARATOR)
                            ? tokens.get(i + 4).lexeme // Hora
                            : "00:00:00"; // Hora por defecto
                    String timezone = "-05:00"; // Zona horaria predeterminada (Colombia)

                    try {
                        // Convertir la fecha al formato ISO 8601
                        Date parsedDate = inputDateFormat.parse(date);
                        String formattedDate = outputDateFormat.format(parsedDate);
                        currentEvent.put(currentField, formattedDate + "T" + time + ":00" + timezone);
                    } catch (ParseException e) {
                        System.err.println("Formato de fecha inválido: " + date);
                        e.printStackTrace();
                    }
                    i += 4; // Avanzar en los tokens ya que estamos manejando varios a la vez
                } else {
                    // Otros campos (título, ubicación, etc.)
                    Token valueToken = tokens.get(i + 2);
                    currentEvent.put(currentField, valueToken.lexeme.replaceAll("^\"|\"$", "")); // Remover comillas si aplica
                    i += 2; // Avanzar solo dos tokens (etiqueta, `:`, valor)
                }
                currentField = null; // Reset para el próximo campo
            }
        }

        // Agregar el último evento procesado
        if (currentEvent != null) {
            events.add(currentEvent);
        }

        // Procesar y crear eventos en Google Calendar
        for (Map<String, String> eventDetails : events) {
            Event event = new Event()
                    .setSummary(eventDetails.get("Titulo"))
                    .setLocation(eventDetails.get("Ubicacion"))
                    .setDescription(eventDetails.get("Descripcion"));

            DateTime startDateTime = new DateTime(eventDetails.get("FechaInicio"));
            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Bogota");
            event.setStart(start);

            DateTime endDateTime = new DateTime(eventDetails.get("FechaFin"));
            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Bogota");
            event.setEnd(end);

            List<EventAttendee> attendees = new ArrayList<>();
            String[] emails = eventDetails.get("Correo").split(",");
            for (String email : emails) {
                attendees.add(new EventAttendee().setEmail(email.trim()));
            }
            event.setAttendees(attendees);

            Event createdEvent = calendar.events().insert("primary", event).execute();
            System.out.printf("Evento creado: %s\n", createdEvent.getHtmlLink());
        }
    }


//    private void parseTokens() throws IOException {
//        List<Token> tokens = tokenizer.getTokens();
//        Map<String, String> eventDetails = new HashMap<>();
//        String currentField = null;
//        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
//        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        for (int i = 0; i < tokens.size(); i++) {
//            Token token = tokens.get(i);
//
//            switch (token.token) {
//                case TITULO:
//                    currentField = "Titulo";
//                    break;
//                case FECHAINICIO:
//                    currentField = "FechaInicio";
//                    break;
//                case FECHAFIN:
//                    currentField = "FechaFin";
//                    break;
//                case UBICACION:
//                    currentField = "Ubicacion";
//                    break;
//                case DESCRIPCION:
//                    currentField = "Descripcion";
//                    break;
//                case CORREO:
//                    currentField = "Correo";
//                    break;
//            }
//
//            if (currentField != null && i + 2 < tokens.size()
//                    && tokens.get(i + 1).token == Token.DOBLE_DOT) {
//
//                if (currentField.equals("FechaInicio") || currentField.equals("FechaFin")) {
//                    // Manejar fecha y hora con formato ISO 8601
//                    String date = tokens.get(i + 2).lexeme; // Fecha
//                    String time = (i + 4 < tokens.size() && tokens.get(i + 3).token == Token.SEPARATOR)
//                            ? tokens.get(i + 4).lexeme // Hora
//                            : "00:00:00"; // Hora por defecto
//                    String timezone = "-05:00"; // Zona horaria predeterminada (Colombia)
//
//                    try {
//                        // Convertir la fecha al formato ISO 8601
//                        Date parsedDate = inputDateFormat.parse(date);
//                        String formattedDate = outputDateFormat.format(parsedDate);
//                        eventDetails.put(currentField, formattedDate + "T" + time + ":00" + timezone);
//                    } catch (ParseException e) {
//                        System.err.println("Formato de fecha inválido: " + date);
//                        e.printStackTrace();
//                    }
//                    i += 4; // Avanzar en los tokens ya que estamos manejando varios a la vez
//                } else {
//                    // Otros campos (título, ubicación, etc.)
//                    Token valueToken = tokens.get(i + 2);
//                    eventDetails.put(currentField, valueToken.lexeme.replaceAll("^\"|\"$", "")); // Remover comillas si aplica
//                    i += 2; // Avanzar solo dos tokens (etiqueta, `:`, valor)
//                }
//                currentField = null; // Reset para el próximo campo
//            }
//        }
//
//
//        eventDetails.forEach((key, value) -> {
//            System.out.println(key + ": " + value);
//        });
//
//        Event event = new Event()
//                .setSummary(eventDetails.get("Titulo"))
//                .setLocation(eventDetails.get("Ubicacion"))
//                .setDescription(eventDetails.get("Descripcion"));
//
//        DateTime startDateTime = new DateTime(eventDetails.get("FechaInicio"));
//        EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Bogota");
//        event.setStart(start);
//
//        DateTime endDateTime = new DateTime(eventDetails.get("FechaFin"));
//        EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Bogota");
//        event.setEnd(end);
//
//
//        List<EventAttendee> attendees = new ArrayList<>();
//        String[] emails = eventDetails.get("Correo").split(",");
//        for (String email : emails) {
//            attendees.add(new EventAttendee().setEmail(email.trim()));
//        }
//        event.setAttendees(attendees);
//
//
//        Event createdEvent = calendar.events().insert("primary",  event).execute();
//        System.out.printf("Evento creado: %s\n", createdEvent.getHtmlLink());
//
//    }

}

