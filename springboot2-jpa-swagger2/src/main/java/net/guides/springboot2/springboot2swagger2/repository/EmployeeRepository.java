package net.guides.springboot2.springboot2swagger2.repository;

import net.guides.springboot2.springboot2swagger2.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByUsernameOrEmailId(String userName, String email);

}
