# API administrativa

## Configuracion

La autenticacion JWT requiere `NAUTIFLOW_JWT_SECRET`, una cadena Base64 que represente al menos 32 bytes aleatorios. Puede generarla con `openssl rand -base64 32` o en PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:NAUTIFLOW_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Configúrela en el entorno del proceso del backend. Los tokens duran 12 horas. Las credenciales del administrador se configuran con `NAUTIFLOW_ADMIN_EMAIL` y `NAUTIFLOW_ADMIN_PASSWORD`; si falta alguna, `POST /login` rechaza todas las solicitudes.

El indicador `viajesActivos` cuenta lancheros cuyo `estado_viaje` es `EN_VIAJE`. La tabla `viaje` no tiene un campo de estado.

No ejecutar de nuevo `back/init/01-schema.sql` sobre una base que se quiera conservar: ese script elimina y vuelve a crear `delta_tigre_db`.

El registro ya no solicita ni almacena el documento de identidad del lanchero. Para actualizar una base existente, ejecutar manualmente `back/init/02-eliminar-documento-identidad.sql` una vez que se haya decidido descartar los valores guardados en esa columna; la operación elimina esos datos.

El registro de lancheros no requiere cuentas ni contraseñas. En bases existentes, ejecutar manualmente `back/init/03-eliminar-password-hash-lanchero.sql` para retirar los hashes de contraseña que ya no se usan. El script inicial `back/init/01-schema.sql` ya crea el esquema sin esa columna.

## Endpoints

`GET /zonas` es público y devuelve únicamente `id` y `nombre` de las zonas existentes, ordenadas por nombre. En `POST /lancheros/registrar`, `solicitud` es un campo de texto con JSON y requiere `nombreCompleto`, `whatsapp`, `correo`, `nombreEmbarcacion` y `capacidadPasajeros`; `zonasIds` es opcional. No se envían credenciales. Los IDs de zonas deben existir y no repetirse; si se omiten o están vacíos, la lancha queda sin zonas. El multipart requiere `licencia`, `seguro` y `antecedentes`; `fotos` es opcional (máximo cinco). La respuesta `201` contiene únicamente `idLanchero`, `idLancha`, `estado` y `mensaje`. El registro encola el correo de confirmación para procesamiento asíncrono: la respuesta HTTP no confirma su entrega. Los errores usan el campo `message` junto al estado, ruta y marca de tiempo.

La colección Postman importable está en `back/docs/NautiFlow.postman_collection.json`. Configura `baseUrl`, `adminEmail` y `adminPassword`; ejecuta Login para guardar el JWT. Los IDs de ejemplo se deben cambiar por registros válidos de la base de datos. No se incluyen credenciales ni secretos.

- `POST /login`: acepta `{ "email": "...", "password": "..." }`; devuelve un JWT Bearer o `401`.
- `GET /admin/dashboard`: devuelve contadores de solicitudes pendientes, lanchas de lancheros aprobados y viajes en curso.
- `GET /admin/solicitudes?estado=PENDIENTE&page=0&size=10`: estado opcional (`PENDIENTE`, `APROBADO`, `RECHAZADO`, `TODOS`); page inicia en cero y size va de 1 a 100. Se ordena por fecha de registro descendente y, en empate, por ID descendente.
- `GET /admin/solicitudes/{id}`: incluye lanchero, lancha, zonas y URLs prefirmadas de documentos. Nunca incluye claves internas de R2.
- `PATCH /admin/solicitudes/{id}/aprobar`: solo acepta solicitudes pendientes. La decisión y la fecha se guardan, el lanchero pasa automáticamente a `estadoViaje=ACTIVO` y se envía un correo al destinatario registrado. Si el proveedor de correo no está disponible, la transacción se revierte y se responde `503`.
- `PATCH /admin/solicitudes/{id}/rechazar`: requiere `{ "motivo": "..." }`; persiste el motivo antes de enviar el correo. Si falla el proveedor de correo, se revierte la decisión y se responde `503`.
- `GET /admin/lancheros?estado=APROBADO&page=0&size=10`: filtra por `estadoSolicitud` y devuelve `estadoSolicitud` y `estadoViaje` como propiedades separadas.
- `GET /admin/lancheros/disponibles?capacidad=4&zonaOrigen=1&zonaDestino=3&hora=23:00`: busca embarcaciones aprobadas y activas por capacidad, zonas y hora. `capacidad`, `zonaOrigen` y `zonaDestino` son enteros; `hora` es obligatoria y usa el formato `HH:mm`.
- `PATCH /admin/lancheros/{id}/estado-viaje`: recibe `{ "estadoViaje": "ACTIVO" }` o `{ "estadoViaje": "INACTIVO" }` para establecer explícitamente el estado operativo de un lanchero aprobado. Es idempotente (no alterna al repetir una llamada) y no permite modificar el estado mientras esté `EN_VIAJE`. La aprobación inicial ya lo deja `ACTIVO`.
- `POST /admin/viajes`: registra e inicia el viaje en la misma operación; requiere lancha aprobada y `ACTIVO`, dos zonas existentes, pasajeros dentro de capacidad y datos del turista. El inicio se fija a la hora actual y la lancha pasa a `EN_VIAJE`.
- `GET /admin/viajes?estado=EN_VIAJE&page=0&size=10`: pagina los viajes sin fecha de fin cuyo lanchero tiene `estadoViaje=EN_VIAJE`; incluye el nombre y WhatsApp del turista registrados al crear el viaje. No utiliza una columna de estado en `viaje`.
- `GET /admin/viajes/seguimiento?estado=TODOS&page=0&size=10`: acepta `TODOS`, `EN_VIAJE` o `FINALIZADO` en el filtro `estado`. pagina todo el historial, del viaje más reciente al más antiguo e incluye nombre y WhatsApp del turista. El estado se deriva de `fecha_hora_fin` (`EN_VIAJE` si es nula; `FINALIZADO` si tiene valor). La última ubicación confirmada es el destino del viaje finalizado más reciente de la embarcación; un viaje en curso no se considera llegado a su destino.
- `PATCH /admin/viajes/{id}/finalizar`: fija la fecha de fin, genera y guarda un token aleatorio asociado a ese viaje, y reactiva al lanchero. Devuelve `tokenResena`, el nombre y WhatsApp guardados en la reserva y `mensajeResena` personalizado. React debe construir la URL de su ruta de reseñas con su propio origen y el token, y agregarla al mensaje al construir el enlace `wa.me`; el backend no genera enlaces de frontend ni de WhatsApp.
- `GET /resena/{token}`: público; resuelve la reserva por el token y devuelve el nombre, las zonas y las fechas guardadas en el viaje, además de indicar si ya existe una reseña. No devuelve el WhatsApp del pasajero.
- `POST /resena/{token}`: público; requiere únicamente `{ "puntuacion": 5, "comentario": "..." }`. El token identifica y autoriza el viaje, no se aceptan nombre, WhatsApp ni ID del viaje del formulario. Solo se permite una reseña por viaje. Una notificación se envía al lanchero mediante Resend; si falla, la reseña se revierte y se responde `503`.
- `GET /admin/resenas?page=0&size=10`: consulta reseñas y datos públicos mínimos del lanchero calificado.

`GET /resena/{token}` devuelve JSON de la reserva; no es una página con formulario. El frontend debe implementar la ruta React `/resena/{token}`, leer el token y consumir este endpoint para mostrar el formulario. Al enviar, debe llamar `POST /resena/{token}` con puntuación y comentario.

Todos los endpoints `/admin/**` requieren `Authorization: Bearer <token>`. El registro público `POST /lancheros/registrar` permanece sin cambios.
