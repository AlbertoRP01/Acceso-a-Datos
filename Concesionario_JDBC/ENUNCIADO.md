# Enunciado — Concesionario JDBC (AD RA2)

Apertura: martes, 11 de noviembre de 2025 · Cierre: jueves, 20 de noviembre de 2025

Se debe desarrollar un programa en Java que gestione un concesionario de coches, persistiendo la información en una base de datos relacional (MySQL) y ofreciendo una alternativa a una base de datos embebida (SQLite).

## Menú de texto

1. **Conectar BBDD**: permitir al usuario elegir (y probar) la conexión (SQLite o MySQL) leyendo los datos del fichero `config.properties`.
2. **Inicializar/Crear Tablas**: ejecutar el DDL (`CREATE TABLE IF NOT EXISTS...`) para asegurar que las tablas `propietarios`, `coches` y `traspasos` existen.
3. **Registrar Propietario**: dar de alta un nuevo propietario (cliente) en la BBDD.
4. **Insertar Coche**: pedir por consola los datos de un nuevo vehículo e insertarlo (INSERT). El coche inicialmente no tendrá propietario (pertenece al concesionario).
5. **Importar Coches (CSV)**: leer un fichero CSV y añadir los coches a la base de datos. La operación debe ser transaccional (o se insertan todos, o ninguno).
6. **Listar Coches (Concesionario)**: mostrar por consola todos los coches que NO tienen propietario.
7. **Listar Coches (Propietarios)**: mostrar por consola todos los coches que SÍ tienen propietario, indicando el DNI del mismo (JOIN).
8. **Modificar Coche**: pedir una matrícula, solicitar los nuevos datos (excepto la matrícula) y actualizar el registro.
9. **Borrar Coche**: pedir una matrícula y eliminar el registro.
10. **Realizar Traspaso**: solicitar el DNI del comprador, la matrícula del coche y el monto económico.
    - Debe verificar que el comprador existe.
    - Debe verificar que el coche existe.
    - Debe realizar la operación de forma transaccional.
11. **Ejecutar Procedimiento Almacenado (solo MySQL)**: llamar a un procedimiento almacenado (ej. `sp_coches_por_marca`) y mostrar el resultado.
12. **Generar Informe Resumen**: crear un fichero `informe_concesionario.txt` con:
    - Número total de coches (`COUNT(*)`).
    - Listado de coches agrupados por marca.
    - El extra o equipamiento que más se repite.

## Apreciaciones

- La creación del schema, la estructura de la BBDD y el procedimiento almacenado debe hacerse **desde el código Java**, no directamente en MySQL Workbench.
- **Fichero de configuración**: la aplicación NO debe tener los datos de conexión escritos en el código. Usar `config.properties` para drivers, URLs, user, pass y rutas de ficheros (CSV, informe).
- **Gestores BBDD**: el código debe poder operar con SQLite (embebida) y MySQL (independiente).
- **Clave primaria**: `matricula` en coches y `dni` en propietarios son únicos. Gestionar las `SQLException` correspondientes.
- **Gestión de recursos**: usar try-with-resources cuando se deba.
- **Transacciones**: commit y rollback correctos en la importación de CSV y en los traspasos.
- **Excepciones**: gestionar de forma específica (`SQLException`, `IOException`, `NumberFormatException`, etc.).
- **Documentación**: el código debe incluir Javadoc.
- **Código limpio**: se valora no repetir código y usar métodos auxiliares.

## Evaluación

Prueba individual en clase: preguntas sobre el código o una pequeña modificación en vivo (p. ej. "Añade un nuevo campo" o "Crea una nueva consulta"). Hay que superarla para tener nota distinta de 0.

## Entrega

Fichero `AD_RA2_Apellidos_Nombre.rar` con:
- Carpeta completa del proyecto IntelliJ.
- `schema_concesionario.sql` con el DDL y el `CREATE PROCEDURE` para MySQL.
- `LEEME.txt` con ventajas e inconvenientes de los conectores JDBC y de BBDD embebidas (SQLite) frente a servidor (MySQL).

## Criterios de evaluación (RA2)

a) Ventajas e inconvenientes de utilizar conectores.
b) Gestores de bases de datos embebidos e independientes.
c) Conector idóneo en la aplicación.
d) Conexión establecida.
e) Estructura de la base de datos definida.
f) Aplicaciones que modifican el contenido de la base de datos.
g) Objetos destinados a almacenar el resultado de las consultas.
h) Aplicaciones que efectúan consultas.
i) Eliminación de los objetos una vez finalizada su función.
j) Gestión de transacciones.
k) Ejecución de procedimientos almacenados.

Material adjunto en la tarea: `ejemplo CSV.csv` (ahora en `src/main/resources/coches.csv`) y `modelo de dominio.png`.
