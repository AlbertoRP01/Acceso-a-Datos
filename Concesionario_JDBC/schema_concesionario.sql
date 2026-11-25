-- ==========================================================
--  schema_concesionario.sql
--  Estructura de la base de datos del concesionario (MySQL)
--
--  Es el mismo DDL que ejecuta el programa desde Java
--  (clases ConexionDB y CreadorTablas, opciones 1 y 2 del menú).
--  Este fichero es para consultarlo o para ejecutarlo a mano
--  en MySQL Workbench; el programa NO lo necesita.
-- ==========================================================

-- ---------- Base de datos ----------
CREATE DATABASE IF NOT EXISTS concesionario;
USE concesionario;


-- ---------- Tablas ----------
-- El orden importa: primero las tablas a las que apuntan las
-- claves foráneas (propietarios, extra) y después las que las usan.

-- Propietarios (clientes). El DNI no se puede repetir.
CREATE TABLE IF NOT EXISTS propietarios (
    id_propietario INT AUTO_INCREMENT PRIMARY KEY,
    dni            VARCHAR(9)   NOT NULL UNIQUE,
    nombre         VARCHAR(50)  NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    telefono       VARCHAR(15)
);

-- Coches. La matrícula es la clave primaria.
-- id_propietario = NULL significa que el coche es del concesionario.
-- Si se borra el propietario, el coche vuelve a ser del concesionario.
CREATE TABLE IF NOT EXISTS coches (
    matricula      VARCHAR(10)   PRIMARY KEY,
    marca          VARCHAR(50)   NOT NULL,
    modelo         VARCHAR(50)   NOT NULL,
    precio         DECIMAL(10,2) NOT NULL,
    id_propietario INT NULL,
    FOREIGN KEY (id_propietario) REFERENCES propietarios(id_propietario)
        ON DELETE SET NULL
);

-- Catálogo de extras (equipamiento). El nombre no se repite.
CREATE TABLE IF NOT EXISTS extra (
    id_extra INT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(50) NOT NULL UNIQUE
);

-- Relación N:M entre coches y extras.
-- La clave primaria es la pareja: un coche no puede tener el mismo extra dos veces.
-- Si se borra el coche, se borran sus extras (CASCADE).
CREATE TABLE IF NOT EXISTS coche_extra (
    matricula VARCHAR(10) NOT NULL,
    id_extra  INT         NOT NULL,
    PRIMARY KEY (matricula, id_extra),
    FOREIGN KEY (matricula) REFERENCES coches(matricula) ON DELETE CASCADE,
    FOREIGN KEY (id_extra)  REFERENCES extra(id_extra)
);

-- Traspasos (ventas).
-- id_vendedor = NULL significa que vende el concesionario (primera venta del coche).
-- Si se borra el coche, se borra su historial de traspasos (CASCADE).
CREATE TABLE IF NOT EXISTS traspasos (
    id_traspaso  INT AUTO_INCREMENT PRIMARY KEY,
    matricula    VARCHAR(10)   NOT NULL,
    id_vendedor  INT NULL,
    id_comprador INT NOT NULL,
    monto        DECIMAL(10,2) NOT NULL,
    fecha        DATE          NOT NULL,
    FOREIGN KEY (matricula)    REFERENCES coches(matricula) ON DELETE CASCADE,
    FOREIGN KEY (id_vendedor)  REFERENCES propietarios(id_propietario),
    FOREIGN KEY (id_comprador) REFERENCES propietarios(id_propietario)
);


-- ---------- Procedimiento almacenado ----------
-- Devuelve los coches de una marca con el DNI de su propietario
-- (NULL si el coche es del concesionario).
--
-- DELIMITER cambia el fin de sentencia a // para que Workbench no
-- corte el procedimiento en el ";" de dentro del BEGIN...END.
-- Desde Java no hace falta: cada sentencia se envía por separado.

DROP PROCEDURE IF EXISTS sp_coches_por_marca;

DELIMITER //
CREATE PROCEDURE sp_coches_por_marca(IN p_marca VARCHAR(50))
BEGIN
    SELECT c.matricula, c.marca, c.modelo, c.precio, c.id_propietario, p.dni
    FROM coches c
    LEFT JOIN propietarios p ON c.id_propietario = p.id_propietario
    WHERE c.marca = p_marca
    ORDER BY c.modelo, c.matricula;
END //
DELIMITER ;

-- Ejemplo de uso:
-- CALL sp_coches_por_marca('Seat');
