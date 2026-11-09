INSERT INTO zona (id_zona, nombre) VALUES
    (1, 'Primera sección'),
    (2, 'Segunda sección'),
    (3, 'Tercera sección')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('zona', 'id_zona'), GREATEST((SELECT MAX(id_zona) FROM zona), 1));
