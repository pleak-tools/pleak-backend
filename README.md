# pleak-backend
The services powering Privacy Leak Tools

## JSON messages
Requests get JSON messages as results with the following base structure:
```
{
  type: "",
  code: 0
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
  code: 0
}
```
Used in:
* /pleak/save (success)

### Text
```
{
  type: "",
  code: 0,
  text: ""
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
  code: 0,
  text: "",
  description: ""
}
```
Currently not used.

### List
```
{
  type: "",
  code: 0,
  list: []
}
```
Used in:
* /pleak/list (success)

## License

MIT
