-- Units of measure. Every quantity is stored in the base unit of its dimension (g, ml, unit).
-- Codes are stable identifiers (English); names are display data shown to users (Spanish).
CREATE TABLE unit (
    code        VARCHAR(20)    PRIMARY KEY,
    name        VARCHAR(60)    NOT NULL,
    dimension   VARCHAR(10)    NOT NULL CHECK (dimension IN ('MASS', 'VOLUME', 'COUNT')),
    base_factor NUMERIC(18, 6) NOT NULL CHECK (base_factor > 0),
    standard    BOOLEAN        NOT NULL DEFAULT FALSE,
    created_by  VARCHAR(100)   NOT NULL DEFAULT 'system',
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- Standard units. Kitchen measures in metric: teaspoon 5 ml, tablespoon 15 ml, cup 240 ml.
INSERT INTO unit (code, name, dimension, base_factor, standard) VALUES
    ('mg',    'Miligramo',   'MASS',   0.001,     TRUE),
    ('g',     'Gramo',       'MASS',   1,         TRUE),
    ('kg',    'Kilogramo',   'MASS',   1000,      TRUE),
    ('lb',    'Libra',       'MASS',   453.59237, TRUE),
    ('oz',    'Onza',        'MASS',   28.349523, TRUE),
    ('ml',    'Mililitro',   'VOLUME', 1,         TRUE),
    ('l',     'Litro',       'VOLUME', 1000,      TRUE),
    ('tsp',   'Cucharadita', 'VOLUME', 5,         TRUE),
    ('tbsp',  'Cucharada',   'VOLUME', 15,        TRUE),
    ('cup',   'Taza',        'VOLUME', 240,       TRUE),
    ('unit',  'Unidad',      'COUNT',  1,         TRUE),
    ('dozen', 'Docena',      'COUNT',  12,        TRUE);
