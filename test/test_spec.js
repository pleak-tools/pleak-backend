var frisby = require("frisby");
var DisplayProcessor = require('./node_modules/jasmine-spec-reporter/src/display-processor');

function TimeProcessor(options) {
}

function getTime() {
    var now = new Date();
    return now.getHours() + ':' +
           now.getMinutes() + ':' +
           now.getSeconds()
}

TimeProcessor.prototype = new DisplayProcessor();

TimeProcessor.prototype.displaySuite = function (suite, log) {
  return getTime() + ' - ' + log;
};

TimeProcessor.prototype.displaySuccessfulSpec = function (spec, log) {
  return getTime() + ' - ' + log;
};

TimeProcessor.prototype.displayFailedSpec = function (spec, log) {
  return getTime() + ' - ' + log;
};

TimeProcessor.prototype.displayPendingSpec = function (spec, log) {
  return getTime() + ' - ' + log;
};

var SpecReporter = require('jasmine-spec-reporter');

var reporter = new SpecReporter({
    customProcessors: [TimeProcessor]
});

jasmine.getEnv().addReporter(reporter);

var host = "http://localhost:8080/pleak-backend/rest";

var api = {
  auth: {
    check: host + "/auth",
    login: host + "/auth/login",
    logout: host + "/auth/logout"
  },
  directory: {
    new: host + "/directories/",
    root: host + "/directories/root/",
    shared: host + "/directories/shared/"
  },
  file: {
    new: host + "/directories/files/",
    public: host + "/directories/files/public/"
  }
};

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

var updateJwt = function(jwt) {
  frisby.globalSetup({
    request: {
      headers: {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "json-web-token": jwt
      }
    }
  });
};

console.log("\nPLEAK-BACKEND TESTS\n");

frisby.create("Authenticate user without JWT")
  .get(api.auth.check)
  .expectStatus(401)
  .after(function (error, response, body) {
  })
.toss();

frisby.create("Login user with wrong email")
  .post(api.auth.login, {"email": "asd", "password": "asd"}, {json: true})
  .expectStatus(404)
  .after(function (error, response, body) {
  })
.toss();

frisby.create("Login user with wrong password")
  .post(api.auth.login, {"email": user1.credentials.email, "password": "asd"}, {json: true})
  .expectStatus(403)
  .after(function (error, response, body) {
  })
.toss();

frisby.create("Login user with correct credentials")
  .post(api.auth.login, user1.credentials, {json: true})
  .expectStatus(200)
  .expectJSONTypes({
    token: String,
  })
  .after(function (error, response, body) {
    updateJwt(body.token);

    frisby.create("Authenticate user with JWT")
      .get(api.auth.check)
      .expectStatus(200)
      .after(function (error, response, body) {
      })
    .toss();

    frisby.create("Get root directory")
      .get(api.directory.root)
      .expectStatus(200)
      .expectJSONTypes({
        title: String,
        id: Number,
        pobjects: Array,
        permissions: Array,
        user: Object
       })
      .expectJSON({
        title: 'root',
        pobjects: [],
        permissions: [],
        user: {
          email: user1.credentials.email
        }
      })
      .after(function (error, response, body) {

        var fileNoDirectory = {
          "title": "fileNoDirectory.bpmn",
        };
        frisby.create("Create new model with no directory")
          .post(api.file.new, fileNoDirectory, {json: true})
          .expectStatus(404)
          .after(function (error, response, body) {
          })
        .toss();

        var fileForbiddenDirectory = {
          "title": "fileForbiddenDirectory.bpmn",
          "directory": {
            id: 5
          }
        };
        frisby.create("Create new model with forbidden directory")
          .post(api.file.new, fileForbiddenDirectory, {json: true})
          .expectStatus(403)
          .after(function (error, response, body) {
          })
        .toss();

        var fileIncorrectName = {
          "title": "fileInc¤rrectName.bpmn",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Create new model with incorrect name")
          .post(api.file.new, fileIncorrectName, {json: true})
          .expectStatus(400)
          .after(function (error, response, body) {
          })
        .toss();

        var fileIncorrectExtension = {
          "title": "correctFile.bpm",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Create new model with incorrect extension")
          .post(api.file.new, fileIncorrectExtension, {json: true})
          .expectStatus(400)
          .after(function (error, response, body) {
          })
        .toss();

        var fileCorrect = {
          "title": "correctFile.bpmn",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Check if user1 can create a new model to root directory")
          .post(api.file.new, fileCorrect, {json: true})
          .expectStatus(200)
          .expectJSONTypes({
            title: String,
            id: Number,
            permissions: Array,
            user: Object,
            published: Boolean,
            lastModified: Number,
            modifiedBy: Object,
            md5Hash: String,
            directory: Object
           })
          .expectJSON({
            title: fileCorrect.title,
            permissions: [],
            user: {
              email: user1.credentials.email
            },
            directory: {
              id: body.id
            },
            content: null,
          })
          .after(function (error, response, body) {

            var renamedFile = body;
            renamedFile.title = "renamedFile.bpmn";
            frisby.create("Check if user1 can rename the created model")
              .put(api.file.new + body.id, renamedFile, {json: true})
              .expectStatus(200)
              .after(function (error, response, body) {
                var editedFile = JSON.parse(JSON.stringify(renamedFile)); // Copy
                editedFile.content = "some content";
                frisby.create("Check if user1 can edit the created model")
                  .put(api.file.new + body.id, editedFile, {json: true})
                  .expectStatus(200)
                  .after(function (error, response, body) {
                  })
                .toss();
              })
            .toss();



            var copyFile = renamedFile;
            frisby.create("Check if user1 can copy the created model")
              .post(api.file.new, copyFile, {json: true})
              .expectStatus(200)
              .after(function (error, response, body) {
                frisby.create("Check if user1 can delete the copied model")
                  .delete(api.file.new + body.id)
                  .expectStatus(200)
                  .after(function (error, response, body) {
                  })
                .toss();
              })
            .toss();

            frisby.create("Check if user1 can delete the created model")
              .delete(api.file.new + body.id)
              .expectStatus(200)
              .after(function (error, response, body) {
              })
            .toss();

          })
        .toss();

      })
    .toss();

  })
.toss();
