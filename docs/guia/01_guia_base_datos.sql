-- Ejecutar en el SQL Editor de Supabase antes de iniciar esta versión.
-- Solo inserta categorías ausentes; repetirlo no sobrescribe ediciones posteriores.
BEGIN;
CREATE TABLE IF NOT EXISTS public.guia_contenedores (
    slug varchar(80) PRIMARY KEY,
    nombre varchar(120) NOT NULL,
    color varchar(30) NOT NULL,
    simbolo varchar(16) NOT NULL,
    descripcion text NOT NULL,
    preparacion text NOT NULL,
    fuente_url varchar(1000) NOT NULL,
    orden integer NOT NULL
);
CREATE TABLE IF NOT EXISTS public.guia_contenedor_ejemplos (
    contenedor_slug varchar(80) NOT NULL REFERENCES public.guia_contenedores(slug) ON DELETE CASCADE,
    posicion integer NOT NULL CHECK (posicion >= 0),
    ejemplo varchar(250) NOT NULL,
    PRIMARY KEY (contenedor_slug, posicion)
);
-- La aplicación consulta mediante JDBC; estas tablas no se exponen al cliente web.
ALTER TABLE public.guia_contenedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.guia_contenedor_ejemplos ENABLE ROW LEVEL SECURITY;

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('organicos', 'Orgánicos', 'verde', '♧', 'Restos vegetales para compostaje, cuando exista un programa que los reciba.', 'Sepáralos de los envases. Para compostaje doméstico evita carnes, lácteos y aceites.', 'https://www.epa.gov/recycle/composting-home', 0)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Cáscaras de frutas'),
    (1, 'Restos de verduras'),
    (2, 'Hojas secas'),
    (3, 'Posos de café')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('carton', 'Cartón', 'cafe', '▱', 'Cajas y empaques de cartón.', 'Retira restos de comida y aplana las cajas.', 'https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes', 1)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Cajas de cereal'),
    (1, 'Cajas de zapatos')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('plasticos', 'Plásticos', 'azul', '♳', 'Envases aceptados por el programa local.', 'Vacía y limpia. El símbolo del plástico no garantiza su aceptación.', 'https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes', 2)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Botellas de agua'),
    (1, 'Envases plásticos')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('metales', 'Metales', 'gris', '◎', 'Envases metálicos recuperables.', 'Vacía y retira restos de comida. Confirma qué metales reciben.', 'https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes', 3)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Latas de bebidas'),
    (1, 'Latas de conservas')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('papel', 'Papel', 'amarillo', '▤', 'Productos de papel para recuperación.', 'Consérvalos limpios y secos. Consulta antes de llevar papel laminado.', 'https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes', 4)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Periódicos'),
    (1, 'Revistas'),
    (2, 'Hojas de papel')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('multicapa', 'Envases multicapa', 'naranja', '▧', 'Envases de bebidas que combinan distintos materiales.', 'Consulta al centro si acepta estos envases y cómo entregarlos. No asumas que van con el cartón.', 'https://www.epa.gov/recycle/frequent-questions-recycling', 5)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Cartones de leche'),
    (1, 'Cartones de jugo')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('vidrio', 'Vidrio', 'celeste', '♙', 'Envases de vidrio de alimentos y bebidas.', 'Retira tapas metálicas. No deposites vidrio roto en el contenedor común.', 'https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes', 6)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Botellas de vidrio'),
    (1, 'Frascos de alimentos')
) AS datos(posicion, ejemplo);

WITH nueva AS (
    INSERT INTO public.guia_contenedores
        (slug, nombre, color, simbolo, descripcion, preparacion, fuente_url, orden)
    VALUES ('especiales', 'Manejo especial', 'negro', '!', 'Residuos que requieren una recepción específica.', 'No los mezcles con reciclables comunes. Consulta un punto especializado antes de llevarlos.', 'https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes', 7)
    ON CONFLICT (slug) DO NOTHING
    RETURNING slug
)
INSERT INTO public.guia_contenedor_ejemplos (contenedor_slug, posicion, ejemplo)
SELECT nueva.slug, datos.posicion, datos.ejemplo
FROM nueva CROSS JOIN (VALUES
    (0, 'Pilas y baterías'),
    (1, 'Aparatos electrónicos')
) AS datos(posicion, ejemplo);

COMMIT;
