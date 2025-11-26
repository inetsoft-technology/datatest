# DataTest Summary

Used for performing auto tests on various modules. All modules are not installed to local repository.

## Project Structure

This is a Maven multi-module project containing the following test modules:

- **commons** - Common test utilities module
- **chart** - Chart testing module
- **css** - CSS style testing module
- **docker** - Docker related testing module
- **mv** - MV testing module
- **olap** - OLAP testing module
- **tabular** - Tabular testing module
- **vpm** - VPM testing module
- **vsexport** - VS export testing module
- **vsothers** - VS other features testing module (contains multiple test modes)
- **vsscript** - VS script testing module
- **ws** - Worksheet testing module

## Compile and Run Tests

### Prerequisites: Configure root.dir

Before running tests locally, you **must** modify the `root.dir` property in the root `pom.xml` file.

Open the `pom.xml` file and locate the following configuration:

```xml
<root.dir>/inetsoft/styleBI/test/datatest</root.dir>
<!-- <root.dir>E:\inetsoft\sr14_0\test\datatest</root.dir> -->
```

Uncomment the local path and modify it to your actual project path:

```xml
<!-- <root.dir>/inetsoft/styleBI/test/datatest</root.dir> -->
<root.dir>E:\inetsoft\sr14_0\test\datatest</root.dir>
```

**Note**: Use backslashes `\` for Windows paths and forward slashes `/` for Linux/Mac paths.

### Initialize Environment

First, clean all projects, then package the commons module to your local Maven repository:

```powershell
.\mvnw.cmd clean
.\mvnw.cmd package -pl commons
```

### Execute Tests for Different Modules

#### 1. Modules with Single Test Mode

The following modules have only one test mode and can be executed directly:

```powershell
# Worksheet tests
.\mvnw.cmd test -pl ws

# Chart tests
.\mvnw.cmd test -pl chart

# CSS tests
.\mvnw.cmd test -pl css

# MV tests
.\mvnw.cmd test -pl mv

# OLAP tests
.\mvnw.cmd test -pl olap

# Tabular tests
.\mvnw.cmd test -pl tabular

# VPM tests
.\mvnw.cmd test -pl vpm

# VS export tests
.\mvnw.cmd test -pl vsexport

# VS script tests
.\mvnw.cmd test -pl vsscript
```

#### 2. Modules with Multiple Test Modes (vsothers)

The `vsothers` module contains multiple test modes. Use the `-P` parameter to specify the profile:

```powershell
# VSCalc tests
.\mvnw.cmd test -pl vsothers -Pvscalc

# VSCrosstab tests
.\mvnw.cmd test -pl vsothers -Pvscrosstab

# VSFreehand tests
.\mvnw.cmd test -pl vsothers -Pvsfreehand

# VSPara tests
.\mvnw.cmd test -pl vsothers -Pvspara

# OtherAssembly tests
.\mvnw.cmd test -pl vsothers -Potherassembly
```

### Other Commonly Used Commands

```powershell
# Clean specific module
.\mvnw.cmd clean -pl vsothers -Pvscalc

# Generate test resources (debug mode)
.\mvnw.cmd generate-test-resources -pl vsothers -Pvscalc -X

# Package (skip tests)
.\mvnw.cmd package -DskipTests -pl ws
```

## View Test Results

After tests complete, you can view test results in the following ways:

### 1. Spock Test Reports

Test reports are located in the `target/spock-reports` directory of each module:

```
<module-name>/target/spock-reports/index.html
```

For example:
- `ws/target/spock-reports/index.html`
- `vsothers/target/spock-reports/index.html`

Open the `index.html` file in a browser to view detailed test reports.

### 2. Surefire Test Reports

JUnit format test reports are located in the `target/surefire-reports` directory of each module:

```
<module-name>/target/surefire-reports/
```

This directory contains:
- `TEST-*.xml` - JUnit XML format test results
- `*.txt` - Test output logs

## Git Workflow

### 1. Create Branch

```bash
# Create new branch from main
git checkout -b feature/your-feature-name
```

### 2. Commit Changes

```bash
# Add files to staging area
git add <file-name>
# Or add all changes
git add .

# Commit changes
git commit -m "Describe your changes"
```

### 3. Push Branch

```bash
# Push branch to remote
git push -u origin feature/your-feature-name
```

### 4. Create Pull Request (PR)

After pushing your branch, on your Git hosting platform (GitHub, GitLab, etc.):

1. Go to the repository page
2. You'll usually see a prompt "Compare & pull request", click it
3. Or manually:
   - Click the "Pull Requests" tab
   - Click "New Pull Request"
   - Select your branch and target branch (usually `main`)
   - Fill in PR title and description
   - Add reviewers (if needed)
   - Click "Create Pull Request"

## Important Notes

1. **root.dir Configuration**: Must modify the `root.dir` property in `pom.xml` before running tests locally
2. **commons Module**: Must package the `commons` module before running any tests
3. **Test Resources**: Ensure test resource files (config files, test data) are correctly placed
4. **Java Version**: Project requires Java 21
5. **Memory Settings**: Some tests may require large memory. If encountering out-of-memory errors, adjust JVM parameters

## Docker Environment Testing

Docker environment testing configuration is pending (@TODO)
