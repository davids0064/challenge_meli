======== Despliegue
Contexto: Para la prueba se realizo el despliegue en docker swarm esto para poder tener un stack de las imegenes de mongodb, redis, prometheus, grafana, datadogs, k6 y adicional a los componentes back y front, se escogio swarm 
para poder tener un despliegue de replicas del servicio de proxy, esto como ejemplo de escalabilidad  por la cantidad de peticiones

Pasos:
1. Dentro del directorio "challenge_meli/docker" abrir una consola de Powershell
2. Construir las imagenes del back y front
	- docker build -t meli-proxy:latest -f Dockerfile.proxy .
	- docker build -t meli-consulta:latest -f Dockerfile.consulta .
	- docker build -t meli-front:latest -f Dockerfile.front .
3. Se debe iniciar el docker swarm
	- docker swarm init
4. Una vez construidas las imagenes, se debe levantar el resto de las imagenes con el archivo docker-compose.yml, ejecutando el comando
	- docker stack deploy -c docker-compose.yml meli-stack
	
======== Base de datos

Contexto: Dentro del stack se encuentra la imagen de Mongodb, siendo esta una base de datos NoSQL, permite el registro el almacenamiento de grandes cantidades de datos, siend asi muy escalable

Pasos:
1. verificar que la imagen de Mongodb esta arriba con el comando
	- docker service ps meli-stack_mongodb --filter "desired-state=running"
2. Si el estado es "Running" se ejecuta el comando
3. En MongoDB compass se crea una nueva conexión con la siguiente URI
	- mongodb://localhost:27018

======== Proxy

Contexto: El proxy fue desarrollado en springboot, implementando control de tráfico y resiliencia para el proxy MELI. Aplica reglas de rate limiting configurables por IP y ruta, registra cada solicitud 
con su estado HTTP, y protege el backend con circuit breakers reactivos.
#### Tecnologías clave
- Spring Cloud Gateway (WebFlux)
- Redis reactivo (Spring Data)
- Circuit Breaker (Resilience4j)
- Logging estructurado (Slf4j + servicio personalizado)
- Configuración externa de reglas (RateLimitConfig)
#### Comportamiento
- Rechaza solicitudes que exceden el límite configurado (`429`)
- Protege el backend de fallos (`503`)
- Registra cada solicitud con IP, ruta y estado
- Permite configuración dinámica de reglas
#### Extensiones posibles
- Integración con Prometheus para métricas
- Persistencia de logs en MongoDB 
- Dashboards en Grafana para visualización en tiempo real 

Pasos: 

1. verificar que la imagen del proxy esta arriba con el comando, es de aclarar que este tiene como escalabilidad el despliegue de 3 replicas
	- docker service ps meli-stack_proxy --filter "desired-state=running"
2. Si el estado es "Running" se ejecuta el comando dentro del Powershell, para poder realizar el consumo del servicio
	- curl http://127.0.0.1:8080/categories/MLA120350
3. El servicio debe responder StatusCode 200, junto con todo los demás atributos

======== Consulta - Front

Contexto: El microservicio de consulta se desarrollo con springboor, haciendo las consultas a la base de datos MongoDB, exponiendo servicios rest, los cuales son consumidos por el front, este fue desarrollado en angular 16,
dentro de este se muestra la cantidad de consultas de acuerdo al status (200, 429, 503).

Pasos:
1. verificar que la imagen del micro de consulta y front estan arriba con el comando
	- docker service ps meli-stack_consulta --filter "desired-state=running"
	- docker service ps meli-stack_front --filter "desired-state=running"
2. En cualquier browser se abre la url
	- http://localhost:8082/#/dashboard

======== Test de estres y carga

Contexto: Para el test de carga del proxy se ejecuta en K6 siendo este una herramienta de código abierto, se realiza la implementación de la imagen en el docker

Pasos:

1. verificar que la imagen de K6 esta arriba con el comando dentro del Powershell
	- docker service ps meli-stack_k6-runner --filter "desired-state=running"
2. Si el estado es "Running" se ejecuta el comando
	- k6 run proxy_test.js
    - Este lanza consumo a los path
		- http://localhost:8080/items/MLA120350
		- http://localhost:8080/categories/MLA120350
3. Para poder ver los logs por favor abrir el readme-logs.txt

======== Monitoreo

Contexto: Para el monitoreo se utilizó Prometheus para obtener las metricas y se integra con Grafana, donde se muestra el comportamiento de las peticiones en el dashboard

Pasos: 

1. verificar que las imagenes de prometheus y grafana estan arriba con el comando dentro del Powershell
	- docker service ps meli-stack_prometheus --filter "desired-state=running"
	- docker service ps meli-stack_grafana --filter "desired-state=running"
2. Si el estado es "Running" se ejecuta el comando, abrir en browser la siguiente url
	- http://localhost:3000/d/d47253c5-3aa3-46dc-9361-11ce13d00d1b/meli?orgId=1&from=now-30m&to=now&timezone=browser&refresh=5s