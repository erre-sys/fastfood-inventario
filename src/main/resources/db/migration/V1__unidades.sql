-- Unidades de medida. Toda cantidad se guarda en la unidad base de su dimensión (g, ml, und).
CREATE TABLE unidad (
    codigo        VARCHAR(20)    PRIMARY KEY,
    nombre        VARCHAR(60)    NOT NULL,
    dimension     VARCHAR(10)    NOT NULL CHECK (dimension IN ('MASA', 'VOLUMEN', 'CONTEO')),
    factor_a_base NUMERIC(18, 6) NOT NULL CHECK (factor_a_base > 0),
    estandar      BOOLEAN        NOT NULL DEFAULT FALSE,
    creado_por    VARCHAR(100)   NOT NULL DEFAULT 'sistema',
    creado_en     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- Unidades estándar. Cocina en medidas métricas: cucharadita 5 ml, cucharada 15 ml, taza 240 ml.
INSERT INTO unidad (codigo, nombre, dimension, factor_a_base, estandar) VALUES
    ('mg',          'Miligramo',   'MASA',    0.001,     TRUE),
    ('g',           'Gramo',       'MASA',    1,         TRUE),
    ('kg',          'Kilogramo',   'MASA',    1000,      TRUE),
    ('lb',          'Libra',       'MASA',    453.59237, TRUE),
    ('oz',          'Onza',        'MASA',    28.349523, TRUE),
    ('ml',          'Mililitro',   'VOLUMEN', 1,         TRUE),
    ('l',           'Litro',       'VOLUMEN', 1000,      TRUE),
    ('cucharadita', 'Cucharadita', 'VOLUMEN', 5,         TRUE),
    ('cucharada',   'Cucharada',   'VOLUMEN', 15,        TRUE),
    ('taza',        'Taza',        'VOLUMEN', 240,       TRUE),
    ('und',         'Unidad',      'CONTEO',  1,         TRUE),
    ('docena',      'Docena',      'CONTEO',  12,        TRUE);
