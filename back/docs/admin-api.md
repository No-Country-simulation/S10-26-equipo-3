# API administrativa

## Configuracion

La autenticacion JWT requiere `NAUTIFLOW_JWT_SECRET`, una cadena Base64 que represente al menos 32 bytes aleatorios. Puede generarla con `openssl rand -base64 32` o en PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:NAUTIFLOW_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Configúrela en el entorno del proceso del backend. Los tokens duran 12 horas. Las credenciales del administrador son las constantes indicadas en `AdminAuthService`.

El indicador `viajesActivos` cuenta lancheros cuyo `estado_viaje` es `EN_VIAJE`. La tabla `viaje` no tiene un campo de estado.

No ejecutar de nuevo `back/init/01-schema.sql` sobre una base que se quiera conservar: ese script elimina y vuelve a crear `delta_tigre_db`.

El registro ya no solicita ni almacena el documento de identidad del lanchero. Para actualizar una base existente, ejecutar manualmente `back/init/02-eliminar-documento-identidad.sql` una vez que se haya decidido descartar los valores guardados en esa columna; la operación elimina esos datos.

## Endpoints

`GET /zonas` es público y devuelve únicamente `id` y `nombre` de las zonas existentes, ordenadas por nombre. En el registro multipart, incluye opcionalmente `zonasIds` dentro del JSON de texto `solicitud` (por ejemplo `[1,3]`). Los IDs deben existir y no repetirse; si se omite o está vacío, la lancha queda sin zonas. La respuesta del registro contiene únicamente `idLanchero`, `idLancha`, `estado` y `mensaje`. La colección Postman incluye la consulta de zonas y un ejemplo de registro.

La colección Postman importable está en `back/docs/NautiFlow.postman_collection.json`. Configura `baseUrl`, `adminEmail` y `adminPassword`; ejecuta Login para guardar el JWT. Los IDs de ejemplo se deben cambiar por registros válidos de la base de datos. No se incluyen credenciales ni secretos.

- `POST /login`: acepta `{ "email": "...", "password": "..." }`; devuelve un JWT Bearer o `401`.
- `GET /admin/dashboard`: devuelve contadores de solicitudes pendientes, lanchas de lancheros aprobados y viajes en curso.
- `GET /admin/solicitudes?estado=PENDIENTE&page=0&size=10`: estado opcional (`PENDIENTE`, `APROBADO`, `RECHAZADO`, `TODOS`); page inicia en cero y size va de 1 a 100. Se ordena por fecha de registro descendente y, en empate, por ID descendente.
- `GET /admin/solicitudes/{id}`: incluye lanchero, lancha, zonas y URLs prefirmadas de documentos. Nunca incluye claves internas de R2.
- `PATCH /admin/solicitudes/{id}/aprobar`: solo acepta solicitudes pendientes. La decisión y la fecha se guardan, el lanchero pasa automáticamente a `estadoViaje=ACTIVO` y se envía un correo al destinatario registrado. Si SMTP no está disponible, la transacción se revierte y se responde `503`.
- `PATCH /admin/solicitudes/{id}/rechazar`: requiere `{ "motivo": "..." }`; persiste el motivo antes de enviar el correo. Si SMTP falla, se revierte la decisión y se responde `503`.
- `GET /admin/lancheros?estado=APROBADO&page=0&size=10`: filtra por `estadoSolicitud` y devuelve `estadoSolicitud` y `estadoViaje` como propiedades separadas.
- `GET /admin/lancheros/disponibles?capacidad=5&zona_origen=1&zona_destino=2&hora=14:00`: busca embarcaciones aprobadas y operativas por capacidad y zonas disponibles. `hora` es obligatoria con formato `HH:mm`; se combina con la fecha actual en `America/Bogota` mediante el `Clock` inyectado. Los filtros de zona admiten IDs con nombres `zona_origen`/`zona_destino` o nombres de zona con `zonaOrigen`/`zonaDestino`; no deben mezclarse ambas formas para una misma zona. Solo se devuelven lanchas sin viaje que abarque ese instante ni retorno vacío en estado `ACTIVO`. El modelo no registra reservas futuras ni duración estimada, por lo que la respuesta es una lista de candidatas a contactar por WhatsApp, no una confirmación o asignación del horario. Para confirmar horarios futuros, la modificación mínima sería persistir reservas con hora de inicio y fin confirmadas explícitamente e integrarlas en la asignación, sin estimar duraciones.
- `PATCH /admin/lancheros/{id}/estado-viaje`: recibe `{ "estadoViaje": "ACTIVO" }` o `{ "estadoViaje": "INACTIVO" }` para establecer explícitamente el estado operativo de un lanchero aprobado. Es idempotente (no alterna al repetir una llamada) y no permite modificar el estado mientras esté `EN_VIAJE`. La aprobación inicial ya lo deja `ACTIVO`.
- `POST /admin/viajes`: registra e inicia el viaje en la misma operación; requiere lancha aprobada y `ACTIVO`, dos zonas existentes, pasajeros dentro de capacidad y datos del turista. El inicio se fija a la hora actual y la lancha pasa a `EN_VIAJE`.
- `GET /admin/viajes?estado=EN_VIAJE&page=0&size=10`: pagina los viajes sin fecha de fin cuyo lanchero tiene `estadoViaje=EN_VIAJE`; incluye el nombre y WhatsApp del turista registrados al crear el viaje. No utiliza una columna de estado en `viaje`.
- `GET /admin/viajes/seguimiento?page=0&size=10`: pagina todo el historial, del viaje más reciente al más antiguo. El estado se deriva de `fecha_hora_fin` (`EN_VIAJE` si es nula; `FINALIZADO` si tiene valor). La última ubicación confirmada es el destino del viaje finalizado más reciente de la embarcación; un viaje en curso no se considera llegado a su destino.
- `PATCH /admin/viajes/{id}/finalizar`: fija la fecha de fin, genera y guarda un token aleatorio asociado a ese viaje, y reactiva al lanchero. Devuelve `tokenResena`, el nombre y WhatsApp guardados en la reserva y `mensajeResena` personalizado. React debe construir la URL de su ruta de reseñas con su propio origen y el token, y agregarla al mensaje al construir el enlace `wa.me`; el backend no genera enlaces de frontend ni de WhatsApp.
- `GET /resena/{token}`: público; resuelve la reserva por el token y devuelve el nombre, las zonas y las fechas guardadas en el viaje, además de indicar si ya existe una reseña. No devuelve el WhatsApp del pasajero.
- `POST /resena/{token}`: público; requiere únicamente `{ "puntuacion": 5, "comentario": "..." }`. El token identifica y autoriza el viaje, no se aceptan nombre, WhatsApp ni ID del viaje del formulario. Solo se permite una reseña por viaje. Una notificación Gmail se envía al lanchero; si falla, la reseña se revierte y se responde `503`.
- `GET /admin/resenas?page=0&size=10`: consulta reseñas y datos públicos mínimos del lanchero calificado.

`GET /resena/{token}` devuelve JSON de la reserva; no es una página con formulario. El frontend debe implementar la ruta React `/resena/{token}`, leer el token y consumir este endpoint para mostrar el formulario. Al enviar, debe llamar `POST /resena/{token}` con puntuación y comentario.

Todos los endpoints `/admin/**` requieren `Authorization: Bearer <token>`. El registro público `POST /lancheros/registrar` permanece sin cambios.
