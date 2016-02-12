# The Purpose #

This task occurs from time to time in my projects. I need to handle a collection of some complex elements, having different attributes, such as login, password\_hash, role, etc. And, I need to be able to query that collection, just like I query a table in the database, having only partial data. For example: get all persons, with role "user". Or check, if there's a user with login "root" and role "superuser". Removing items, based on same data is also needed.

Tablej was developed to satisfy this need. It is based on top of google collections, but does not use iterators to find data when you query it. Instead, it has hashed indexes, so that search is performed as fast as `HashMap.get(key);`

**This is not the replacement for database in any case.**

# Example #

```
Table<Person> persons = new Table<Person>();

persons.index("by_name_and_surname", new Indexer<Person>() {

@Override
protected Object[] getValues(Person t){
  return new Object[] {t.getName(), t.getSurname()};
}

});

persons.index("by_name", new Indexer<Person>() {

@Override
protected Object[] getValues(Person t){
  return new Object[] {t.getName()};
}

});


table.add(new Person("John", "Doe"));
table.add(new Person("John", "Doe"));
table.add(new Person("Foo", "Bar"));
table.add(new Person("John", "Baz"));


...

//will return 2 elements
Collection<Person> johnDoes = table.find("by_name_and_surname", "John", "Doe");

//will return 3 elements: 2 John Doe's and one John Baz
Collection<Person> johns = table.find("by_name", "John");

```

# Maven #

use this as maven repo:

```
<repository>
  <id>tablej-snapshots</id>
  <url>http://tablej.googlecode.com/svn/trunk/tablej/maven-repo/raw/master/snapshots</url>
</repository>
```

# Similar projects #

If you're not satisfied with tablej, you can try this:
