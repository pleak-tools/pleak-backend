# pleak-backend
The services powering Privacy Leak Tools

## JSON messages
Requests get JSON messages as results with the following base structure:
```
{
  type: ""
  data: {
    code: 0
  }
}
```
There are currently these types:
* success
* error

Codes are regular HTTP status codes.

Different types of results have the following structure:

### Default
```
{
  type: ""
  data: {
    code: 0
  }
}
```

### Text
```
{
  type: ""
  data: {
    code: 0,
    text: ""
  }
}
```

### TextDescription
```
{
  type: ""
  data: {
    code: 0,
    text: "",
    description: ""
  }
}
```

### List
```
{
  type: ""
  data: {
    code: 0,
    list: []
  }
}
```

## License

MIT
