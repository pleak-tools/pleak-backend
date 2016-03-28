# Welcome
This is a new and improved repository for pleak-backend that uses:
- [Maven](https://maven.apache.org/)
- [Tomcat](http://tomcat.apache.org/)
- [Jersey](https://jersey.java.net/)
- [MySQL](https://www.mysql.com/)
- [Hibernate](http://hibernate.org/)
- [JSON Web Tokens](https://jwt.io/)

## Requirements
- Java 1.8
- Maven 3.0+
- MySQL 5.5+```

## Install Maven
```sudo apt-get install maven```

## Navigate to the project root directory

## Setup database
```mysql -u root -p < src/main/resources/db/setup.sql```

## Migrate database
```mvn flyway:migrate```

## Build application and start tomcat
```mvn tomcat:run```

## Clean database
```mvn flyway:clean```

## API

### All files
```GET /rest/file```

### File
```GET /rest/file/{id}```

### Save
```POST /rest/file```

### Delete
```DELETE /rest/file/{id}```