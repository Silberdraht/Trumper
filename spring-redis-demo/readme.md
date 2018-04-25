# Spring Redis Demo

Hinweise zum Betrieb der Demo

## Prozesse Starten

Starten des Redis-Servers: 
`# redis-server`

Starten der Anwendung in Eclipse: 
`run as --> Spring Boot App`

Alternativ: Starten der Anwendung über Terminal
`mvn spring-boot:run`

## Aufrufen der Seiten 
(siehe auch Request Mapping in UserController.java)

- [http://localhost:8080/users](http://localhost:8080/users)
	- listet alle user auf
- [http://localhost:8080/adduser](http://localhost:8080/adduser) 	
	- zeigt Formular zur Eingabe der Daten für einen neuen User
- [http://localhost:8080/user/{username}](http://localhost:8080/user/{username})	
	- sucht user mit dem namen "username"
- [http://localhost:8080/searchuser/{pattern}](http://localhost:8080/searchuser/{pattern})
	- sucht user, die die Zeichenfolge pattern enthalten


## Key Patterns (Schlüsselmuster) 

`KEY_{keytype}_{keyname}`

```
KEY_SET_ALL_USERNAMES = "all:usernames" // set for all usernames

KEY_ZSET_ALL_USERNAMES = "all:usernames:sorted"	 // sorted set for usernames

KEY_HASH_ALL_USERS = "all:user" // hash for user as java objects

KEY_PREFIX_USER = "user:" // prefix for user
```

	


