package org.santicluke.padronAfip.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "archivos")
public class ArchivoAfip {

	@Id
	private Integer id;
    private Date fecha_archivo_zip;
    @Temporal(TemporalType.DATE)
    private Date fecha_referencia;
    
    public ArchivoAfip(Date fecha_archivo_zip, Date fecha_referencia) {
    	this.fecha_archivo_zip = fecha_archivo_zip;
    	this.fecha_referencia = fecha_referencia;
    }
    
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Date getFecha_archivo_zip() {
		return fecha_archivo_zip;
	}
	
	public void setFecha_archivo_zip(Date fecha_archivo_zip) {
		this.fecha_archivo_zip = fecha_archivo_zip;
	}
	
	public Date getFecha_referencia() {
		return fecha_referencia;
	}
	
	public void setFecha_referencia(Date fecha_referencia) {
		this.fecha_referencia = fecha_referencia;
	}
    
}
