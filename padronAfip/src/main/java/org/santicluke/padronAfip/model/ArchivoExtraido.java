package org.santicluke.padronAfip.model;

public class ArchivoExtraido {
	private final byte[] contenido;
	private final String nombre;
	    
    public ArchivoExtraido(byte[] contenido, String nombre) {
        this.contenido = contenido;
        this.nombre = nombre;
    }
    
    public byte[] getContenido() {
        return contenido;
    }
    
    public String getNombre() {
        return nombre;
    }
}
