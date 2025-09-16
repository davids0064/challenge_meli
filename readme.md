Este repositorio cuenta con tres directorios

- Desarrollo: Contiene el código fuente del desarrollo de cada uno de los componentes back y front 
- Docker: Contiene todo lo necesario para el consumo de la solución, para guiarse por favor leer el readme.md contenido en ese directorio
- Documentación: Contiene la documentación necesaria respecto al challenge y el diagrama C4

Para ver el consumo cloud

front:

http://34.10.194.128/#/dashboard

grafana:

http://34.69.105.197/d/d1733f40-27d7-4d96-b487-0524cf3ae8da/meli?orgId=1&from=now-5m&to=now&timezone=browser&refresh=auto

user: admin
pass: proxy_meli

consumo del api:
powershell
curl http://34.172.64.199/categories/MLA120350

para la prueba masiva, ejecutar }

k6 run proxy_test_cloud.js