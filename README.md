# quarkus-fhir
ensemble de microservices fhir construits à partir de jaxrs server du projet HapiFhir.

Trois services Patient, Apointment, et Practitioner
L'ensemble accessible via un proxy nginx

le but est d'expérimenter 
1) l'approche jax-rs de HapiFhir (bien qu'elle ne soit pas directement développée part l'équipe Hapi).
2) la vision globale de toutes les ressources disponibles.
3) la gestion globale les capacités du service.


HapiFhir fournit les bases pour implémenter un serveur FHIR monolithique. Il est basé sur les servlets.
Un groupe a fait l'effort d'adapter ce code à jax-rs pour pouvoir l'intégrer dans un serveur JEE.

Cette adaptation, bien que présente dans le repo de Hapi, n'est pas maintenue par l'équipe principale.
Elle est le fait d'un groupe d'utilisateurs d'Hapi et ne semble pas être très suivie.

Constitution des services
3 microservices : Patient, Apointment, Practitioner.

Les trois sont basées sur AbstractJaxRsResourceProvider et AbstractJaxRsConformanceProvider
Les trois projets quarkus n'implémentent que ces deux ressources.
La première définit la ressource FHIR exposée et la seconde fournit conformément à la norme les capacités du microservice.

les trois sont accessibles via nginx

    location /Patient {
      proxy_pass http://localhost:8080;
    }

    location /Appointment {
      proxy_pass http://localhost:8081;
    }

    location /Practitioner {
      proxy_pass http://localhost:8082;
    }

Contrairement à un serveur monolithique dans le cas de micros services chaque microserveur fourni sa propre URL /metadata donnant les capacités de celui-ci

Ce qui rend l'appel via nginx impossible puisque pour être conforme il devrait fournir un seul point d'accès /metadata donnant les capacités des trois microservices.

Pour résoudre le problème, le projet metadata fournit un point d'accès qui agrège les capacités de tous les microservices déclarés dans sa config.

## Quarkus et Hapi

Les approches de Hapi et de Quarkus sont fondamentalement différentes. Hapi utilise l'introspection et reporte à l'exécution un grand nombre d'opérations là ou qQuakus cherche à en faire le maximum à la compilation.

Hapi serveur cherche à couvrir l'ensemble du périmètre fonctionnel de Fhir là ou Quakus cherche à rendre le plus indépendant possible chaque sous-domaine fonctionnel.

Malgré ces divergences l'exécution de jaxrsServeur d'Hapi sous Quarkus est simple.

La librairie jaxrsSereveur d'Hapi n'est pas bien supportée.

Une extension Quarkus s'appuyant sur la lib core de Hapi et produisant à la compilation et au runtime l'équivalent de jaxrsServer semble plus pertinente.

La mise en oeuvre d'un centralisateur des "Capabilities" est simple et efficace.

Hapi gère dans la session utilisateur la pagination des réponses au travers de AbstractJaxRsPageProvider
C'est élément est inutilisable dans Quakus qui est sessionless de plus il est bogué, la réponse ne fournit pas la bonne URL pour la page suivante.

Ce point est entièrement à revoir.

##Build & run

     ./mvnw install -Dquarkus.container-image.build=true -Dquarkus.container-image.build=true -Dquarkus.container-image.group=fhir

Utiliser le fichier docker-compose.yml pour démarrer le serveur.
