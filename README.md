# SpringBot

**SpringBot** is a high-performance, standalone CLI tool designed to scan Spring Boot repositories and generate a token-optimized "Semantic Map" in Markdown.

When feeding large codebases into Large Language Models (LLMs) like GPT-4 or Gemini, raw source code often exhausts the context window. SpringBot solves this by using advanced AST parsing to extract only the critical architectural elements (Beans, Repositories, REST Endpoints, Data Entities, and Configuration) while ignoring method bodies and stripping fully-qualified package names to save tokens.

Official NPM Package : https://www.npmjs.com/package/springbot

---

## Key Features

- **Blazing Fast CLI:** Built on Java 21 using [Picocli](https://picocli.info/) for a native-like command-line experience.
- **Deep AST Parsing:** Uses `JavaParser` with Symbol Resolution to accurately identify Spring annotations (`@Service`, `@RestController`, `@Entity`, etc.) without needing to compile the target project.
- **Modern Java Support:** Fully supports modern Java constructs up to Java 17+, including `record` declarations, enhanced `switch` expressions, and pattern matching.
- **Smart Discovery:** Recursively scans directories for `.java`, `application.properties`, and `.yml` files, flattening nested YAML configurations automatically.
- **Secure Configuration Extraction:** Includes a `ConfigSanitizer` that automatically redacts sensitive keys containing "password", "secret", "token", or "key" before passing them to the LLM.
- **Token-Optimized Output:** Generates a highly dense, hierarchical Markdown file (no heavy Markdown tables) specifically formatted for optimal LLM consumption.

---

## Prerequisites

- **Java 21** (or higher)
- **Maven** (3.6+)

---

## Installation

SpringBot is published on NPM, making it incredibly easy to run globally.

### Option 1: Run instantly with npx (Recommended)

You don't even need to install it! Just use `npx`:

```bash
npx springbot pack
```

### Option 2: Install Globally via NPM

```bash
npm install -g springbot
```

### Option 3: Build from Source

If you want to build the tool locally:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vishwahiremath/SpringContextPacker.git
   cd SpringContextPacker
   ```
2. **Build the CLI tool:**
   ```bash
   mvn clean package -DskipTests
   ```
3. **Run from source:**
   ```bash
   java -jar target/springContextPacker-0.0.1-SNAPSHOT.jar pack
   ```

---

## Usage

SpringBot uses a standard subcommand architecture. The primary command is `pack`.

### Basic Scan

To scan the current directory and generate `semantic-map.md`:

```bash
# If installed globally:
springbot pack

# Or using npx:
npx springbot pack
```

### Scan a Specific Project

Provide the absolute or relative path to a Spring Boot project root:

```bash
npx springbot pack C:\path\to\your\spring-boot-project
```

### Advanced Options

```bash
# Specify a custom output file
npx springbot pack . -o custom-architecture.md

# View the built-in help menu
npx springbot --help
npx springbot help pack
```

---

## Architecture

SpringBot is structured into three primary layers:

1. **Discovery Layer:** (`ProjectScanner`, `ConfigSanitizer`)
   - Recursively walks the file tree.
   - Parses `application.properties` and flattens `application.yml`.
   - Redacts sensitive credentials.
2. **Parser Layer:** (`ParserEnvironment`, `*Visitor`)
   - Initializes a `JavaSymbolSolver` over the target's `src/main/java`.
   - `SpringBeanVisitor`: Extracts `@Service`, `@Component`, `@Configuration` and constructor/field dependencies.
   - `JpaRepositoryVisitor`: Extracts Spring Data JPA interfaces and custom query method signatures.
   - `EntityVisitor`: Extracts `@Entity` and Java `record` declarations (DTOs) along with their fields.
   - `EndpointVisitor`: Extracts `@RestController` endpoints, standardizing paths (`@GetMapping`, `@PostMapping`, etc.).
3. **Generator Layer:** (`MarkdownRenderer`)
   - Aggregates the extracted Java Records into a `ProjectContext`.
   - Renders a clean, nested Markdown list.

---

## License

This project is licensed under the MIT License. See the [LICENSE.md](LICENSE.md) file for details.

---

_Built with ❤️ by Vishwanath Hiremath._
