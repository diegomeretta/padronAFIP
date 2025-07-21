package org.santicluke.padronAfip.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "contribuyentes")
public class Contribuyente {

	@Id
	private String cuit;
	private String denominacion;
	private Integer impuesto_ganancias_id;
	private Integer impuesto_iva_id;
	private String monotributo;
	private String integrante_sociedades;
	private String empleador;
	private String actividad_monotributo;
	@Temporal(TemporalType.DATE)
	private Date fecha;
	
	public Contribuyente() {}
	
    public Contribuyente(String cuit, String denominacion, Integer impuestoGananciasId, 
            Integer impuestoIvaId, String monotributo, String integranteSociedades,
            String empleador, String actividadMonotributo, Date fecha) {
		this.cuit = cuit;
		this.denominacion = denominacion;
		this.impuesto_ganancias_id = impuestoGananciasId;
		this.impuesto_iva_id = impuestoIvaId;
		this.monotributo = monotributo;
		this.integrante_sociedades = integranteSociedades;
		this.empleador = empleador;
		this.actividad_monotributo = actividadMonotributo;
		this.fecha = fecha;
	}
}
