package warehouse.fh_muenster.de.warehouse;

import warehouse.fh_muenster.de.warehouse.Server.ServerMockImple;

/**
 * Created by Thomas on 09.05.2016.
 */
public class Employee {


    private int     employeeNr;
    private String  password;
    private Role    role;
    private int     sessionId;

    private ServerMockImple server;


    public Employee(int employeeNr, String password) {
        this.employeeNr = employeeNr;
        this.password = password;
    }

    public Employee(int employeeNr,  int sessionId) {
        this.employeeNr = employeeNr;
        this.sessionId = sessionId;
    }

    public Employee() {
    }

    public int getEmployeeNr() {
        return employeeNr;
    }

    public void setEmployeeNr(int employeeNr) {
        this.employeeNr = employeeNr;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString(){
        return "EmployeeNr: " + employeeNr + " Password: " + password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}
