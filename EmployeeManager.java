package org.example;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Scanner;

public class EmployeeManager {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nМеню:");
            System.out.println("1. Добавить сотрудника");
            System.out.println("2. Искать сотрудника по ID");
            System.out.println("3. Искать сотрудника по имени");
            System.out.println("4. Удалить сотрудника");
            System.out.println("5. Показать общую сумму зарплат");
            System.out.println("6. Выйти");
            System.out.print("Выберите опцию: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    addEmployee(scanner);
                    break;
                case 2:
                    searchEmployeeById(scanner);
                    break;
                case 3:
                    searchEmployeeByName(scanner);
                    break;
                case 4:
                    deleteEmployee(scanner);
                    break;
                case 5:
                    calculateTotalSalary();
                    break;
                case 6:
                    HibernateUtil.shutdown();
                    System.exit(0);
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }

    private static void addEmployee(Scanner scanner) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Employee employee = new Employee();

            System.out.print("Введите ID: ");
            Long id = scanner.nextLong();

            // Проверка уникальности ID
            Employee existingEmployee = session.get(Employee.class, id);
            if (existingEmployee != null) {
                System.out.println("Сотрудник с таким ID уже существует. Попробуйте другой ID.");
                return;
            }

            employee.setId(id);

            System.out.print("Введите имя: ");
            employee.setFirstName(scanner.next());
            System.out.print("Введите фамилию: ");
            employee.setLastName(scanner.next());
            System.out.print("Введите дату рождения (ГГГГ-ММ-ДД): ");
            String dobInput = scanner.next();
            java.sql.Date dob = java.sql.Date.valueOf(dobInput);
            employee.setDateOfBirth(dob);
            System.out.print("Введите место рождения: ");
            employee.setPlaceOfBirth(scanner.next());
            System.out.print("Введите зарплату: ");
            employee.setSalary(scanner.nextDouble());

            session.save(employee);
            transaction.commit();
            System.out.println("Сотрудник добавлен.");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private static void searchEmployeeById(Scanner scanner) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        System.out.print("Введите ID сотрудника: ");
        Long id = scanner.nextLong();
        Employee employee = session.get(Employee.class, id);
        session.close();

        if (employee != null) {
            System.out.println("Сотрудник найден: " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Сотрудник с таким ID не найден.");
        }
    }

    private static void searchEmployeeByName(Scanner scanner) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        System.out.print("Введите имя сотрудника: ");
        String name = scanner.next();
        List<Employee> employees = session.createQuery("from Employee where firstName = :name", Employee.class)
                .setParameter("name", name)
                .getResultList();
        session.close();

        if (!employees.isEmpty()) {
            for (Employee emp : employees) {
                System.out.println("Найден сотрудник: " + emp.getFirstName() + " " + emp.getLastName());
            }
        } else {
            System.out.println("Сотрудник с таким именем не найден.");
        }
    }

    private static void deleteEmployee(Scanner scanner) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        System.out.print("Введите ID сотрудника для удаления: ");
        Long id = scanner.nextLong();
        Employee employee = session.get(Employee.class, id);

        if (employee != null) {
            session.delete(employee);
            transaction.commit();
            System.out.println("Сотрудник удален.");
        } else {
            System.out.println("Сотрудник с таким ID не найден.");
        }

        session.close();
    }

    private static void calculateTotalSalary() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Double totalSalary = (Double) session.createQuery("select sum(salary) from Employee").getSingleResult();
        session.close();
        System.out.println("Общая сумма зарплат: " + totalSalary);
    }
}
