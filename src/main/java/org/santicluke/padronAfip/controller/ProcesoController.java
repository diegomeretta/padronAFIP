package org.santicluke.padronAfip.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.santicluke.padronAfip.Proceso;
import org.santicluke.padronAfip.model.Contribuyente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proceso")
public class ProcesoController {

	@Autowired
	Proceso proceso;
	
	@GetMapping("/comenzar")
	public ResponseEntity<String> obtenerUsuario() {
	    CompletableFuture.runAsync(() -> {
            proceso.correr();
	    });
	    return ResponseEntity.ok("Proceso iniciado en background");
	}
	
	@GetMapping("/contribuyente/{denominacion}")
	public ResponseEntity<List<Contribuyente>> obtenerContribuyentesPorApellido(@PathVariable String denominacion) {
		List<Contribuyente> contribuyentes = proceso.obtenerContribuyentes(denominacion);
	    return ResponseEntity.ok(contribuyentes);
	}
	
}
