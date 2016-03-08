# pleak-backend
The services powering Privacy Leak Tools

## JSON messages
Requests get JSON messages as results with the following base structure:
```
{
  type: "",
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
  type: "",
  data: {
    code: 0
  }
}
```
Used in:
* /pleak/save (success)

### Text
```
{
  type: "",
  data: {
    code: 0,
    text: ""
  }
}
```
Used in:
* /pleak/open (success)
* /pleak/open (error)
* /pleak/save (error)
* /pleak/list (error)

### TextDescription
```
{
  type: "",
  data: {
    code: 0,
    text: "",
    description: ""
  }
}
```
Currently not used.

### List
```
{
  type: "",
  data: {
    code: 0,
    list: []
  }
}
```
Used in:
* /pleak/list

## License

MIT
