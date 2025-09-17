Este repositorio cuenta con tres directorios

- Desarrollo: Contiene el código fuente del desarrollo de cada uno de los componentes back y front 
- Docker: Contiene todo lo necesario para el consumo de la solución, para guiarse por favor leer el readme.md contenido en ese directorio
- Documentación: Contiene la documentación necesaria respecto al challenge y el diagrama C4

Para ver el consumo cloud

front:

http://34.58.187.162/#/dashboard

grafana:

http://34.28.1.94/d/ee72abae-a043-408a-93bb-20372153d6fe/new-dashboard?orgId=1&from=now-6h&to=now&timezone=browser

user: admin
pass: proxy_meli

consumo del api:
powershell
curl http://35.193.18.51/categories/MLA120350

para la prueba masiva, ejecutar }

k6 run proxy_test_cloud.js