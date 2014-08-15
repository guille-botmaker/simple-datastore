<img width="15%" src="https://raw.githubusercontent.com/ZupCat/simple-datastore/master/doc/zupcat.png" alt="ZupCat Logo" title="ZupCat" align="center" />

simple-datastore
================

**simple-datastore** simplifies complex business data models to be stored on [Google Datastore]

***

## Why **simple-datastore**

We develop and operate an important quantity of massive games, having +16M users. 
Every game has a complex and always-changing data model, such as any complex application has.

Major problems that **simple-datastore** addresses are:



| **Challenge**                                 | **How**                                       |
|-----------------------------------------------|-----------------------------------------------|
| **Feature changing is usual**. They impact in the data model and they have to be deployed very quickly | Adding a new attribute requires adding it to a Java object: no mapping is required, no "ALTER TABLE" scripts, no migration of old rows |
| **Downtime should be avoided** as much as possible | No need to stop a running app. Deploying a new version automatically deploys a new model. The migration occurs when the entity is loader, with no overhead |
| Reducing Google Datastore paradigm **learning curve** | Because of even higher abstraction level when modeling |
| Achieving very **high performance** | No reflection usage, very little extra steps on entity convertion to model |


## Main Features

* **Complex business object data model conversion** to single [Google Datastore Entity]
* **Automatic migration** of models versions 
* Handy **data access methods** for common [DatastoreService] features (Queries, CRUD on both synchronous and asynchronous model)
* [Apache Avro] based fast Java objects serialization without Java Reflection usage
* **Easy, declarative** way to model
* Very **low overhead**, extreme reduction of queries when materializing a persisted model 
* Support for **massive, parallel data access outside of Google App Engine**
* Useful **[MemCache] strategies** on data access
* **Retrying algorithms** for all Google App Engine calls
* Data access logging for **performance tuning**
* **Common business object behaviour**: equals/hashcode, lastUpdateTimestamp, several helper for getting information about update events on the Entity and toString showing entity data
* **Audit hooks** for data change
* **Easy and transparent** way to store and retrieve complex app parameters
* It works! **simple-datastore** concepts are a part of our architecture core for running games with tons of users 

***

## Getting started
1. Check **[some examples](https://github.com/ZupCat/simple-datastore/wiki/simple-datastore-Common-Uses-and-Features)**
2. Follow the **[Starting Guide](https://github.com/ZupCat/simple-datastore/wiki/Starting-Guide)** to include simple-datastore in your project

***

## How

The following simplified class diagram shows a little part of our always changing game model for **one game**:


<img src="https://raw.githubusercontent.com/ZupCat/simple-datastore/master/doc/model.png" alt="Class diagram" title="Class diagram" align="center" />


The idea behind **simple-datastore** is to persist those object instances in just **one Datastore Entity** but allowing the program to deal with the same class diagram shown above.


## Why simple-datastore is different to [objectify-appengine](https://code.google.com/p/objectify-appengine/)
Objectify is a very nice and mature project. I believe simple-datastore is different because of **simplicity**:
* The main idea behind simple-datastore is to keep most of the model in just one Entity
* simple-datastore doesn't have entities relations and doesn't expose Key and other JPA/JDO features
* You could say simple-datastore is less powerful but simpler.
* Even though the simplicity, this is still being the core of our complex model for massive games

## Contributing

We would love to get your contributions! If you spot a bug, then please [raise an issue] in our main GitHub project; likewise if you have developed a cool new feature or improvement in your simple-datastore fork, then send us a pull request!

## Copyright and license

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0


[Google Datastore]:https://developers.google.com/appengine/docs/java/datastore/
[Google Datastore Entity]:https://developers.google.com/appengine/docs/java/datastore/entities
[DatastoreService]:https://developers.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreService
[MemCache]:https://developers.google.com/appengine/docs/java/memcache/
[Distribution Directory]:https://github.com/ZupCat/simple-datastore/tree/master/dist
[raise an issue]:https://github.com/ZupCat/simple-datastore/issues
[modeling samples]:https://github.com/ZupCat/simple-datastore/wiki/simple-datastore-Samples
[Apache Avro]:http://avro.apache.org/

