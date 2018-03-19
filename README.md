# Central Delegation Service

Using Wildfly Swarm

## Docs
https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/v/2017.6.1/

### Eclipse users
You will need to enable annotation processing to make use of Immutables.

Install the m2e-apt plugin (via marketplace).
Enable annotation processing either globally (`Window/Preferences/Maven/Annotation Processing`) or per-project.

Full instructions are at [https://immutables.github.io/apt.html](https://immutables.github.io/apt.html)

### Fat Jar

mvn package or mvn wildfly-swarm:run

Custom Main
https://github.com/wildfly-swarm/wildfly-swarm-examples/commit/942a1cf0a478dc3d4894c473ddc1c253216b8933

### Database
comes up when you do

```
docker-compose up
```

You'll need to create databases just once:
```
psql -h localhost -U docker -c 'create database "cds-dev"'
psql -h localhost -U docker -c 'create database "cds-test"'
```

username and password is set to `docker`

### Tests
Integration tests are not run by default, to avoid slowing down the run.
There's a maven profile (`mvn test -P it`) to activate them.

Tests run in Arquillian. That means to debug them, you'll need to attach a remote debugger.
Debugging can be enabled in the running container by editing `src/test/resources/arquillian.launch_file` so
that contains just `debug` - this activates a container config that sets the right jvm args.

Alternatively, you can set the system property `-Darquillian.launch=debug` in a test's run config.

### Migrations
Migrations are driven by Flyway and live under `src/main/resources/db/migration`
There is 1 for structure and 1 for development seed data
```
mvn flyway:migrate
mvn flyway:clean
```

Updating the Database structure and continuing generally follows the following steps.
1. Change persistence.xml to create-drop (don't save it)
2. Adjust your entities
3. `mvn wildfly-swarm:run` to trigger the create-drop
4. Copy the database DDL into the `create_initial_tables` seed (IDE makes this easy)
5. `mvn flyway:clear` followed by `mvn flyway:migrate`
6. Commit changes except for the persistence.xml

At some point we'll lock down the initial data load and migrate properly, i.e. don't change existing migrations.

### Api Calls
Use http-pie.  There are examples under `src/site/api-examples`

### Swagger
`docker-compose` includes a swagger-ui, listening on `8090`.
Point your browser at [http://localhost:8090/?url=http://localhost:8080/api/swagger.json](http://localhost:8090/?url=http://localhost:8080/api/swagger.json) to play with it.

In other environments, the cds will most likely sit behind an httpd service, which will probably give you a different context root and will mess up swagger ui.
You can provide a value for the `swagger.context.root` system property to account for this.

For example, in pocdev, the service definition executes `java -jar /opt/cds/cds-swarm.jar -Dswagger.context.root=cds` because it's sitting
behind a web server that proxies `https://www.pocdev.delegation.org.nz/cds` to `http://app2.pocdev.delegation.org.nz:8080/`

#### Things I learned
Swagger fraction works as advertised. However, you need `src/main/resources/META-INF/swarm.swagger.conf` if you're doing a war deployment (like we are).

Swagger fraction is limited and broken. I've gone with depending directly on io.swagger:swagger-jaxrs and configuring on that basis.
Specifically, processing @BeanParam doesn't work properly with the swagger fraction.