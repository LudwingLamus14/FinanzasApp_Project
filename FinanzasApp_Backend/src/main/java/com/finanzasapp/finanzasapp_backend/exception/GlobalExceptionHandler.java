package com.finanzasapp.finanzasapp_backend.exception;

import com.finanzasapp.finanzasapp_backend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/** Convierte las excepciones en respuestas JSON con un mensaje comprensible. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(DatosInvalidosException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Falla alguna anotacion de MovimientoRequest (@NotBlank, @NotNull, ...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    /** JSON mal formado, tipo desconocido o fecha que no es yyyy-MM-dd. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Datos inválidos: revisa el JSON, el tipo (INGRESO o GASTO) y la fecha (yyyy-MM-dd).");
    }

    /** Por ejemplo GET /api/movimientos/abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "El valor '" + ex.getValue() + "' no es válido para '" + ex.getName() + "'.");
    }

    /** Problemas con la base de datos (caida, restriccion violada, etc.). */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> baseDeDatos(DataAccessException ex) {
        log.error("Error de acceso a la base de datos", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al acceder a la base de datos. Intenta de nuevo más tarde.");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensaje));
    }
}
