
## Starting a DbQuery

A DbQuery is started by providing the entity class that represents the table in the from clause and by providing the hibernate session.  There are a few ways to do this.  For the rest of this tutorial I will be using the last option.

Use the constructor directly:

``` c#
DbQuery<Person> dbQuery = new DbQuery<>(Person.class, entityManager.unwrap(Session.class));
```

Use the the static from() method:

``` c#
DbQuery<Person> dbQuery = DbQuery.from(Person.class, entityManager.unwrap(Session.class));
```

Create a private method and use that:

``` c#
private <T> DbQuery<T> from(Class<T> type) {
  return DbQuery.from(type, entityManager.unwrap(Session.class));
}
DbQuery<Person> dbQuery = from(Person.class);
```

## Executing a DbQuery

A DbQuery is not executed and does not return results until you make an output call.  Below are some of the most common and simple output options.  More complicated options will be discussed later.

You can fetch the entire result set as a List or an array:

``` c#
List<Person> persons = from(Person.class).toList();
Person[] persons = from(Person.class).toArray();
```

You can also easily get a List or an array typed as a different superclass:

``` c#
List<PersonInfo> personInfo = from(Person.class).toList(PersonInfo.class);
PersonInfo[] persons = from(Person.class).toArray(PersonInfo.class);
```

You can also fetch an individual result with various built in error handling:

``` c#
// no error
Person person = from(Person.class).firstOrNull();
// error if no result
Person person = from(Person.class).first();
// error if multiple results
Person person = from(Person.class).singleOrNull();
// error if no result or multiple results
Person person = from(Person.class).single();
```

## Filtering a DbQuery

So far we've been fetching the all the rows of the database, but normally you'll want to filter out rows with one or more where clauses.  

A where clause is created first by declaring which column will be checked, then by declaring the filtering operation.  The example below checks for equality, but all the standard SQL operations have their own method.

``` c#
List<Person> persons = from(Person.class)
.where(Person.FirstName).isEqualTo("Andrew")
.toList();
// select * from person where first_name = 'Andrew';
```

Multiple where clauses can be added to query, and by default they will be added as a conjunction (AND):

``` c#
List<Person> persons = from(Person.class)
.where(Person.FirstName).isEqualTo("Andrew")
.where(Person.LastName).isEqualTo("Simpkins")
.toList();
// select * from person where first_name = 'Andrew' and last_name = 'Simpkins';
```

If a set of where clauses should be treated as a disjunction (OR) then a disjuction clause can be created and then terminated with the close() method:

``` c#
List<Person> persons = from(Person.class)
.openDisjuction()
	.where(Person.FirstName).isEqualTo("Andrew")
	.where(Person.LastName).isEqualTo("Simpkins")
.close()
.toList();
// select * from person where first_name = 'Andrew' or last_name = 'Simpkins';
```

Disjunctions and conjunctions can be nested inside each other as needed:

``` c#
List<Person> persons = from(Person.class)
.openDisjuction()
	.where(Person.FirstName).isEqualTo("Andrew")
	.openConjuction()
		.where(Person.LastName).isEqualTo("Simpkins")
		.where(Person.FirstName).isEqualTo("Andy")
	.close()
.close()
.toList();
// select * from person where first_name = 'Andrew' or (last_name = 'Simpkins' and first_name = 'Andy');
```

## Ordering a DbQuery

The results of a DbQuery can be ordered by making one or more ordering calls.  The two most common are:

``` c#
List<Person> persons = from(Person.class)
.orderBy(Person.LastName)
.orderByDescending(Person.FirstName)
.toList();
// select * from person order by last_name asc, first_name desc;
```

There are also a few more advanced options.  The way null values are handled can be reversed:

``` c#
List<Person> persons = from(Person.class).orderByNullsFirst(Person.FirstName).toList();
List<Person> persons = from(Person.class).orderByDescendingNullsLast(Person.FirstName).toList();
```
  
Also, ordering can apply the lower() function to each the column:

``` c#
List<Person> persons = from(Person.class).orderByIgnoreCase(Person.FirstName).toList();
List<Person> persons = from(Person.class).orderByDescendingIgnoreCase(Person.FirstName).toList();
```

Or both:

``` c#
List<Person> persons = from(Person.class).orderByNullsFirstIgnoreCase(Person.FirstName).toList();
List<Person> persons = from(Person.class).orderByDescendingNullsLastIgnoreCase(Person.FirstName).toList();
```

## Limiting a DbQuery

The results from a DbQuery can be limited or offset to pull just portion of the result from the database:

``` c#
// select * from person limit 50;
List<Person> persons = from(Person.class).limit(null, 50).toList();
// select * from person offset 50;
List<Person> persons = from(Person.class).limit(50, null).toList();
// select * from person offset 50 limit 50;
List<Person> persons = from(Person.class).limit(50, 50).toList();
```

## Eagerly loading relationships

Related table rows can be marked to be pulled along with the rows in the result in order to fetch all data in a single query.

``` c#
List<Person> persons = from(Person.class)
.loadWith(Person.Employer)
.toList();
// select * from person left join employer on employer.id = person.employer_id;
```

Alternatively you can specify that related table rows be fetched but in a separate query for efficiency purposes.

``` c#
List<Person> persons = from(Person.class)
.loadAfter(Person.Pets)
.toList();
// select * from person;
// select * from pets where pet.person_id in (...);
```

To minimize code you can also indicate that all joins specified in any where, order by, or sub query clauses be fetched eagerly:
``` c#
List<Person> persons = from(Person.class)
.loadWithAllJoins()
.where(Person.Employer.join(Employer.Id)).isEqualTo(123)
.toList();
// select * from person left join employer on employer.id = person.employer_id;
```

## Dynamically applying criteria

Sometimes you want criteria to only be applied to the DbQuery in certain cases.  In the following example, the where clause will only be applied if the excludeChildren variable is true:

``` c#
List<Person> persons = from(Person.class)
.when(excludeChildren, dbQuery -> dbQuery.where(Person.Age).isGreaterThanOrEqualTo(21))
.toList();
```

## Selecting specific columns

Instead of selecting the entire entity you can select just one or more columns.

``` c#
List<String> firstNames = from(Person.class).select(Person.FirstName);
// select first_name from person;
```

When more than one column is specified then a DbResultRow will be returned instead.  This is essentially a map that allows you to fetch the database value by the same identifier you selected it with.

``` c#
List<DbResultRow> results = from(Person.class).select(Person.FirstName, Person.LastName);
// select first_name, last_name from person;
for (DbResultRow row in results) {
  String firstName = row.get(Person.FirstName);
  String lastName = row.get(Person.LastName);
}
```

Either of the above calls can also be converted into a select distinct as well:

``` c#
List<String> firstNames = from(Person.class).selectDistinct(Person.FirstName);
// select distinct first_name from person;
List<DbResultRow> results = from(Person.class).select(Person.FirstName, Person.LastName);
// select distinct first_name, last_name from person;
```

Finally, any of the above calls can fetch individual results with error handling by appending firstOrNull, first, singleOrNull, or single to the method call as detailed in the earlier Executing a DbQuery section.

## Selecting aggregates

There are methods to select individual aggregate results for a DbQuery:

```c#
long count = from(Person.class).count();
long distinctCount = from(Person.class).countDistinct(Person.LastName);
Integer sum = from(Person.class).sum(Person.Age);
Integer minimum = from(Person.class).min(Person.Age);
Integer maximum = from(Person.class).max(Person.Age);
Double average = from(Person.class).avg(Person.Age);
Boolean any = from(Person.class).any(Person.IsDeceased);
Boolean all = from(Person.class).all(Person.IsDeceased);
Boolean none = from(Person.class).none(Person.IsDeceased);
```

You can also select more than one aggregate which will also use the DbResultRow object.  In this example the result List will contain no more than one row.

``` c#
List<DbResultRow> results = from(Person.class).aggregate(Person.Age.min(), Person.Age.max());
// select min(age), max(age) from person;
for (DbResultRow row : results) {
  Integer minimum = results.get(Person.Age.min());
  Integer maximum = results.get(Person.Age.max());
}
```

You can also specify a grouping aggregation in which case the result List may contain more than one row, grouped by the provided grouping aggregations:

``` c#
List<DbResultRow> results = from(Person.class).aggregate(Person.FirstName.groupBy(), Person.Gender.groupBy(), Person.Age.avg());
// select first_name, gender, avg(age) from person group by first_name, gender
for (DbResultRow row : results) {
  String firstName = results.get(Person.FirstName.groupBy());
  String gender = results.get(Person.Gender.groupBy());
  Double averageAge = results.get(Person.Age.avg());
}
```
