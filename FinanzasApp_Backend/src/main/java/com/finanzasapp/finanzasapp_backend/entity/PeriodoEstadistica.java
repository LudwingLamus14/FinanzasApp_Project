package com.finanzasapp.finanzasapp_backend.entity;

/**
 * Periodo para GET /api/estadisticas. No es una tabla: Spring convierte
 * el parametro de la URL (?periodo=DIA) a este enum automaticamente.
 */
public enum PeriodoEstadistica {
    DIA,
    SEMANA,
    MES,
    ANIO
}
