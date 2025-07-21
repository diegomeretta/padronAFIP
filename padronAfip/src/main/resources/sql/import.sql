USE padron;
CREATE TABLE contribuyentes (cuit varchar(11), denominacion varchar(30),
	impuesto_ganancias_id INT, impuesto_iva_id INT, monotributo varchar(2),
	integrante_sociedades varchar(1), empleador varchar(1),
  actividad_monotributo varchar(2), fecha date);
	
USE padron;
CREATE TABLE referencias (id INT, descripcion varchar(30));
INSERT INTO referencias (id, descripcion) VALUES (1, 'No Inscripto'), (2, 'Activo'), (3, 'Exento'),
	(4, 'No alcanzado'), (5, 'Exento no alcanzado'), (6, 'Activo no alcanzado'), (7, 'No corresponde');

USE padron;
CREATE TABLE archivos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_archivo_zip TIMESTAMP NOT NULL,
    fecha_referencia DATE NOT NULL
);
