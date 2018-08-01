# Proyecto de ejemplo para Transbank SDK Java

## Requerimientos

Para poder ejecutar el proyecto de ejemplo necesitas tener instalada las siguientes herramientas
en tu computador:

1. git ([como instalar git][git_install])
2. docker y docker-compose. ([como instalar docker][docker_install])
3. maven ([como instalar maven][maven_install])

[git_install]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
[docker_install]: https://docs.docker.com/install/
[maven_install]: https://maven.apache.org/install.html

## Clonar repositorio

Primero deberas clonar este repositorio en tu computador:

````batch
git clone https://github.com/TransbankDevelopers/transbank-sdk-java-example.git
````

## Empaquetar

Una vez que haz terminado de clonar el repositorio debes acceder a la carpeta 
```transbank-sdk-java-example.``` y ejecutar el siguiente comando:

````batch
mvn clean package
````

Una vez que termine de empaquetar se creara una carpeta ```target``` en el mismo
directorio dentro de la cual debemos encontrar un archivo con extensión ```.war```

## Ejecutar ejemplo

El ejemplo viene listo para correr en ```docker-compose``` por lo tanto para
ejecutarlo deberas correr el siguiente comando en la carpeta raíz del proyecto
ejemplo:

````batch
docker-compose up
````

Si todo ha salido bien deberías poder acceder al ejemplo en la url 
http://localhost:8081/onepay-sdk-example/