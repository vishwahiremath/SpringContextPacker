# Spring Project Semantic Map

## Configuration Properties
- `spring.ai.google.genai.api-key`: ***REDACTED***
- `logging.level.com.example.amisecure`: INFO
- `amisecure.zap.api-key`: ***REDACTED***
- `logging.file.path`: logs
- `logging.file.name`: logs/amisecure.log
- `spring.application.name`: amisecure
- `amisecure.jwt.expiration`: 86400000
- `spring.profiles.active`: dev
- `logging.level.com.example.amisecure.service.checks`: INFO
- `spring.ai.google.genai.chat.options.model`: gemini-2.5-flash
- `spring.security.oauth2.client.registration.google.client-secret`: ***REDACTED***
- `amisecure.scan.queue-capacity`: 100
- `amisecure.jwt.secret`: ***REDACTED***
- `spring.security.oauth2.client.registration.google.client-id`: YOUR_GOOGLE_CLIENT_ID
- `amisecure.scan.thread-pool-size`: 10
- `spring.data.mongodb.uri`: ${MONGODB_URI:mongodb://localhost:27017/ami-knowledge}
- `amisecure.google.safe-browsing-api-key`: ***REDACTED***
- `spring.security.oauth2.client.registration.google.scope`: email,profile

## Data Entities
- **ComplianceQuestionnaireTemplate** (Table: `compliance_questionnaire_templates`)
  - String id
  - String domainIdentifier
  - Map<String,Object> questionnaireTree
  - String version
- **VulnerabilityKnowledgeBase** (Table: `vulnerabilities-kb`)
  - String id
  - String cveId
  - String vulnerabilityName
  - Map<String,Object> aiRemediationSteps
  - Map<String,List<String>> complianceMappings
  - Date lastUpdated
- **AiScanRemediate** (Table: `ai_scan_remediate`)
  - UUID id
  - byte[] remediationPlan
  - ScanReport scanReport
- **ComplianceReport** (Table: `compliance_reports`)
  - UUID id
  - String compliance
  - String clause
  - String status
  - List<String> vulnerability
  - ScanReport scanReport
  - LocalDateTime createdAt
- **ScanReport** (Table: `scan_reports`)
  - UUID id
  - User user
  - String url
  - String scanType
  - String status
  - LocalDateTime createdAt
  - List<Vulnerability> vulnerabilities
  - List<ComplianceReport> complianceReports
  - AiScanRemediate aiScanRemediate
- **User** (Table: `users`)
  - UUID id
  - String email
  - String password
  - String fullName
  - AuthProvider provider
  - String providerId
  - String role
  - String resetToken
  - LocalDateTime resetTokenExpiry
  - List<ScanReport> scanReports
- **Vulnerability** (Table: `vulnerabilities`)
  - UUID id
  - String title
  - String severity
  - String description
  - String recommendation
  - ScanReport scanReport

## JPA Repositories
- AiScanRemediateRepository {
      Optional<AiScanRemediate> findByScanReportId(UUID scanId);
      long countByScanReport_User(User user);
  }
- ComplianceReportRepository {
      List<ComplianceReport> findByScanReportId(UUID scanReportId);
      long countByScanReport_UserAndStatus(User user, String status);
      long countByScanReport_User(User user);
  }
- ScanReportRepository {
      List<ScanReport> findAllByUserOrderByCreatedAtDesc(User user);
      Page<ScanReport> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
      Page<ScanReport> findAllByUserAndUrlContainingIgnoreCaseOrderByCreatedAtDesc(User user, String url, Pageable pageable);
      long countByUser(User user);
      long countByUserAndStatus(User user, String status);
      List<ScanReport> findAllByUserAndCreatedAtAfter(User user, LocalDateTime date);
      List<ScanReport> findAllByUserAndStatus(User user, String status);
      long countDistinctUrl();
  }
- UserRepository {
      Optional<User> findByEmail(String email);
      Optional<User> findByResetToken(String resetToken);
  }
- VulnerabilityRepository {
      long countByScanReport_UserAndSeverity(User user, String severity);
  }

## REST Endpoints
- `POST /api/v1/auth/register`
  - Handler: `ResponseEntity<AuthResponse> register(RegisterRequest request)`
- `POST /api/v1/auth/login`
  - Handler: `ResponseEntity<AuthResponse> login(LoginRequest request)`
- `POST /api/v1/auth/forgot-password`
  - Handler: `ResponseEntity<Map<String,String>> forgotPassword(String email)`
- `POST /api/v1/auth/reset-password`
  - Handler: `ResponseEntity<Map<String,String>> resetPassword(String token, String newPassword)`
- `GET /api/v1/auth/oauth2/success`
  - Handler: `ResponseEntity<Map<String,String>> oauth2Success()`
- `GET /api/v1/dashboard/stats`
  - Handler: `ResponseEntity<DashboardStatsResponse> getDashboardStats(Principal principal)`
- `GET /api/v1/dashboard/trends`
  - Handler: `ResponseEntity<List<ScanTrendPoint>> getScanTrends(Principal principal)`
- `GET /api/v1/health`
  - Handler: `ResponseEntity<Map<String,Object>> healthCheck()`
- `GET /api/v1/knowledge-base/vulnerability/{cveId}`
  - Handler: `ResponseEntity<VulnerabilityKnowledgeBase> getVulnerabilityData(String cveId)`
- `GET /api/v1/knowledge-base/questionnaire/{domainId}`
  - Handler: `ResponseEntity<ComplianceQuestionnaireTemplate> getQuestionnaireTemplate(String domainId)`
- `GET /api/v1/public/metrics`
  - Handler: `ResponseEntity<PublicMetricsResponse> getPublicMetrics()`
- `POST /api/v1/scan/full`
  - Handler: `ResponseEntity<ScanResponse> startFullScan(ScanRequest request, Principal principal)`
- `POST /api/v1/scan/basic`
  - Handler: `ResponseEntity<ScanResponse> startBasicScan(ScanRequest request, Principal principal)`
- `POST /api/v1/scan/premium`
  - Handler: `ResponseEntity<ScanResponse> startPremiumScan(ScanRequest request, Principal principal)`
- `POST /api/v1/scan`
  - Handler: `ResponseEntity<ScanResponse> startScan(ScanRequest request, Principal principal)`
- `GET /api/v1/scan/history`
  - Handler: `ResponseEntity<Page<ScanReport>> getScanHistory(int page, int size, String search, Principal principal)`
- `GET /api/v1/scan/{id}`
  - Handler: `ResponseEntity<ScanReport> getScanResult(UUID id)`
- `GET /api/v1/scan/{id}/remediate`
  - Handler: `ResponseEntity<AiRemediationResponse> getAiRemediation(UUID id)`
- `GET /api/v1/scan/{id}/remediate/structured`
  - Handler: `ResponseEntity<List<StructuredRemediation>> getStructuredRemediation(UUID id)`
- `GET /api/v1/scan/{id}/get-remediation-plan`
  - Handler: `ResponseEntity<RemediationPlanResponse> getSavedRemediationPlan(UUID id)`
- `POST /api/v1/zap-scan`
  - Handler: `ResponseEntity<ScanResponse> startZapScan(ScanRequest request, Principal principal)`
- `GET /api/v1/zap-scan/{id}`
  - Handler: `ResponseEntity<ScanReport> getZapScanResult(UUID id)`

## Spring Beans & Wiring
- **ApplicationConfig** (`@Configuration`)
  - Dependencies:
    - UserRepository
- **AsyncConfig** (`@Configuration`)
  - Dependencies: None
- **CustomOAuth2SuccessHandler** (`@Component`)
  - Dependencies: None
- **DataStoreConfig** (`@Configuration`)
  - Dependencies: None
- **JacksonConfig** (`@Configuration`)
  - Dependencies: None
- **JwtAuthenticationFilter** (`@Component`)
  - Dependencies: None
- **SecurityConfig** (`@Configuration`)
  - Dependencies: None
- **WebSocketConfig** (`@Configuration`)
  - Dependencies: None
- **ZapProperties** (`@Configuration`)
  - Dependencies: None
- **AuthController** (`@RestController`)
  - Dependencies: None
- **DashboardController** (`@RestController`)
  - Dependencies: None
- **HealthController** (`@RestController`)
  - Dependencies: None
- **KnowledgeBaseController** (`@RestController`)
  - Dependencies: None
- **PublicMetricsController** (`@RestController`)
  - Dependencies: None
- **ScanController** (`@RestController`)
  - Dependencies: None
- **ZapScanController** (`@RestController`)
  - Dependencies: None
- **AiKnowledgeBaseService** (`@Service`)
  - Dependencies: None
- **AiRemediationService** (`@Service`)
  - Dependencies: None
- **AuthService** (`@Service`)
  - Dependencies: None
- **DnsCheck** (`@Component`)
  - Dependencies: None
- **FileProbeCheck** (`@Component`)
  - Dependencies: None
- **HeadersCheck** (`@Component`)
  - Dependencies: None
- **HttpMethodsCheck** (`@Component`)
  - Dependencies: None
- **PhishTankCheck** (`@Component`)
  - Dependencies: None
- **RobotsTxtCheck** (`@Component`)
  - Dependencies: None
- **SafeBrowsingCheck** (`@Component`)
  - Dependencies: None
- **SslCheck** (`@Component`)
  - Dependencies: None
- **CmsFingerprintCheck** (`@Component`)
  - Dependencies: None
- **CorsCheck** (`@Component`)
  - Dependencies: None
- **SmartFuzzingCheck** (`@Component`)
  - Dependencies: None
- **SubdomainDiscoveryCheck** (`@Component`)
  - Dependencies: None
- **GeminiServiceImpl** (`@Service`)
  - Dependencies: None
- **PublicMetricsServiceImpl** (`@Service`)
  - Dependencies: None
- **ScannerServiceImpl** (`@Service`)
  - Dependencies:
    - ScanReportRepository
    - SimpMessagingTemplate
    - List<SecurityCheck>
- **JwtService** (`@Service`)
  - Dependencies: None

