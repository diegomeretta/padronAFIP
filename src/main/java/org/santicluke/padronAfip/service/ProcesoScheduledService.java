package org.santicluke.padronAfip.service;

import org.santicluke.padronAfip.Proceso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProcesoScheduledService {
	
	@Autowired
	Proceso proceso;
	
	@Scheduled(cron = "0 0 12 * * *")
    public void ejecutarProcesoDiario() {        
        proceso.correr();
    }	

}
