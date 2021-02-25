### Contenu de l'archive :

- Dossiers :
    1. `src/` contient le code source du projet.
    2. `www/` contient les fichiers .html qui seront utilisés pour la démonstration du 
       fonctionnement du serveur, ainsi que la Javadoc du code source (la Javadoc peut-être affichée par 
       le serveur une fois celui-ci mis en route.).
- Fichiers :
    1. `README.md` Ce fichier, contient les explications pour l'utilisation du programme.
    2. `Rapport.pdf` Contient le rapport décrivant le travail réalisé.
    3. `serverpub.cer` Certificat à entrer dans les Trusted Root Certification Authorities du navigateur web
        Pour qu'il reconnaisse le certificat comme authentique.
    4. `TEAKeyStoreServer.jks` Le KeyStore contenant le certificat qui sera utilisé par le serveur.
    5. `TEA.jar` Contient le projet pré-compilé et prêt à exécuter.

<i>Le code présent dans cette archive a été développé, testé fonctionnel avec openJDK 11.0.10</i>

### Mise en route :

Deux options sont possibles;<br>
La première est d'utiliser le fichier .jar contenant le serveur pré-compilé, pour l'exécuter
il suffit d'effectuer la commande suivante :
-   `java -jar TEA.jar` <br>
<i>Attention le fichier .jar doit se trouver dans le même dossier que le dossier `www/` et le fichier `TEAKeyStoreServer.jks`</i>

La deuxième option est de compiler le code source soi-même en effectuant la commande suivante :
```
    javac .\src\main\java\ClientHanding\*.java .\src\main\java\IOHandling\*.java .\src\main\java\Server\*.java .\src\main\java\*.java -d out/ 
```
<br>

Puis exécuter les classes compilées grace à la commande :
-   `java -classpath .\out\ Main` <br>
    <i>Attention le dossier out/ doit se trouver dans le même dossier que le dossier `www/` et le fichier `TEAKeyStoreServer.jks`</i>


#### Informations supplémentaires :

Le certificat utilisé par le serveur a été généré via les commandes suivantes :

```
keytool -genkeypair -alias localhostserver -ext san=dns:localhost,dns:127.0.0.1,ip:127.0.0.1,ip:::1 -keyalg RSA -keypass teapassword -storepass teapassword -keystore TEAKeyStoreServer.jks
```
- `keytool -exportcert -alias localhostserver -file serverpub.cer -keystore TEAKeyStoreServer.jks -storepass teapassword`
- Ajouter le certificat exporté aux Trusted Root CA de l'ordinateur.
- Profit.