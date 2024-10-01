Документация
	Настраиваем файл pop.xml
Подефолту расположение файла сразу видно, но на всякий вот:
Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----        01.10.2024     21:25                .idea
d-----        01.10.2024     20:28                src
d-----        01.10.2024     21:15                target
-a----        01.10.2024     20:28            490 .gitignore
-a----        01.10.2024     21:10           1507 pom.xml
Содержание:
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>untitled36</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Hibernate -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.6.15.Final</version>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.200</version>
        </dependency>

        <!-- JPA API -->
        <dependency>
            <groupId>javax.persistence</groupId>
            <artifactId>javax.persistence-api</artifactId>
            <version>2.2</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.30</version>
        </dependency>
    </dependencies>

</project>
Конфигурация Hibernate
	Надо создать в папке resources файл hibernate.cfg.xml

<hibernate-configuration>
    <session-factory>
        <!-- H2 Database -->
        <property name="hibernate.connection.driver_class">org.h2.Driver</property>
        <property name="hibernate.connection.url">jdbc:h2:mem:testdb</property>
        <property name="hibernate.connection.username">sa</property>
        <property name="hibernate.connection.password"></property>

        <!-- Hibernate Settings -->
        <property name="hibernate.dialect">org.hibernate.dialect.H2Dialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="hibernate.show_sql">true</property>

        <!-- Регистрация класса Employee -->
        <mapping class="org.example.Employee"/>
    </session-factory>
</hibernate-configuration>

В конфигурации можно долго объяснить за что отвечает разные строчки, распишу про самую основную: 
<property name="hibernate.connection.url">jdbc:h2:mem:testdb</property>
mem – база данных хранится в оперативной памяти, поэтому вся информация будет храниться в бд пока запущен код, после перезапуская бд очистится.
<property name="hibernate.connection.url">jdbc:h2:./data/testdb</property>
jdbc:h2:./data/testdb – сохранит все что ранее записывали в бд


Класс Employee
import javax.persistence.*; - подтягивает аннотации для работы Hibernate
@Entity
@Table(name = "employees")
public class Employee {

@Entity – эта аннотация сообщает Hibernate, что данный класс является сущностью и должен быть связан с таблицей в базе данных.
@Table(name = "employees") - указывает, что класс Employee связан с таблицей базы данных, которая называется employees

@Id
private Long id;
@Id - указывает, что это поле является первичным ключом таблицы.

Дальше аналогично также со всеми данными о сотриднике

В конце прописывает геттеры и сеттеры, их можно самим не писать, что alt+f12 и выбираешь все getter setter и все!

HibernateUtil
import org.hibernate.SessionFactory; - импортирует класс SessionFactory, который используется для создания сессий в Hibernate. Сессии необходимы для взаимодействия с базой данных.
import org.hibernate.cfg.Configuration; - импортирует класс Configuration, который управляет конфигурацией Hibernate, в том числе чтением настроек из конфигурационного файла (например, hibernate.cfg.xml).

В общем, самое для меня самое трудное в понимании, просто все что есть в этом файлике – значит так должно быть. Без него ничего не сработает.

EmployeeManager
import org.hibernate.Session;
import org.hibernate.Transaction;

Session и Transaction из Hibernate используются для работы с сессиями и транзакциями.
 
Остальное нет смысла объяснять, так как очень много и можно разными схемами оформить данный класс
