package net.guides.springboot2.springboot2swagger2.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.guides.springboot2.springboot2swagger2.exception.ResourceNotFoundException;
import net.guides.springboot2.springboot2swagger2.model.Employee;
import net.guides.springboot2.springboot2swagger2.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Api(value = "Employee Management System", description = "Operations pertaining to employee in Employee Management System")
public class EmployeeController {
    @Autowired
    private EmployeeRepository employeeRepository;

    @ApiOperation(value = "View a list of available employees", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @ApiOperation(value = "Get an employee by Id")
    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> getEmployeeById(
            @ApiParam(value = "Employee id from which employee object will retrieve", required = true)
            @PathVariable(value = "id") Long employeeId)
            throws ResourceNotFoundException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for this id :: " + employeeId));
        return ResponseEntity.ok().body(employee);
    }

    private String getAuthUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @ApiOperation(value = "Get employee info by userName")
    @GetMapping("/employees/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Employee> getCurrentEmployee()
            throws ResourceNotFoundException {
        String userName = getAuthUserName();
        Employee employee = employeeRepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for this username :: " + userName));
        return ResponseEntity.ok().body(employee);
    }


    @ApiOperation(value = "Add an employee")
    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public Employee createEmployee(
            @ApiParam(value = "Employee object store in database table", required = true)
            @Valid @RequestBody Employee employee) {
        employeeRepository.findByUsernameOrEmailId(employee.getUsername(), employee.getEmailId())
                .ifPresent(employee1 -> {
                    throw new RuntimeException("Employee with username or emailId exists");
                });

        return employeeRepository.save(employee);
    }

    @ApiOperation(value = "Update an employee")
    @PutMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> updateEmployee(
            @ApiParam(value = "Employee Id to update employee object", required = true)
            @PathVariable(value = "id") Long employeeId,
            @ApiParam(value = "Update employee object", required = true)
            @Valid @RequestBody Employee employeeDetails) throws ResourceNotFoundException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for this id :: " + employeeId));

        employee.setEmailId(employeeDetails.getEmailId());
        employee.setLastName(employeeDetails.getLastName());
        employee.setFirstName(employeeDetails.getFirstName());
        final Employee updatedEmployee = employeeRepository.save(employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @ApiOperation(value = "Delete an employee")
    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Boolean> deleteEmployee(
            @ApiParam(value = "Employee Id from which employee object will delete from database table", required = true)
            @PathVariable(value = "id") Long employeeId)
            throws ResourceNotFoundException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for this id :: " + employeeId));

        employeeRepository.delete(employee);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
