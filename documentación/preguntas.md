- Diagrama de la arquitectura de la solución
	- Considera puntos de falla
		- Respuesta: 
			- Se no se hace un balanceo correcto se puede colapsar el proxy
			- Api de mercadolibre caida

	- Cómo escalaría el producto y la solución: 
		- Respuesta: 
			- Horizontal scaling: Réplicas del proxy detrás de un Load Balancer.
			- Stateless services: Facilita el escalado sin compartir estado.
			- Rate limit distribuido: Usar Redis para compartir límites entre instancias.
			   
	- ¿Qué mejoras le podrías hacer a largo plazo? 
		- Respuesta:
			- Autoajuste de límites con GenAI
			- Análisis predictivo de tráfico
			- Dashboard de métricas para stakeholders
			- API Gateway para centralizar control


	- ¿Cómo determinarías qué cosas mejorar y/o reparar?
		- Respuesta:
			- Analizar métricas de latencia, errores y throughput.
			- Revisar logs y trazas con observabilidad.
			- Usar GenAI para detectar anomalías o patrones de abuso

- ¿Qué restricciones tendría tu solución en caso de escalar?
	- Respuesta:
			- Persistencia de estado: Si el rate limit depende de una base de datos central, puede limitar la escalabilidad.
			- Consistencia de métricas: En sistemas distribuidos, mantener métricas precisas es complejo.
			- Costo de infraestructura: Más nodos, más monitoreo, más complejidad.

- Agregar pruebas unitarias y de integración a los flujos críticos del challenge
	-Pruebas unitarias:
		-Respuesta:
			- Validación de reglas de rate limit.
			- Formateo correcto de requests/responses.
			- Manejo de errores HTTP

	-Pruebas de integración:
		-Respuesta:
			- Flujo completo desde cliente → proxy → API externa.
			- Simulación de múltiples IPs y paths.
			- Validación de límites aplicados correctamente

	- ¿Existe algún otro tipo de pruebas que se puedan agregar? Escuchamos tus propuestas
		-Respuesta:
			- Pruebas de carga (stress testing): Simula miles de requests por segundo.
			- Pruebas de resiliencia: ¿Qué pasa si la API externa falla?
			- Pruebas de seguridad: Inyección, autenticación, rate limit bypass.
			- Pruebas de observabilidad: Validar que se generan métricas y logs correctamente. 

- ¿Qué consideraciones de observabilidad tendrías que valorar para poder comprender mejor el producto?
	- Respuesta: 
		- Prometheus: Recolección de métricas.
		- Grafana: Visualización de dashboards.
		- ELK Stack o OpenTelemetry: Logs y trazas distribuidas
		