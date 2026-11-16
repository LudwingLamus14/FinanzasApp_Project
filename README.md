# FinanzasApp

Aplicación Android de finanzas personales con almacenamiento **SQLite local**, siguiendo el enfoque de `sqlite.md`: una clase `DatabaseHelper` basada en `SQLiteOpenHelper`, escritura con `ContentValues` y consultas parametrizadas con cursores. La interfaz existente en Jetpack Compose se conserva.

## Funcionamiento

- La base privada `finanzas.db` se crea automáticamente en el dispositivo al usar la app por primera vez, con categorías de ingresos y gastos. Los movimientos empiezan vacíos.
- Permite registrar, consultar, filtrar y eliminar movimientos. Resumen y estadísticas se calculan con esos datos locales, incluyendo día, semana de lunes a domingo, mes y año de la fecha seleccionada.
- Los importes se almacenan en centavos enteros y se calculan con `BigDecimal`, sin pérdida de precisión por coma flotante.
- Los datos permanecen al cerrar y volver a abrir la app. Las futuras versiones del esquema deben implementar migraciones que los conserven; no se borran tablas en `onUpgrade`.
- No requiere servidor, PostgreSQL, credenciales, URL de API ni conexión a Internet. El código anterior de Retrofit y el permiso de Internet se han retirado de Android.

Los registros existentes en PostgreSQL **no se importan automáticamente**. La base local es independiente en cada dispositivo; desinstalar la app o borrar sus datos elimina esa copia local (sujeta a las opciones de respaldo de Android).

## Desarrollo y pruebas

Abrir `FinanzasApp/` en Android Studio, con JDK 25 (versión solicitada por Gradle), Android SDK 37 y Build Tools 36. Configurar el SDK mediante `local.properties` o `ANDROID_HOME` según el equipo.

Desde `FinanzasApp/`:

```bash
bash gradlew :app:assembleDebug :app:testDebugUnitTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Las pruebas Robolectric ejercitan SQLite mediante el modo LEGACY de Robolectric (sqlite4java): persistencia al reabrir, categorías iniciales, precisión monetaria, eliminación, filtros, validación y estadísticas de calendario. No requieren servidor ni emulador. Para una prueba manual, instalar el APK, guardar un ingreso y un gasto sin conexión, revisar saldo y estadísticas, cerrar y abrir la app y eliminar un movimiento.

`FinanzasApp_Backend/` conserva el backend anterior como referencia histórica; no participa en la ejecución ni en la compilación de la app Android actual.
