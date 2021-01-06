#### groovy-orm

groovy-orm - lightweight groovy ORM library. 
groovy-orm provides access to database tables via simple POJO classes.

##### Groovy SQL

For access to db groovy orm uses groovy.sql.Sql object. 

##### Model

Model - simple POJO class extended from Model class. Legal field's classes:

- String (text)
- Integer (integer)
- Long (bigint)
- Double, Float (double precision)
- Date (timestamptz)

Example:

    class Worker extends Model {
        String firstName
        String lastName
        Integer age
        Date birthday
    
        Worker(Sql sql, String firstName, String lastName, Integer age, Date birthday) {
            super(sql)
            this.firstName = firstName
            this.lastName = lastName
            this.age = age
            this.birthday = birthday
        }
    
        @Override
        Config config() {
            return null
        }
    }

##### Table management

groovy-orm provides CREATE TABLE  and DROP TABLE functionality:

    Table.create(sql, Worker)   // creates table in db
    Table.drop(sql, Worker)     // drops table in db
    
Default generated table name is model's class name in snake case (Worker -> worker, WorkerAddress -> worker_address). You can override table name and column by tableName property in Config and columnName property in ColumnConfig.

    new Config(
            tableName: 'custom_worker',
            columns: [
                    biography: new ColumnConfig(columnName: 'bio')
            ]
    )
    
`Model.insert()` is used to insert row to db.

    Worker worker = new Worker('John', 'Doe', 30, new Date()).insert()

`Model.update()` is used to update row in db.

    worker.age = 31
    worker.update()

##### Retrieving data from db

You can retrieve data via Select class.

###### Order by

Method `orderBy()` is using to order result.
Parameter fieldName should be in format:
- `name` or `name__asc` - to order in asc mode
- `name__desc` - to order in desc mode

Example:

    Select.orderBy(sql, Worker, 'firstName__desc')

###### Where 

Method `where()` is using to filter result.
Parameter fieldName should be in format:

- `name` - `=` operator
- `name__g` - `>` operator (greater)
- `name__ge` - `>=` operator (greater or equals)
- `name__l` - `<` operator (less)
- `name__le` - `<=` operator (less or equals)
- `name__is_null` - 'is null', 'is not null' operators depends on value (true, false). value - Boolean
- `name__in` - `in ()` operator. value - List 

If you need to execute more complex queries (for example, queries with OR statements), you can use Q objects.

Q objects can be combined using the `&` (and), `|` (or), `~`(not) operators. Q receives [key: fieldName, value: value] map as init paramater and combined by `and` operator.

Example:

    Select.where(sql, Worker, new Q(firstName: 'John') | ~ new Q(bio__is_null: true, age__le: '30'))
    // first_name = 'John' or not (bio is null and age <= 30)
    

###### Select

Method `select()` is using to execute built select statement.

Selects all Workers with null biography and sort it by first name:

    List<Worker> worker = Select.orderBy(sql, Worker, 'first_name').where('bio__is_null', true).select()

Select all Workers

    List<Worker> worker = Select.select(sql, Worker)
    
You can use `filter()` and `get()` methods (`where()` + `select()`).

`filter()` - returns List<Model>

`get()` - returns Model, throws RecordNotFoundException if there are no suitable records, or MultipleRecordsFoundException if there are more than 1 suitable records.

    List<Worker> worker = Select.orderBy(sql, Worker, 'first_name').filter('bio__is_null', true)

groovy-orm provides access to linked models. You can describe link using ForeignKey in Config.
`getFieldNameModel()`or `getterName` is used to access to linked model. 
Example:

    class Address extends Model {
        String country
        String city
        String street
        Integer houseNumber
    
        Address(Sql sql, String country, String city, String street, Integer houseNumber) {
            super(sql)
            this.country = country
            this.city = city
            this.street = street
            this.houseNumber = houseNumber
        }
    
        @Override
        Config config() {
            return new Config()
        }
    }
    
    class Worker extends Model {
        String firstName
        String lastName
        Integer age
        Date birthday
        String address
        Long managerId
    
        Worker(Sql sql, String firstName, String lastName, Integer age, Date birthday, String address, Long managerId) {
            super(sql)
            this.firstName = firstName
            this.lastName = lastName
            this.age = age
            this.birthday = birthday
            this.address = address
            this.managerId = managerId
        }
    
        @Override
        Config config() {
            return new Config(
                    tableName: 'test_fk',
                    columns: [
                            lastName: new ColumnConfig(columnName: 'family_name'),
                            address: new ColumnConfig(columnName: 'address_id', foreignKey: new ForeignKey(dest: Address, destColumnName: 'country')),
                            managerId: new ColumnConfig(foreignKey: new ForeignKey(dest: Worker, getterName: 'getManager'))
                    ]
            )
        }
    }
    
    Address address = new Address(sql, 'Germany', 'Berlin', 'Schroderstrasse', 11).insert()
    Worker manager = new Worker(sql, 'Rick', 'Jackson', 50, null, 'Germany', null).insert()
    Worker worker = new Worker(sql, 'John', 'Doe', 30, null, 'Germany', manager).insert()
    assert worker.getAddress() == 'Germany'
    assert worker.getAddressModel().id == address.id
    assert worker.getManagerId() == manager.id
    assert worker.getManager().firstName == 'Rick'

##### Config

tableName: custom table name 

columns: mapping of field's name to column's config

ColumnConfig

columnName: custom column name

lo: save value like large object

foreignKey: foreign key properties

ForeignKey

dest - destination table Model class 

destColumnName - column name of destination table (default: id)

getterName - override getter name
