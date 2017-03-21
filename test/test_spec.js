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
    public: host + "/directories/files/public/",
    delete: host + "/directories/files/permissions/"
  },
  user: {
    password: host + "/user/password"
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

var blocked = {
  "credentials": {
    "email": "blocked@example.com",
    "password": "blocked"
  }
};

var changePassword = {
  "credentials": {
    "email": "changepassword1@example.com",
    "password": "changepassword1"
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
.toss();

frisby.create("Login user with wrong email")
  .post(api.auth.login, {"email": "asd", "password": "asd"}, {json: true})
  .expectStatus(404)
.toss();

frisby.create("Login user with wrong password")
  .post(api.auth.login, {"email": user1.credentials.email, "password": "asd"}, {json: true})
  .expectStatus(403)
.toss();

frisby.create("Login user with blocked account")
  .post(api.auth.login, blocked.credentials, {json: true})
  .expectStatus(401)
.toss();

frisby.create("Change user password without authentication")
  .put(api.user.password, {currentPassword: changePassword.credentials.password, newPassword: newPassword}, {json: true})
  .expectStatus(401)
.toss();

var user1Tests = frisby.create("Login user with correct credentials")
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
        frisby.create("Create a new model with no directory")
          .post(api.file.new, fileNoDirectory, {json: true})
          .expectStatus(404)
        .toss();

        var fileForbiddenDirectory = {
          "title": "fileForbiddenDirectory.bpmn",
          "directory": {
            id: 5
          }
        };
        frisby.create("Create a new model with forbidden directory")
          .post(api.file.new, fileForbiddenDirectory, {json: true})
          .expectStatus(403)
        .toss();

        var fileIncorrectName = {
          "title": "fileInc¤rrectName.bpmn",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Create a new model with incorrect name")
          .post(api.file.new, fileIncorrectName, {json: true})
          .expectStatus(400)
        .toss();

        var fileIncorrectExtension = {
          "title": "correctFile.bpm",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Create a new model with incorrect extension")
          .post(api.file.new, fileIncorrectExtension, {json: true})
          .expectStatus(400)
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
            .toss();

            var editedFile = body;
            editedFile.published = true;
            editedFile = JSON.parse(JSON.stringify(body)); // Copy
            editedFile.content = "some content";
            frisby.create("Check if user1 can edit the created model")
              .put(api.file.new + body.id, editedFile, {json: true})
              .expectStatus(200)
              .after(function (error, response, body) {

                var publicFile = body;
                frisby.create("Log out (from user1 account)")
                  .get(api.auth.logout)
                  .expectStatus(200)
                  .after(function (error, response, body) {

                    frisby.create("Login user2 with correct credentials to check if it can view and edit a model with public url")
                      .post(api.auth.login, user2.credentials, {json: true})
                      .expectStatus(200)
                      .expectJSONTypes({
                        token: String,
                      })
                      .after(function (error, response, body) {
                        updateJwt(body.token);

                        frisby.create("View public model")
                          .get(api.file.public + publicFile.uri)
                          .expectStatus(200)
                          .after(function (error, response, body) {

                            var editedFile = JSON.parse(JSON.stringify(publicFile)); // Copy
                            editedFile.content = "some new content";

                            frisby.create("Check if user2 can edit the file")
                              .put(api.file.new + publicFile.id, editedFile, {json: true})
                              .expectStatus(403)
                              .after(function (error, response, body) {

                                frisby.create("Log out (from user2 account)")
                                  .get(api.auth.logout)
                                  .expectStatus(200)
                                .toss();

                              })
                            .toss();

                          })
                        .toss();

                      })
                    .toss();

                  })
                .toss();

              })
            .toss();

            var sharedFileNonExistingUser = JSON.parse(JSON.stringify(body));
            sharedFileNonExistingUser.permissions = [{
              action: {
                title: "view"
              },
              user: {
                email: "nonExistingUser@example.com"
              }
            }];
            frisby.create("Check if user1 can share the created model with non-existing user")
              .put(api.file.new + body.id, sharedFileNonExistingUser, {json: true})
              .expectStatus(400)
            .toss();

            var sharedFileExistingUserView = JSON.parse(JSON.stringify(body));
            sharedFileExistingUserView.permissions = [{
              action: {
                title: "view"
              },
              user: {
                email: user2.credentials.email
              }
            }];
            frisby.create("Check if user1 can share the created model (permissions: view) with existing user (user2)")
              .put(api.file.new + body.id, sharedFileExistingUserView, {json: true})
              .expectStatus(200)
              .after(function (error, response, body) {

                var sharedFile = body;

                frisby.create("Log out")
                  .get(api.auth.logout)
                  .expectStatus(200)
                  .after(function (error, response, body) {

                    frisby.create("Login user2 with correct credentials to view the shared model")
                      .post(api.auth.login, user2.credentials, {json: true})
                      .expectStatus(200)
                      .expectJSONTypes({
                        token: String,
                      })
                      .after(function (error, response, body) {
                        updateJwt(body.token);

                        frisby.create("View shared model")
                          .get(api.file.new + sharedFile.id)
                          .expectStatus(200)
                          .after(function (error, response, body) {

                            var editedFile = JSON.parse(JSON.stringify(sharedFile)); // Copy
                            editedFile.content = "some new content";

                            frisby.create("Check if user2 can edit the shared model")
                              .put(api.file.new + sharedFile.id, editedFile, {json: true})
                              .expectStatus(403)
                              .after(function (error, response, body) {

                                frisby.create("Log out (from user2 account)")
                                  .get(api.auth.logout)
                                  .expectStatus(200)
                                  .after(function (error, response, body) {

                                    frisby.create("Login user1 with correct credentials to remove sharing of a model")
                                      .post(api.auth.login, user1.credentials, {json: true})
                                      .expectStatus(200)
                                      .expectJSONTypes({
                                        token: String,
                                      })
                                      .after(function (error, response, body) {
                                        updateJwt(body.token);

                                        frisby.create("Remove sharing of a model")
                                          .delete(api.file.delete + sharedFile.id)
                                          .expectStatus(200)
                                          .after(function (error, response, body) {

                                            frisby.create("Log out (from user1 account)")
                                              .get(api.auth.logout)
                                              .expectStatus(200)
                                            .toss();

                                          })
                                        .toss();

                                      })
                                    .toss();

                                  })
                                .toss();

                              })
                            .toss();

                          })
                        .toss();

                      })
                    .toss();

                  })
                .toss();

                frisby.create("Log out (from user1 account)")
                  .get(api.auth.logout)
                  .expectStatus(200)
                  .after(function (error, response, body) {

                    frisby.create("Login user3 with correct credentials to view and edit a not shared model")
                      .post(api.auth.login, user3.credentials, {json: true})
                      .expectStatus(200)
                      .expectJSONTypes({
                        token: String,
                      })
                      .after(function (error, response, body) {
                        updateJwt(body.token);

                        frisby.create("Check if user3 can view the file")
                          .get(api.file.new + sharedFile.id)
                          .expectStatus(403)
                          .after(function (error, response, body) {

                            var editedFile = JSON.parse(JSON.stringify(sharedFile)); // Copy
                            editedFile.content = "some new content";

                            frisby.create("Check if user3 can edit the file")
                              .put(api.file.new + sharedFile.id, editedFile, {json: true})
                              .expectStatus(403)
                              .after(function (error, response, body) {

                                frisby.create("Log out (from user3 account)")
                                  .get(api.auth.logout)
                                  .expectStatus(200)
                                .toss();

                              })
                            .toss();

                          })
                        .toss();

                      })
                    .toss();

                  })
                .toss();

              })
            .toss();

            var copyFile = renamedFile;
            frisby.create("Check if user1 can copy the created model")
              .post(api.file.new, copyFile, {json: true})
              .expectStatus(200)
              .after(function (error, response, body) {

                var sharedFileExistingUserEdit = JSON.parse(JSON.stringify(body));
                sharedFileExistingUserEdit.permissions = [{
                  action: {
                    title: "edit"
                  },
                  user: {
                    email: user2.credentials.email
                  }
                }];
                frisby.create("Check if user1 can share the created model (permissions: edit) with existing user (user2)")
                  .put(api.file.new + body.id, sharedFileExistingUserEdit, {json: true})
                  .expectStatus(200)
                  .after(function (error, response, body) {

                    var sharedFile = body;

                    frisby.create("Log out")
                      .get(api.auth.logout)
                      .expectStatus(200)
                      .after(function (error, response, body) {

                        frisby.create("Login user2 with correct credentials to edit the shared model")
                          .post(api.auth.login, user2.credentials, {json: true})
                          .expectStatus(200)
                          .expectJSONTypes({
                            token: String,
                          })
                          .after(function (error, response, body) {
                            updateJwt(body.token);

                            var editedFile = JSON.parse(JSON.stringify(sharedFile)); // Copy
                            editedFile.content = "some more new content";

                            frisby.create("Check if user2 can edit the model")
                              .put(api.file.new + sharedFile.id, editedFile, {json: true})
                              .expectStatus(200)
                              .after(function (error, response, body) {

                                frisby.create("Log out (from user2 account)")
                                  .get(api.auth.logout)
                                  .expectStatus(200)
                                  .after(function (error, response, body) {

                                    frisby.create("Login user1 with correct credentials to delete the copied model")
                                      .post(api.auth.login, user1.credentials, {json: true})
                                      .expectStatus(200)
                                      .expectJSONTypes({
                                        token: String,
                                      })
                                      .after(function (error, response, body) {
                                        updateJwt(body.token);

                                        frisby.create("Check if user1 can delete the model")
                                          .delete(api.file.new + sharedFile.id)
                                          .expectStatus(200)
                                          .after(function (error, response, body) {

                                            frisby.create("Log out (from user1 account)")
                                              .get(api.auth.logout)
                                              .expectStatus(200)
                                            .toss();

                                          })
                                        .toss();

                                      })
                                    .toss();

                                  })
                                .toss();

                              })
                            .toss();

                          })
                        .toss();

                      })
                    .toss();

                  })
                .toss();

              })
            .toss();

          })
        .toss();

        var directoryIncorrectName = {
          "title": "directoryInc¤rrectName",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Create a new directory with incorrect name")
          .post(api.directory.new, directoryIncorrectName, {json: true})
          .expectStatus(400)
          .after(function (error, response, body) {
          })
        .toss();

        var directoryCorrect = {
          "title": "correctDirectory",
          "directory": {
            "title": "root"
          }
        };
        frisby.create("Create a new directory with correct name")
          .post(api.directory.new, directoryCorrect, {json: true})
          .expectStatus(200)
          .after(function (error, response, body) {

          var renamedDirectory = body;
          renamedDirectory.title = "renamedDirectory";
          frisby.create("Check if user1 can rename the created directory")
            .put(api.directory.new + body.id, renamedDirectory, {json: true})
            .expectStatus(200)
          .toss();

          var copyDirectory = renamedDirectory;
          frisby.create("Check if user1 can copy the created directory")
            .post(api.directory.new, copyDirectory, {json: true})
            .expectStatus(200)
            .after(function (error, response, body) {

              frisby.create("Check if user1 can delete the copied directory")
                .delete(api.directory.new + body.id)
                .expectStatus(200)
              .toss();

            })
          .toss();

          })
        .toss();

      })
    .toss();

    frisby.create("Get shared directory")
      .get(api.directory.shared)
      .expectStatus(200)
      .expectJSONTypes({
        title: String,
        id: null,
        pobjects: Array,
        permissions: Array,
        user: Object
      })
      .expectJSON({
        title: 'shared',
        pobjects: [],
        permissions: [],
        user: {
          email: user1.credentials.email
        }
      })
    .toss();

  });

var newPassword = "changepassword2";
frisby.create("Login user with correct credentials to change password")
  .post(api.auth.login, changePassword.credentials, {json: true})
  .expectStatus(200)
  .expectJSONTypes({
    token: String,
  })
  .after(function (error, response, body) {
    updateJwt(body.token);

    frisby.create("Change user password")
      .put(api.user.password, {currentPassword: changePassword.credentials.password, newPassword: newPassword}, {json: true})
      .expectStatus(200)
      .after(function (error, response, body) {

        frisby.create("Log out")
          .get(api.auth.logout)
          .expectStatus(200)
          .after(function (error, response, body) {

          frisby.create("Login user with correct credentials to change password back to initial")
            .post(api.auth.login, {email: changePassword.credentials.email, password: newPassword}, {json: true})
            .expectStatus(200)
            .expectJSONTypes({
              token: String,
            })
            .after(function (error, response, body) {
              updateJwt(body.token);

              frisby.create("Change user password back to initial")
                .put(api.user.password, {currentPassword: newPassword, newPassword: changePassword.credentials.password}, {json: true})
                .expectStatus(200)
                .after(function (error, response, body) {

                  frisby.create("Log out")
                    .get(api.auth.logout)
                    .expectStatus(200)
                    .after(function (error, response, body) {

                      user1Tests
                      .toss();

                    })
                  .toss();

                })
              .toss();

            })
          .toss();

          })
        .toss();

      })
    .toss();

  })
.toss();
