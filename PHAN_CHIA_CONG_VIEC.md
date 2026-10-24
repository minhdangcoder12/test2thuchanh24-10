# 📋 KẾ HOẠCH PHÂN CHIA CÔNG VIỆC — Quản lý Giờ Làm & Lương Nhân Viên

> **Stack:** Android Java · Room Database · SharedPreferences · Material Design 3

---

## 🏗️ Cấu trúc dự án đã tạo sẵn

```
app/src/main/java/com/example/employeemanagement/
├── data/
│   ├── model/          → Account, Employee, WorkLog, Salary ✅
│   ├── dao/            → AccountDao, EmployeeDao, WorkLogDao, SalaryDao ✅
│   └── database/       → AppDatabase (Room singleton + seed) ✅
├── utils/
│   ├── SessionManager.java   (SharedPreferences login) ✅
│   └── SalaryCalculator.java (tính lương, overtime) ✅
└── ui/
    ├── auth/           → LoginActivity ✅
    ├── admin/          → Dashboard, EmployeeList, AddEdit, Salary, Attendance ✅
    ├── employee/       → Dashboard, CheckInOut, History, Salary, Profile ✅
    └── adapter/        → EmployeeAdapter, WorkLogAdapter, SalaryAdapter ✅
```

**Tài khoản admin mặc định:** `admin / admin123`

---

## 👥 PHÂN CHIA CÔNG VIỆC CHO 3 NGƯỜI

---

### 🧑‍💻 NGƯỜI 1 — Authentication & Employee Management (Admin)

**Branch:** `feature/person1-auth-employee`

#### Nhiệm vụ:

| # | Công việc | File cần làm |
|---|-----------|-------------|
| 1 | **Hoàn thiện Login UI** — Validate form, hiển thị lỗi đẹp | `LoginActivity.java` + `activity_login.xml` |
| 2 | **Quản lý nhân viên** — Hoàn thiện RecyclerView + search real-time | `EmployeeListActivity.java` + `EmployeeAdapter.java` |
| 3 | **Form thêm/sửa nhân viên** — Validate đầy đủ, kiểm tra mã trùng | `AddEditEmployeeActivity.java` + `activity_add_edit_employee.xml` |
| 4 | **Xem chi tiết nhân viên** — Click item → xem full info | Tạo `EmployeeDetailActivity.java` mới |
| 5 | **Unit Test** — Test AccountDao, EmployeeDao | Tạo test files trong `androidTest/` |

#### Hướng dẫn bắt đầu:
```bash
git checkout -b feature/person1-auth-employee
# Mở Android Studio > Open project tại thư mục gốc
# Sync Gradle, chạy thử trên emulator
```

---

### 🧑‍💻 NGƯỜI 2 — Attendance (Check-in/Check-out) & WorkLog

**Branch:** `feature/person2-attendance`

#### Nhiệm vụ:

| # | Công việc | File cần làm |
|---|-----------|-------------|
| 1 | **Màn hình chấm công** — Hiển thị đồng hồ real-time, nút check-in/out | `CheckInOutActivity.java` + `activity_check_in_out.xml` |
| 2 | **Validation chấm công** — Không cho check-in 2 lần, không cho check-out trước check-in | `CheckInOutActivity.java` |
| 3 | **Lịch sử chấm công nhân viên** — RecyclerView theo tháng + tổng kết | `AttendanceHistoryActivity.java` + `WorkLogAdapter.java` |
| 4 | **Báo cáo chấm công (Admin)** — Xem tất cả nhân viên theo tháng | `AttendanceReportActivity.java` |
| 5 | **Tính giờ tự động** — Kiểm tra `SalaryCalculator.calculateHours()` chính xác | `SalaryCalculator.java` + test |

#### Điểm cần chú ý:
- Dùng `SimpleDateFormat("yyyy-MM-dd")` cho date, `("HH:mm:ss")` cho time
- Kiểm tra `WorkLog.status` = "PRESENT" / "INCOMPLETE"
- Hiển thị đồng hồ realtime với `Handler.postDelayed()`

```bash
git checkout -b feature/person2-attendance
```

---

### 🧑‍💻 NGƯỜI 3 — Salary Calculation & Employee Self-Service

**Branch:** `feature/person3-salary`

#### Nhiệm vụ:

| # | Công việc | File cần làm |
|---|-----------|-------------|
| 1 | **Tính lương tự động (Admin)** — Tính lương cho tất cả nhân viên theo tháng | `SalaryManagementActivity.java` + `SalaryAdapter.java` |
| 2 | **Quản lý lương (Admin)** — Đánh dấu đã trả, xem chi tiết | `SalaryManagementActivity.java` |
| 3 | **Xem lương (Employee)** — Hiển thị chi tiết lương tháng: giờ thường, OT, tổng | `SalaryDetailActivity.java` + `activity_salary_detail.xml` |
| 4 | **Hồ sơ cá nhân (Employee)** — Xem thông tin cá nhân | `ProfileActivity.java` + `activity_profile.xml` |
| 5 | **Công thức OT** — Tùy chỉnh tỷ lệ OT, test tính toán | `SalaryCalculator.java` |

#### Công thức lương:
```
Lương = Giờ_thường × Đơn_giá + Giờ_OT × Đơn_giá × 1.5
Giờ OT = Tổng_giờ - 160h (nếu > 160h/tháng)
```

```bash
git checkout -b feature/person3-salary
```

---

## 🚀 Quy trình GitHub Collaboration

### Bước 1 — Setup ban đầu (1 người làm, 2 người clone)
```bash
# Người 1 (leader): Tạo repo trên GitHub, push code gốc
cd "c:\Users\nguye\OneDrive\Máy tính\test2 thuchanhmobile"
git init
git add .
git commit -m "feat: initial project structure - all 4 tables, base activities"
git branch -M main
git remote add origin https://github.com/<username>/<repo-name>.git
git push -u origin main

# Người 2 & 3: Clone về
git clone https://github.com/<username>/<repo-name>.git
```

### Bước 2 — Làm việc theo branch
```bash
# Mỗi người tạo branch của mình
git checkout -b feature/person1-auth-employee   # người 1
git checkout -b feature/person2-attendance      # người 2
git checkout -b feature/person3-salary          # người 3

# Commit thường xuyên (ít nhất 1 commit/tính năng nhỏ)
git add .
git commit -m "feat: implement real-time search for employee list"
git push origin feature/person1-auth-employee
```

### Bước 3 — Merge vào main
```bash
# Tạo Pull Request trên GitHub
# Nhờ 1 người khác review trước khi merge
# Merge bằng GitHub UI hoặc:
git checkout main
git merge --no-ff feature/person1-auth-employee
git push origin main
```

---

## ✅ Checklist hoàn thành

### Người 1
- [ ] Login với validation đầy đủ
- [ ] Danh sách nhân viên với tìm kiếm
- [ ] Thêm nhân viên (+ tạo account)
- [ ] Sửa thông tin nhân viên
- [ ] Xóa nhân viên (xóa cả account)
- [ ] SharedPreferences auto-login

### Người 2
- [ ] Check-in ghi giờ vào
- [ ] Check-out ghi giờ ra + tính tổng giờ
- [ ] Chặn check-in trùng ngày
- [ ] Chặn check-out trước giờ vào
- [ ] Xem lịch sử theo tháng
- [ ] Tổng kết giờ làm theo tháng

### Người 3
- [ ] Tính lương tự động theo tháng
- [ ] Hiển thị lương thường + OT riêng biệt
- [ ] Đánh dấu trạng thái "Đã trả"
- [ ] Employee xem lương của bản thân
- [ ] Employee xem hồ sơ cá nhân
- [ ] Test tính toán với nhiều trường hợp

---

## 📌 Lưu ý kỹ thuật chung

1. **Luôn chạy database trên background thread:**
   ```java
   AppDatabase.databaseWriteExecutor.execute(() -> {
       // Room operations here
       runOnUiThread(() -> { /* Update UI */ });
   });
   ```

2. **Định dạng ngày/giờ nhất quán:**
   - Date: `yyyy-MM-dd` (ví dụ: `2025-10-24`)
   - Time: `HH:mm:ss` (ví dụ: `08:30:00`)

3. **Không hardcode string** — dùng `res/values/strings.xml`

4. **Test với emulator:** minSdk 26 (Android 8.0+)
