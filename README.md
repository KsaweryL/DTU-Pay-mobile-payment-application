# DTU Pay Application

This is a redacted copy of the real project.
DTU Pay is an application that offers a mobile payment option for merchants and customers. Internally, the system is composed of multiple microservices that communicate asynchronously using message queues while the external connections are handled via REST APIs or SOAP interface.

## How to run it?

End to end tests can be run using command 

```bash
./build_and_run_code_cover.sh
```

However, becasue this is a redacted version of the original project, the tests will most likely fail due to several reasons:
- Original API key to our production sevrer
- Password and username to Jenkins on prodcution server is not included
- Production sevrer is down

API keys and pasword are not included due to confidentiality reasons, while the server is down since the project has already been completed.

Nevertheless, the project has all of the code for all of the services, as well as end to end tests.
