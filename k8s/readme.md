======== Despliegue
Contexto: Para la prueba se realizo el despliegue en kubernet esto para poder tener un stack de las imegenes de mongodb, redis, prometheus, grafana, datadogs, k6 y adicional a los componentes back y front

Pasos:
1. Dentro del directorio "challenge_meli/docker" abrir una consola de Powershell
2. Construir las imagenes necesarias
	-> docker build -t meli-proxy:latest -f Dockerfile.proxy .
	-> docker build -t meli-consulta:latest -f Dockerfile.consulta .
	-> docker build -t meli-front:latest -f Dockerfile.front .
3. Una vez construidas las imagenes, se aplican todos los manifiestos
	-> kubectl apply -f . -R -n meli
		-f . 	→ aplica desde la carpeta actual
		-R 		→ recursivo, incluye subcarpetas
		-n meli → usa el namespace meli
4. Se instalan las metricas
	-> kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
5. Instalar el ingress-nginx
	-> kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml
6. Se aplica el manifiesto del ingress
	-> kubectl apply -f k8s/ingress.yml -n meli
7. Verificar que los pods estan arriba
	-> kubectl get pods -n meli
	-> kubectl get svc -n meli
	-> kubectl get ingress -n meli
	
======== Base de datos

Contexto: Dentro del stack se encuentra la imagen de Mongodb, siendo esta una base de datos NoSQL, permite el registro el almacenamiento de grandes cantidades de datos

Pasos:
1. Ya con el pod de mongodb en estado "Running"
2. Se crea un tunel para poderse conectar a mongodb
	- kubectl port-forward svc/mongodb 27018:27018 -n meli
	Esto dado a que el id de mongodb compass no permite la conexión por medio de servicename

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

1. Ya con el pod del proxy en estado es "Running" se ejecuta el comando dentro del Powershell, para poder realizar el consumo del servicio
	- curl http://proxy.localhost/categories/MLA120350
3. El servicio debe responder StatusCode 200, junto con todo los demás atributos

======== Consulta - Front

Contexto: El microservicio de consulta se desarrollo con springboot, haciendo las consultas a la base de datos MongoDB, exponiendo servicios rest, los cuales son consumidos por el front, este fue desarrollado en angular 16,
dentro de este se muestra la cantidad de consultas de acuerdo al status (200, 429, 503).

Pasos:
1. Para poder acceder al front desde cualquier browser se abre la url
	- http://front.localhost/#/dashboard

======== Test de estres y carga

Contexto: Para el test de carga del proxy se ejecuta en K6 siendo este una herramienta de código abierto, se realiza la implementación de la imagen en el docker

Pasos:

1. Desde el directorio /scripts-k6/ se ejecuta el comando
	- k6 run proxy_test.js

======== Monitoreo

Contexto: Para el monitoreo se utilizó Prometheus para obtener las metricas y se integra con Grafana, donde se muestra el comportamiento de las peticiones en el dashboard

Pasos: 

1. verificar que las imagenes de prometheus y grafana estan arriba con el comando dentro del Powershell
	- docker service ps meli-stack_prometheus --filter "desired-state=running"
	- docker service ps meli-stack_grafana --filter "desired-state=running"
2. Si el estado es "Running" se ejecuta el comando, abrir en browser la siguiente url
	- http://grafana.localhost/d/cffeb4ad-55b5-4dc2-b8ff-ac217dea4945/meli-dashboard?orgId=1&from=now-15m&to=now&timezone=browser&refresh=5s
	