simple-datastore
================

**simple-datastore** simplifies complex business data models to be stored on [Google Datastore]

h2. Why **simple-datastore**

We develop and operate an important quantity of massive games, having +16M users. 
Every game has a complex and always-changing data model, such as any complex application has.

Major problems that **simple-datastore** addresses are:

| **Challenge** | **How** |
|---------------|---------|
| **Feature changing is usual**. They impact in the data model and they have to be deployed very quickly | Adding a new attribute requires adding it to a Java object: no mapping is required, no "ALTER TABLE" scripts, no migration of old rows |
| **Downtime should be avoided** as much as possible | XX |
| * Reducing Google Datastore paradigm **learning curve** | XX |
| Achieve very **high performance** | XX |

h2. How

The following simplified class diagram shows a little part of our always changing game model for **one game**:

[class diagram]

The idea behind **simple-datastore** is to persist those object instances in just **one Datastore Entity** but allowing the program to deal with the same class diagram shown above.

h2. Main Features

* Complex business object data model conversion to single [Google Datastore Entity]
* Automatic migration of models versions 
* Handy data access methods for common [DatastoreService] features (Queries, CRUD on both synchronous and asynchronous model)
* [Apache Avro] based fast Java objects serialization without Java Reflection usage
* Easy, declarative way to model
* Very low overhead, extreme reduction of queries when materializing a persisted model 
* Support for massive, parallel data access outside of Google App Engine
* Useful [MemCache] strategies on data access
* Retrying algorithms for all Google App Engine calls
* Data access logging for performance tuning
* Common business object behaviour:
** equals/hashcode
** lastUpdateTimestamp
** several helper for getting information about update events on the Entity
** toString showing entity data
* Audit hooks for data change
* Easy and transparent way to store and retrieve complex app parameters
* It works! **simple-datastore** concepts are a part of our architecture core for running games with tons of users 


h2. Getting started
* download x maven
* guia para empezar modelando segun los ejemplos de tests


[Google Datastore]:https://developers.google.com/appengine/docs/java/datastore/
[Google Datastore Entity]:https://developers.google.com/appengine/docs/java/datastore/entities
[DatastoreService]:https://developers.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreService
[MemCache]:https://developers.google.com/appengine/docs/java/memcache/
[Apache Avro]:http://avro.apache.org/
[class diagram]:https://raw.githubusercontent.com/ZupCat/simple-datastore/master/doc/model.png

