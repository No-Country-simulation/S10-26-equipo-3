INSERT INTO zona (id_zona, nombre) VALUES
    (1, 'Primera sección'),
    (2, 'Segunda sección'),
    (3, 'Tercera sección')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('zona', 'id_zona'), GREATEST((SELECT MAX(id_zona) FROM zona), 1));

INSERT INTO lanchero (id_lanchero, nombre_completo, whatsapp, correo, estado_solicitud, fecha_registro, fecha_decision, motivo_rechazo, estado_viaje) VALUES
    (1, 'Martín Gómez', '+5491155550101', 'martin.gomez@example.com', 'APROBADO', now() - interval '210 days', now() - interval '206 days', NULL, 'EN_VIAJE'),
    (2, 'Lucía Fernández', '+5491155550102', 'lucia.fernandez@example.com', 'APROBADO', now() - interval '200 days', now() - interval '197 days', NULL, 'ACTIVO'),
    (3, 'Diego Ruiz', '+5491155550103', 'diego.ruiz@example.com', 'APROBADO', now() - interval '180 days', now() - interval '178 days', NULL, 'ACTIVO'),
    (4, 'Sofía Benítez', '+5491155550104', 'sofia.benitez@example.com', 'APROBADO', now() - interval '160 days', now() - interval '157 days', NULL, 'EN_VIAJE'),
    (5, 'Ramiro Acosta', '+5491155550105', 'ramiro.acosta@example.com', 'APROBADO', now() - interval '140 days', now() - interval '136 days', NULL, 'INACTIVO'),
    (6, 'Paula Ibarra', '+5491155550106', 'paula.ibarra@example.com', 'APROBADO', now() - interval '12 days', now() - interval '10 days', NULL, 'ACTIVO'),
    (7, 'Carla Méndez', '+5491155550107', 'carla.mendez@example.com', 'PENDIENTE', now() - interval '2 days', NULL, NULL, 'ACTIVO'),
    (8, 'Nicolás Paz', '+5491155550108', 'nicolas.paz@example.com', 'PENDIENTE', now() - interval '5 hours', NULL, NULL, 'ACTIVO'),
    (9, 'Esteban Luna', '+5491155550109', 'esteban.luna@example.com', 'RECHAZADO', now() - interval '30 days', now() - interval '28 days', 'El seguro de la embarcación está vencido.', 'ACTIVO');

SELECT setval(pg_get_serial_sequence('lanchero', 'id_lanchero'), (SELECT MAX(id_lanchero) FROM lanchero));

INSERT INTO lancha (id_lancha, id_lanchero, nombre, capacidad) VALUES
    (1, 1, 'La Gaviota', 6),
    (2, 2, 'Río Sereno', 8),
    (3, 3, 'Delta Azul', 4),
    (4, 4, 'Camalote', 10),
    (5, 5, 'El Junco', 5),
    (6, 6, 'Sauce Criollo', 7),
    (7, 7, 'Brisa', 5),
    (8, 8, 'Isla Verde', 6),
    (9, 9, 'Ventarrón', 3);

SELECT setval(pg_get_serial_sequence('lancha', 'id_lancha'), (SELECT MAX(id_lancha) FROM lancha));

INSERT INTO lancha_zona (id_lancha, id_zona) VALUES
    (1, 1), (1, 2),
    (2, 2), (2, 3),
    (3, 1), (3, 3),
    (4, 1), (4, 2), (4, 3),
    (5, 3),
    (6, 2),
    (7, 1),
    (8, 2), (8, 3),
    (9, 1);

INSERT INTO viaje (id_viaje, id_lancha, id_zona_origen, id_zona_destino, fecha_hora_inicio, fecha_hora_fin, cantidad_pasajeros, turista_nombre_completo, turista_whatsapp, token_resena) VALUES
    (1, 1, 1, 2, date_trunc('month', now()) - interval '5 months' + interval '3 days 10 hours', date_trunc('month', now()) - interval '5 months' + interval '3 days 12 hours', 4, 'Ana Pérez', '+5491166660001', md5(random()::text || clock_timestamp()::text)),
    (2, 2, 2, 3, date_trunc('month', now()) - interval '5 months' + interval '17 days 9 hours', date_trunc('month', now()) - interval '5 months' + interval '17 days 11 hours', 6, 'Juan Torres', '+5491166660002', md5(random()::text || clock_timestamp()::text)),
    (3, 3, 1, 3, date_trunc('month', now()) - interval '4 months' + interval '6 days 14 hours', date_trunc('month', now()) - interval '4 months' + interval '6 days 16 hours', 3, 'Sofía Díaz', '+5491166660003', md5(random()::text || clock_timestamp()::text)),
    (4, 4, 1, 3, date_trunc('month', now()) - interval '4 months' + interval '12 days 10 hours', date_trunc('month', now()) - interval '4 months' + interval '12 days 13 hours', 9, 'Grupo Escuela 12', '+5491166660004', md5(random()::text || clock_timestamp()::text)),
    (5, 1, 2, 1, date_trunc('month', now()) - interval '4 months' + interval '22 days 13 hours', date_trunc('month', now()) - interval '4 months' + interval '22 days 15 hours', 5, 'Pedro Álvarez', '+5491166660005', md5(random()::text || clock_timestamp()::text)),
    (6, 2, 3, 2, date_trunc('month', now()) - interval '3 months' + interval '4 days 10 hours', date_trunc('month', now()) - interval '3 months' + interval '4 days 12 hours', 7, 'Valentina Ríos', '+5491166660006', md5(random()::text || clock_timestamp()::text)),
    (7, 5, 3, 1, date_trunc('month', now()) - interval '3 months' + interval '10 days 15 hours', date_trunc('month', now()) - interval '3 months' + interval '10 days 17 hours', 2, 'Tomás Herrera', '+5491166660007', md5(random()::text || clock_timestamp()::text)),
    (8, 4, 2, 3, date_trunc('month', now()) - interval '3 months' + interval '19 days 9 hours', date_trunc('month', now()) - interval '3 months' + interval '19 days 12 hours', 8, 'Familia Ortiz', '+5491166660008', md5(random()::text || clock_timestamp()::text)),
    (9, 1, 1, 2, date_trunc('month', now()) - interval '2 months' + interval '2 days 9 hours', date_trunc('month', now()) - interval '2 months' + interval '2 days 11 hours', 4, 'Camila Sosa', '+5491166660009', md5(random()::text || clock_timestamp()::text)),
    (10, 2, 2, 3, date_trunc('month', now()) - interval '2 months' + interval '9 days 16 hours', date_trunc('month', now()) - interval '2 months' + interval '9 days 18 hours', 8, 'Mateo Castro', '+5491166660010', md5(random()::text || clock_timestamp()::text)),
    (11, 3, 3, 1, date_trunc('month', now()) - interval '2 months' + interval '15 days 11 hours', date_trunc('month', now()) - interval '2 months' + interval '15 days 13 hours', 3, 'Lucas Romero', '+5491166660011', md5(random()::text || clock_timestamp()::text)),
    (12, 5, 3, 2, date_trunc('month', now()) - interval '2 months' + interval '23 days 10 hours', date_trunc('month', now()) - interval '2 months' + interval '23 days 12 hours', 4, 'Julieta Morales', '+5491166660012', md5(random()::text || clock_timestamp()::text)),
    (13, 4, 1, 2, date_trunc('month', now()) - interval '1 months' + interval '1 days 9 hours', date_trunc('month', now()) - interval '1 months' + interval '1 days 11 hours', 10, 'Club de Remo Tigre', '+5491166660013', md5(random()::text || clock_timestamp()::text)),
    (14, 2, 3, 2, date_trunc('month', now()) - interval '1 months' + interval '6 days 15 hours', date_trunc('month', now()) - interval '1 months' + interval '6 days 17 hours', 5, 'Martina Vega', '+5491166660014', md5(random()::text || clock_timestamp()::text)),
    (15, 1, 2, 1, date_trunc('month', now()) - interval '1 months' + interval '12 days 10 hours', date_trunc('month', now()) - interval '1 months' + interval '12 days 12 hours', 6, 'Joaquín Molina', '+5491166660015', md5(random()::text || clock_timestamp()::text)),
    (16, 3, 1, 3, date_trunc('month', now()) - interval '1 months' + interval '18 days 14 hours', date_trunc('month', now()) - interval '1 months' + interval '18 days 16 hours', 4, 'Agustina Silva', '+5491166660016', md5(random()::text || clock_timestamp()::text)),
    (17, 4, 3, 1, date_trunc('month', now()) - interval '1 months' + interval '24 days 9 hours', date_trunc('month', now()) - interval '1 months' + interval '24 days 12 hours', 7, 'Bruno Quiroga', '+5491166660017', md5(random()::text || clock_timestamp()::text)),
    (18, 2, 2, 3, now() - interval '1 day 4 hours', now() - interval '1 day 2 hours', 6, 'Renata Giménez', '+5491166660018', md5(random()::text || clock_timestamp()::text)),
    (19, 3, 3, 1, now() - interval '5 hours', now() - interval '3 hours', 3, 'Federico Núñez', '+5491166660019', md5(random()::text || clock_timestamp()::text)),
    (20, 1, 1, 2, now() - interval '1 hour', NULL, 4, 'Micaela Rojas', '+5491166660020', NULL),
    (21, 4, 2, 3, now() - interval '40 minutes', NULL, 9, 'Excursión Colegio San José', '+5491166660021', NULL);

SELECT setval(pg_get_serial_sequence('viaje', 'id_viaje'), (SELECT MAX(id_viaje) FROM viaje));

INSERT INTO resena (id_viaje, estrellas, comentario, fecha) VALUES
    (1, 5, 'Excelente paseo, muy puntual.', date_trunc('month', now()) - interval '5 months' + interval '4 days'),
    (2, 5, 'Muy buena atención y la lancha impecable.', date_trunc('month', now()) - interval '5 months' + interval '18 days'),
    (3, 4, 'Lindo recorrido, volvería.', date_trunc('month', now()) - interval '4 months' + interval '7 days'),
    (4, 5, 'Los chicos la pasaron genial, muy seguro.', date_trunc('month', now()) - interval '4 months' + interval '13 days'),
    (5, 4, NULL, date_trunc('month', now()) - interval '4 months' + interval '23 days'),
    (6, 5, 'Súper recomendable.', date_trunc('month', now()) - interval '3 months' + interval '5 days'),
    (7, 3, 'Llegó un poco tarde, pero el paseo estuvo bien.', date_trunc('month', now()) - interval '3 months' + interval '11 days'),
    (8, 5, 'Espaciosa y cómoda para toda la familia.', date_trunc('month', now()) - interval '3 months' + interval '20 days'),
    (9, 4, 'Buena experiencia.', date_trunc('month', now()) - interval '2 months' + interval '3 days'),
    (10, 4, 'Muy amable el lanchero.', date_trunc('month', now()) - interval '2 months' + interval '10 days'),
    (11, 5, 'Hermoso recorrido por el Delta.', date_trunc('month', now()) - interval '2 months' + interval '16 days'),
    (12, 4, NULL, date_trunc('month', now()) - interval '2 months' + interval '24 days'),
    (13, 5, 'Impecable, nos llevó a todo el equipo.', date_trunc('month', now()) - interval '1 months' + interval '2 days'),
    (14, 5, 'Puntual y muy conocedor de la zona.', date_trunc('month', now()) - interval '1 months' + interval '7 days'),
    (16, 4, 'Todo muy bien.', date_trunc('month', now()) - interval '1 months' + interval '19 days');

INSERT INTO trayecto_retorno_vacio (id_viaje, id_zona_origen, id_zona_destino, fecha_hora_retorno, estado) VALUES
    (21, 3, 2, now() + interval '2 hours', 'ACTIVO');
