# 🔍 HOW TO RUN JACOCO COVERAGE REPORT

## 📋 QUICK START (2 commands)

```bash
# 1. Run tests + generate JaCoCo report
cd backend
mvn clean test

# 2. Open report in browser
open target/site/jacoco/index.html
# OR on Linux:
xdg-open target/site/jacoco/index.html
```

---

## 📊 DETAILED STEPS

### **Step 1: Navigate to backend folder**
```bash
cd /home/bao_phan/Projects/ShopCart_FE_BE/backend
```

### **Step 2: Run tests with JaCoCo**
```bash
mvn clean test
```

**This will:**
- ✅ Clean previous build
- ✅ Run all tests
- ✅ Generate JaCoCo coverage report
- ✅ Output: `target/site/jacoco/`

**Output should look like:**
```
[INFO] Running com.shopcart.service.OrderServiceTest
[INFO] Running com.shopcart.service.CartServiceTest
...
[INFO] Tests run: 55, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jacoco:0.8.10:report (report) @ shopcart-backend ---
[INFO] Loading execution data file /path/to/jacoco.exec
[INFO] Analyzed bundle 'ShopCart Backend API' with 50 classes
[INFO] BUILD SUCCESS
```

### **Step 3: View the report**

#### **Option A: Open HTML Report (Easiest)**
```bash
# On Mac
open target/site/jacoco/index.html

# On Linux
xdg-open target/site/jacoco/index.html

# On Windows (PowerShell)
start target\site\jacoco\index.html

# Or manually navigate in VS Code
# target/site/jacoco/index.html
```

#### **Option B: View in VS Code**
1. Right-click on `target/site/jacoco/index.html`
2. Select "Open with Live Server" (if installed)
3. Or copy full path and open in browser

#### **Option C: Check from command line**
```bash
# View coverage summary
cat target/site/jacoco/index.html | grep -i "coverage" | head -5

# Or find coverage percentage
grep -o "Total.*%" target/site/jacoco/index.html || \
  ls -lah target/site/jacoco/ && echo "Report generated in target/site/jacoco/"
```

---

## 📈 WHAT TO LOOK FOR IN THE REPORT

### **Main Coverage Metrics:**

| Metric | What it means |
|--------|---------------|
| **Line Coverage** | % of code lines executed by tests |
| **Branch Coverage** | % of conditional branches tested |
| **Complexity Coverage** | How complex logic is covered |
| **Method Coverage** | % of methods tested |

### **Target:**
```
✅ >= 85% overall coverage
✅ Critical paths: 90%+
⚠️  < 80%: May lose points
```

---

## 🔧 ADVANCED OPTIONS

### **Generate report WITHOUT running tests again**
```bash
# If tests already ran, just generate report
mvn jacoco:report
```

### **Run specific test class with JaCoCo**
```bash
mvn clean test -Dtest=OrderServiceTest
```

### **View detailed coverage by package**
1. Open `target/site/jacoco/index.html`
2. Click on package (e.g., `com.shopcart.service`)
3. See class-by-class breakdown
4. Click on class name to see line-by-line coverage

### **Export CSV report**
```bash
# JaCoCo automatically generates:
target/site/jacoco/*.csv  # For analysis tools
```

---

## 📁 GENERATED FILES

After `mvn clean test`:
```
backend/
└── target/
    ├── jacoco.exec                    ← Coverage data
    └── site/jacoco/
        ├── index.html                 ← Main report ⭐
        ├── com.shopcart.service/      ← Service package
        ├── com.shopcart.entity/       ← Entity package
        ├── com.shopcart.dto/          ← DTO package
        └── ...
```

---

## ✅ COMMON SCENARIOS

### **Scenario A: Just want to see coverage %**
```bash
cd backend
mvn clean test
# Look for "jacoco:report" output in console
# Open target/site/jacoco/index.html
```

### **Scenario B: Check if coverage >= 85%**
```bash
mvn clean test
# Then manually check index.html for coverage %
# If < 85%, add more tests or improve existing ones
```

### **Scenario C: Compare coverage between runs**
```bash
# Run 1 (before changes)
mvn clean test
# Save: target/site/jacoco/

# Run 2 (after changes)
mvn clean test
# Compare reports
```

---

## 🚀 QUICK COMMANDS (Copy-Paste)

```bash
# Navigate to backend
cd /home/bao_phan/Projects/ShopCart_FE_BE/backend

# Run tests + generate JaCoCo report
mvn clean test

# View report (choose one)
# macOS:
open target/site/jacoco/index.html
# Linux:
xdg-open target/site/jacoco/index.html
# Or just open manually in VS Code browser
```

---

## 🎯 EXPECTED OUTPUT

**When you run `mvn clean test`:**

```
✅ 55 tests run
✅ 0 failures
✅ 0 errors
✅ 0 skipped
✅ JaCoCo report generated
✅ Coverage report available at: target/site/jacoco/index.html
```

**When you open index.html:**
- Shows overall coverage % (should be >= 85%)
- Break down by package
- Break down by class
- Line-by-line coverage in each class

---

## ⚠️ TROUBLESHOOTING

### **Problem: Report not generated**
```bash
# Solution: Check pom.xml has jacoco plugin
mvn clean test
# If still not generated, check Maven output for errors
mvn clean test -X  # Enable debug mode
```

### **Problem: Coverage seems low**
```bash
# Check which classes have low coverage
# Open target/site/jacoco/index.html
# Look for red/yellow lines (not fully covered)
# Add tests for those classes
```

### **Problem: Can't open HTML file**
```bash
# Copy file path and open manually
# Example path:
/home/bao_phan/Projects/ShopCart_FE_BE/backend/target/site/jacoco/index.html
# Paste in browser address bar
```

---

## 💡 TIPS

✅ **Run regularly** - Check coverage after adding new tests  
✅ **Target 90%** - Aim higher than 85% to have buffer  
✅ **Focus on critical code** - Services, DTOs are important  
✅ **Use report to guide** - Report shows what's not covered  

---

## 📊 SAMPLE REPORT INTERPRETATION

```
Overall Coverage: 87%
├── Instructions: 87%    ← Code execution coverage
├── Branches: 82%        ← If/else branches
├── Lines: 88%           ← Source lines
├── Methods: 90%         ← Function/method coverage
└── Classes: 95%         ← Class coverage

Package: com.shopcart.service
├── CartServiceImpl: 92%  ✅ Good
├── OrderServiceImpl: 88% ✅ Good
└── ProductServiceImpl: 78% ⚠️ Needs more tests

File: OrderServiceImpl.java
├── Line 45: ✅ covered
├── Line 56: ❌ NOT covered
└── Line 67: ⚠️ Partial coverage
```

---

**Ready? Run this now:**
```bash
cd /home/bao_phan/Projects/ShopCart_FE_BE/backend && mvn clean test
```

Then open: `target/site/jacoco/index.html` 🚀
