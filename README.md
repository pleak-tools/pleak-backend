# Pleak backend
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
- MySQL 5.5+

- PostgreSQL installed and database + user (named 'ga_propagation') created, following instructions in [pleak-sql-constraint-propagation](https://github.com/pleak-tools/pleak-sql-constraint-propagation) repository. This is required for [pleak-guessing-advantage-editor](https://github.com/pleak-tools/pleak-guessing-advantage-editor).

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

## Create dev users
Make sure that the users described in ```src/main/resources/db/dev.sql``` do not make it to a live server.
```mysql -u root -p < src/main/resources/db/dev.sql```

## Backup database
```. backup.sh```

## Testing
Make sure that server is running and you have the following users (see above to create dev users):

```
var user1 = {
  "credentials": {
    "email": "test1@example.com",
    "password": "test1"
  }
};

var user2 = {
  "credentials": {
    "email": "test2@example.com",
    "password": "test2"
  }
};

var user3 = {
  "credentials": {
    "email": "test3@example.com",
    "password": "test3"
  }
};
```

Navigate to test directory

```cd test```

If jasmine-node is not installed:

```npm install```

Automated tests

```jasmine-node test_spec.js```

# API

## POST /rest/directories

```
{
  title: string,
  directory: {
    title: string,
    id: number
  }
}
```

Creates a new directory with parent as
- root when ```directory.title = 'root'```
- specific directory when ```directory.id``` has a value of an existing directory ```id``` (which can also be root)

and returns the directory.

## GET /rest/directories/root
Returns root directory.

## GET /rest/directories/share
Returns share directory.

## GET /rest/directories/{id}
Returns directory.

## PUT /rest/directories/{id}

```
{
  title: string,
  directory: {
    title: string,
    id: number
  },
  permissions: [
    {
      user: {
        email: string
      },
      action: {
        title: string
      }
    },
    ...
  ],
}
```

Updates (or creates a new) directory and returns it. The permission object can have ```action.title = 'view'``` or ```action.title = 'edit'```.

## DELETE /rest/directories/{id}
Deletes directory.

## POST /rest/directories/files/

```
{
  title: string,
  directory: {
    title: string,
    id: number
  }
}
```

Creates a new file and returns it. Also inherits permissions from parent directory where applicable. Parent directory is
- root when ```directory.title = 'root'```
- specific directory when ```directory.id``` has a value of an existing directory ```id``` (which can also be root).

## GET /rest/directories/files/{id}
Returns file.

## PUT /rest/directories/files/{id}

```
{
  title: string,
  directory: {
    title: string,
    id: number
  },
  content: string,
  published: boolean,
  permissions: [
    {
      user: {
        email: string
      },
      action: {
        title: string
      }
    },
    ...
  ]
}
```

Updates and returns file. Also inherits permissions from parent directory where applicable. Parent directory is
- root when ```directory.title = 'root'```
- specific directory when ```directory.id``` has a value of an existing directory ```id``` (which can also be root).

Creates a public URI for file when ```file.published = true``` or deletes the public URI when ```file.published = false```. Updates file contents when server-side ```file.md5Hash``` matches with supplied ```file.md5Hash``` and creates a new ```file.md5Hash```.

## DELETE /rest/directories/files/{id}
Deletes file.

## GET /rest/directories/files/public/{uri}
Returns public file.


There are also REST endpoints for different Pleak analysers.


## License

MIT
