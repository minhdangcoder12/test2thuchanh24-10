package com.example.employeemanagement;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.employeemanagement.data.dao.AccountDao;
import com.example.employeemanagement.data.dao.EmployeeDao;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Account;
import com.example.employeemanagement.data.model.Employee;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit Tests cho AccountDao và EmployeeDao
 * Người 1 — feature/person1-auth-employee
 *
 * Chạy: chuột phải vào file → Run
 */
@RunWith(AndroidJUnit4.class)
public class AccountEmployeeDaoTest {

    private AppDatabase db;
    private AccountDao accountDao;
    private EmployeeDao employeeDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Dùng in-memory database để test (không ảnh hưởng DB thật)
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        accountDao  = db.accountDao();
        employeeDao = db.employeeDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    // ===================== ACCOUNT DAO TESTS =====================

    @Test
    public void testInsertAndLoginSuccess() {
        // Tạo tài khoản
        Account acc = new Account("testuser", "password123", "EMPLOYEE");
        accountDao.insert(acc);

        // Login đúng thông tin
        Account found = accountDao.login("testuser", "password123");
        assertNotNull("Phải tìm thấy account khi login đúng", found);
        assertEquals("testuser", found.getUsername());
        assertEquals("EMPLOYEE", found.getRole());
    }

    @Test
    public void testLoginWithWrongPasswordFails() {
        Account acc = new Account("user2", "correctPass", "EMPLOYEE");
        accountDao.insert(acc);

        Account found = accountDao.login("user2", "wrongPass");
        assertNull("Login sai mật khẩu phải trả về null", found);
    }

    @Test
    public void testLoginWithWrongUsernameFails() {
        Account acc = new Account("realuser", "pass123", "ADMIN");
        accountDao.insert(acc);

        Account found = accountDao.login("fakeuser", "pass123");
        assertNull("Login sai username phải trả về null", found);
    }

    @Test
    public void testFindByUsername() {
        Account acc = new Account("admintest", "admin123", "ADMIN");
        accountDao.insert(acc);

        Account found = accountDao.findByUsername("admintest");
        assertNotNull(found);
        assertEquals("ADMIN", found.getRole());
    }

    @Test
    public void testFindByUsernameNotExist() {
        Account found = accountDao.findByUsername("doesnotexist");
        assertNull("Username không tồn tại phải trả về null", found);
    }

    @Test
    public void testUpdateAccount() {
        Account acc = new Account("updateme", "oldpass", "EMPLOYEE");
        long id = accountDao.insert(acc);

        Account inserted = accountDao.findById((int) id);
        inserted.setPassword("newpass");
        accountDao.update(inserted);

        Account updated = accountDao.findById((int) id);
        assertEquals("newpass", updated.getPassword());
    }

    @Test
    public void testDeleteById() {
        Account acc = new Account("deleteme", "pass", "EMPLOYEE");
        long id = accountDao.insert(acc);

        accountDao.deleteById((int) id);

        Account found = accountDao.findById((int) id);
        assertNull("Account đã xóa phải trả về null", found);
    }

    @Test
    public void testGetAllAccounts() {
        accountDao.insert(new Account("user_a", "pass", "EMPLOYEE"));
        accountDao.insert(new Account("user_b", "pass", "EMPLOYEE"));
        accountDao.insert(new Account("admin_c", "pass", "ADMIN"));

        List<Account> all = accountDao.getAll();
        assertTrue("Phải có ít nhất 3 account", all.size() >= 3);
    }

    // ===================== EMPLOYEE DAO TESTS =====================

    @Test
    public void testInsertAndFindEmployee() {
        // Tạo account trước (FK)
        Account acc = new Account("emp1", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);

        Employee emp = new Employee("NV001", "Nguyễn Văn A", "nva@test.com", "0901234567", 50000, (int) accId);
        long empId = employeeDao.insert(emp);

        Employee found = employeeDao.findById((int) empId);
        assertNotNull(found);
        assertEquals("NV001", found.getCode());
        assertEquals("Nguyễn Văn A", found.getFullname());
        assertEquals(50000.0, found.getHourlyRate(), 0.01);
    }

    @Test
    public void testFindByAccountId() {
        Account acc = new Account("emp2", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);

        Employee emp = new Employee("NV002", "Trần Thị B", "ttb@test.com", "0909999999", 45000, (int) accId);
        employeeDao.insert(emp);

        Employee found = employeeDao.findByAccountId((int) accId);
        assertNotNull("Phải tìm thấy nhân viên theo accountId", found);
        assertEquals("NV002", found.getCode());
    }

    @Test
    public void testFindByCode() {
        Account acc = new Account("emp3", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);
        employeeDao.insert(new Employee("NV003", "Lê C", "lc@test.com", "0111111111", 60000, (int) accId));

        Employee found = employeeDao.findByCode("NV003");
        assertNotNull(found);
        assertEquals("Lê C", found.getFullname());
    }

    @Test
    public void testFindByCodeNotExist() {
        Employee found = employeeDao.findByCode("NV999");
        assertNull("Mã không tồn tại phải trả về null", found);
    }

    @Test
    public void testSearchByName() {
        Account acc = new Account("emp4", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);
        employeeDao.insert(new Employee("NV004", "Phạm Văn Dũng", "pvd@test.com", "0900000001", 55000, (int) accId));

        List<Employee> results = employeeDao.search("Dũng");
        assertFalse("Tìm kiếm 'Dũng' phải có kết quả", results.isEmpty());
        assertEquals("Phạm Văn Dũng", results.get(0).getFullname());
    }

    @Test
    public void testSearchByCode() {
        Account acc = new Account("emp5", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);
        employeeDao.insert(new Employee("NV005", "Ngô Thị E", "nte@test.com", "0900000002", 48000, (int) accId));

        List<Employee> results = employeeDao.search("NV005");
        assertFalse(results.isEmpty());
    }

    @Test
    public void testSearchNoResult() {
        List<Employee> results = employeeDao.search("XYZNOTEXIST");
        assertTrue("Tìm không có kết quả phải trả về list rỗng", results.isEmpty());
    }

    @Test
    public void testUpdateEmployee() {
        Account acc = new Account("emp6", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);
        Employee emp = new Employee("NV006", "Vũ F", "vf@test.com", "0900000003", 40000, (int) accId);
        long empId = employeeDao.insert(emp);

        Employee toUpdate = employeeDao.findById((int) empId);
        toUpdate.setHourlyRate(70000);
        toUpdate.setPhone("0999999999");
        employeeDao.update(toUpdate);

        Employee updated = employeeDao.findById((int) empId);
        assertEquals(70000.0, updated.getHourlyRate(), 0.01);
        assertEquals("0999999999", updated.getPhone());
    }

    @Test
    public void testDeleteEmployee() {
        Account acc = new Account("emp7", "pass", "EMPLOYEE");
        long accId = accountDao.insert(acc);
        Employee emp = new Employee("NV007", "Hoàng G", "hg@test.com", "0900000004", 52000, (int) accId);
        long empId = employeeDao.insert(emp);

        Employee toDelete = employeeDao.findById((int) empId);
        employeeDao.delete(toDelete);

        Employee found = employeeDao.findById((int) empId);
        assertNull("Nhân viên đã xóa phải trả về null", found);
    }

    @Test
    public void testGetAllEmployeesOrderedByName() {
        Account a1 = new Account("e_a", "pass", "EMPLOYEE");
        Account a2 = new Account("e_b", "pass", "EMPLOYEE");
        long id1 = accountDao.insert(a1);
        long id2 = accountDao.insert(a2);
        employeeDao.insert(new Employee("NV010", "Zara Z", "z@t.com", "01", 10000, (int) id1));
        employeeDao.insert(new Employee("NV011", "An A", "a@t.com", "02", 20000, (int) id2));

        List<Employee> all = employeeDao.getAll();
        // Kiểm tra sorted theo tên (A trước Z)
        boolean inOrder = false;
        for (int i = 0; i < all.size() - 1; i++) {
            if (all.get(i).getFullname().compareTo(all.get(i+1).getFullname()) <= 0) {
                inOrder = true;
            }
        }
        assertTrue("Danh sách phải sắp xếp theo tên A-Z", inOrder || all.size() <= 1);
    }
}
