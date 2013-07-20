Custom Mashaller
================

This is a custom marshaller example sending data in chunks to the client.

1. Clone the project `git clone https://github.com/ziodave/scala-spray-examples.git`,
2. Move into the `scala-spray-examples/chunked-marshaller` folder,
3. Run the example `sbt run`.

Try the example:

```
$ curl http://localhost:8080/fruits -H "Accept: text/xml"
<fruits>
<fruit name="Bananas" kg="1.0"/>
<fruit name="Oranges" kg="2.0"/>
<fruit name="Lemons" kg="0.5"/>
</fruits>
```

```
$ curl http://localhost:8080/fruits -H "Accept: text/csv"
name	kg
Bananas	1.0
Oranges	2.0
Lemons	0.5
```