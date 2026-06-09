# Auto-Execution — Template Creation → Execution Auth Walkthrough

This traces the implemented offline-token auth flow, from the moment a user creates the
**first execution template** for a study, through the datasets MS restart that fires the
pipeline on VIP. For the runtime two-phase queueing model, see `AUTO_EXEC_PROCESS.md`;
this document focuses on **how the user's credentials travel** from creation to execution.

**Chosen solution:** Keycloak **offline access token, stored per template** (Option A in
`AUTO_EXEC_AUTH.md`). When a template is created, the user's Keycloak refresh/offline token
is captured in the browser and persisted with the template. At execution time the backend
refreshes that token to obtain a live access token, rebuilds a security context for the
original user, and uses it both to call VIP and to embed credentials in the `shanoir://`
data URIs.

---

## Phase 1 — Template creation (frontend)

### 1.1 User saves the form

`shanoir-ng-front/src/app/vip/execution-template/execution-template.component.ts`

```ts
async save(): Promise<ExecutionTemplate> {
    this.templateService.checkOneDatasetGroup(this)
    this.templateService.cleanParameters(this)
    this.templateService.shapeParameterEntities(this)
    await this.templateService.updateEntityOnSave(this)   // ← offline token captured here
    super.save()
    return Promise.resolve(this.entity)
}
```

`save()` is now `async` and **awaits** `updateEntityOnSave` so the token is on the entity
before `super.save()` POSTs it.

### 1.2 The offline token is read from Keycloak

`execution-template.service.ts`

```ts
async updateEntityOnSave(template: ExecutionTemplateComponent): Promise<void> {
    const formValue = template.form.value
    template.entity.filterCombination = formValue.filterCombination
    template.entity.priority = formValue.priority
    template.entity.name = formValue.name
    template.entity.pipelineName = formValue.pipelineName
    template.entity.offlineToken = await this.keycloakService.getRefreshToken()  // ←
}
```

`shared/keycloak/keycloak.service.ts`

```ts
getRefreshToken(): Promise<string> {
    this.tokenPromise = new Promise<string>((resolve, reject) => {
        if (KeycloakService.auth.authz.token) {
            resolve(KeycloakService.auth.authz.refreshToken as string);
        } else {
            reject();
        }
    });
    return this.tokenPromise;
}
```

The value carried on the model is the Keycloak **refresh token** of the current user
session. For it to behave as a non-expiring *offline* token, the session must have been
established with the `offline_access` scope (Keycloak client configuration — see
`AUTO_EXEC_AUTH.md`).

Model field — `vip/models/execution-template.ts`:
```ts
offlineToken?: string
```

---

## Phase 2 — Persistence (datasets backend)

### 2.1 Controller

`vip/executionTemplate/controller/ExecutionTemplateApiController.java`

```java
public ResponseEntity<ExecutionTemplateDTO> saveNewExecutionTemplate(@RequestBody ExecutionTemplateDTO executionTemplateDTO) {
    return new ResponseEntity<>(
        mapper.executionTemplateToDTO(
            repository.save(mapper.executionTemplateDTOToEntity(executionTemplateDTO))),
        HttpStatus.OK);
}
```

### 2.2 DTO → entity mapping copies the token through

`vip/executionTemplate/dto/mapper/ExecutionTemplateDecorator.java`

```java
@Override
public ExecutionTemplate executionTemplateDTOToEntity(ExecutionTemplateDTO dto) {
    ...
    entity.setParameters(ExecutionTemplateParameterDTO.toEntities(dto.getParameters(), entity));
    entity.setOfflineToken(dto.getOfflineToken());   // ←
    return entity;
}
```

The reverse mapping `executionTemplateToDTO` (entity → DTO, the **read/response** direction)
does **not** copy `offlineToken` back out — so the token is write-only over the API and
never returned to clients.

### 2.3 Storage

`vip/executionTemplate/model/ExecutionTemplate.java`
```java
@Column(length = 2000)
private String offlineToken;
```

Migration `0073_add_offline_token_to_execution_template.sql`
```sql
ALTER TABLE execution_template ADD COLUMN offline_token VARCHAR(2000);
```

Stored as **plaintext** for now (encryption-at-rest deferred — tracked separately).

The first template for the study now exists in `execution_template` with its
`offline_token`, plus its filter/parameter rows.

---

## Phase 3 — New data imported

Between now and the next restart, DICOMs are imported normally. Each new
`dataset_acquisition` gets an id higher than the `import_exec_count` watermark in
`miscellaneous_parameter`. No auth involved yet — the template waits in the DB.

---

## Phase 4 — Datasets MS restart: detection & planning

`vip/executionTemplate/service/ExecutionTemplateRunner.java` (`ApplicationRunner`)

```java
SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");   // mock ctx, internal checks only

MiscellaneousParameter importExecParam = miscelleneousParamRepository.findById("import_exec_count")...;
long importExecCount = Long.parseLong(importExecParam.getValue());

List<DatasetAcquisition> newAcquisitions = acquisitionRepository.findByIdGreaterThan(importExecCount);
...
importExecParam.setValue(newAcquisitions.stream().map(DatasetAcquisition::getId).reduce(Long::max).get().toString());
miscelleneousParamRepository.save(importExecParam);

newAcquisitions.stream()
    .collect(Collectors.groupingBy(a -> a.getExamination().getId()))
    .values()
    .forEach(group -> templateService.createExecutionsFromExecutionTemplates(group));
```

Matching acquisitions are persisted as `planned_execution` rows, then
`PlannedExecutionService.applyExecution(...)` picks the execution level and enqueues work
onto `PlannedExecutionManager`.

The mock `ROLE_ADMIN` context here is enough for **planning** (DB reads/writes, internal
authorization). The real user token is only needed in Phase 5, where VIP is contacted.

---

## Phase 5 — Execution: the offline token becomes a live token

This is where the auth solution does its work.
`vip/executionTemplate/service/PlannedExecutionManager.java` → `threadExecution(...)`

```java
private void threadExecution(ExecutionTemplate template, Long objectId, String executionLevel,
                             List<Long> plannedExecutionToRemoveWithAcquisitionId) {
    String offlineToken = template.getOfflineToken();
    if (offlineToken == null) {
        LOG.error("No offline token stored for template {}. Cannot execute without user credentials.", template.getId());
        return;
    }

    try {
        // 1. Exchange the stored offline token for a fresh access token
        AccessTokenResponse tokenResponse = keycloakServiceAccountUtils.refreshUserToken(offlineToken);

        // 2. Decode the new access token to recover who the user is
        Map<String, Object> claims = SecurityContextUtil.decodeJwtClaims(tokenResponse.getToken());
        String username = (String) claims.getOrDefault("preferred_username", "shanoir");
        Object userIdRaw = claims.get("userId");
        Long userId = userIdRaw != null ? Long.valueOf(userIdRaw.toString()) : 92233720L;

        // 3. Build a security context carrying the REAL access token
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN", username, userId, tokenResponse.getToken());
    } catch (SecurityException e) {
        LOG.error("Failed to refresh user token for template {}. Execution aborted.", template.getId(), e);
        return;
    }

    try {
        ExecutionCandidateDTO candidate = plannedExecutionServiceImpl.prepareExecutionCandidate(template, executionLevel, objectId);
        if (Objects.nonNull(candidate)) {
            candidate.setRefreshToken(offlineToken);        // 4. refresh token rides into the data URIs
            IdName monitoringIdName = executionService.createExecutions(List.of(candidate));
            ...
            for (Long acquisitionId : plannedExecutionToRemoveWithAcquisitionId) {
                plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(acquisitionId, template.getId());
            }
            // then poll VIP status until no longer RUNNING
        }
    }
    ...
}
```

### 5.1 Refresh the token — `KeycloakServiceAccountUtils.refreshUserToken`

`shanoir-ng-ms-common/.../KeycloakServiceAccountUtils.java`

```java
@Value("${user-token.client.id:shanoir-ng-front}")
private String userTokenClientId;

public AccessTokenResponse refreshUserToken(String refreshToken) throws SecurityException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("client_id", this.userTokenClientId);        // public front client, no secret
    map.add("grant_type", "refresh_token");
    map.add("refresh_token", refreshToken);
    ...
    return this.restTemplate.exchange(this.serverUrl, HttpMethod.POST, entity, AccessTokenResponse.class).getBody();
}
```

A standard OAuth `grant_type=refresh_token` call. The `client_id` is the public front
client (`shanoir-ng-front` by default, overridable via `user-token.client.id`) — it must
match the client the token was originally issued to, and since that client is public no
secret is sent. (Contrast with `getServiceAccountAccessToken()`, which uses
`client_credentials` + secret.)

### 5.2 Rebuild the user context — `SecurityContextUtil`

`shanoir-ng-ms-common/.../SecurityContextUtil.java`

```java
public static void initAuthenticationContext(String role, String username, Long userId, String accessToken) {
    ...
    Map<String, Object> claims = Map.of("preferred_username", username, "userId", userId, "realm_access", grantedAuthorities);
    Jwt jwt = new Jwt(accessToken, Instant.now(), Instant.now().plusSeconds(300), Map.of("header", "mock"), claims);
    Authentication authentication = new JwtAuthenticationToken(jwt, grantedAuthorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
}

public static Map<String, Object> decodeJwtClaims(String accessToken) {
    String[] parts = accessToken.split("\\.");
    byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);     // JWT payload segment
    return new ObjectMapper().readValue(new String(decoded), Map.class);
}
```

The crucial difference from the startup mock context: the `Jwt` now wraps the **real access
token value** returned by Keycloak. So when downstream code calls `KeycloakUtil.getToken()`
/ `getKeycloakHeader()`, it gets a token VIP and Shanoir's own `/carmin-data/path/`
endpoint will actually accept. This is also what attributes the execution to the original
template creator (`preferred_username`, `userId`) rather than a generic service account.

### 5.3 Token reaches VIP in two places

Downstream in `ExecutionServiceImpl` (unchanged by this branch, but now fed a real token):

```java
// Authorization header on the VIP REST call
.headers(headers -> headers.addAll(utils.getUserHttpHeaders()))   // KeycloakUtil.getKeycloakHeader()

// embedded in the shanoir:// data URIs VIP calls back with
"?...&token=" + KeycloakUtil.getToken()
       + "&refreshToken=" + candidate.getRefreshToken()   // ← the offline token from step 4
       + "&clientId=" + candidate.getClient() + "...";
```

VIP therefore receives:
- a **live access token** (header + URI `token=`) to authenticate immediately, and
- the **offline/refresh token** (URI `refreshToken=`) so it can refresh on its own for
  long-running pipelines.

### 5.4 Cleanup

After VIP accepts the execution, the corresponding `planned_execution` rows are deleted
and the manager polls VIP status until the execution leaves `RUNNING`.

---

## End-to-end summary

```
[Browser] user saves template
   │  keycloakService.getRefreshToken()  →  entity.offlineToken
   ▼
[POST]  ExecutionTemplateApiController.saveNewExecutionTemplate
   │  Decorator.executionTemplateDTOToEntity → setOfflineToken
   ▼
execution_template.offline_token  (DB, plaintext, write-only over API)
   │
   │   ... DICOM imports raise dataset_acquisition ids ...
   ▼
[Datasets MS restart]  ExecutionTemplateRunner
   │  detect new acquisitions vs import_exec_count
   │  match templates → planned_execution rows
   ▼
PlannedExecutionManager.threadExecution
   │  1. refreshUserToken(offlineToken)         → live access token (Keycloak)
   │  2. decodeJwtClaims → username, userId
   │  3. initAuthenticationContext(real token)  → user security context
   │  4. candidate.refreshToken = offlineToken
   ▼
ExecutionServiceImpl  → VIP
   │  header: Bearer <live access token>
   │  data URIs: token=<live>  refreshToken=<offline>
   ▼
VIP runs pipeline, calls back /carmin-data/path to download datasets
   │
   ▼
planned_execution rows deleted, status polled until done
```

## Files in this flow

| Layer | File |
|---|---|
| Frontend model | `vip/models/execution-template.ts` |
| Frontend save | `vip/execution-template/execution-template.component.ts` |
| Frontend token read | `vip/execution-template/execution-template.service.ts`, `shared/keycloak/keycloak.service.ts` |
| API DTO | `vip/executionTemplate/dto/ExecutionTemplateDTO.java` |
| Controller | `vip/executionTemplate/controller/ExecutionTemplateApiController.java` |
| Mapping | `vip/executionTemplate/dto/mapper/ExecutionTemplateDecorator.java` |
| Entity / schema | `vip/executionTemplate/model/ExecutionTemplate.java`, `0073_add_offline_token_to_execution_template.sql` |
| Startup runner | `vip/executionTemplate/service/ExecutionTemplateRunner.java` |
| Planning | `vip/executionTemplate/service/ExecutionTemplateServiceImpl.java`, `PlannedExecutionServiceImpl.java` |
| Execution / token use | `vip/executionTemplate/service/PlannedExecutionManager.java` |
| Token refresh | `shared/security/KeycloakServiceAccountUtils.java` (`refreshUserToken`) |
| Security context | `utils/SecurityContextUtil.java` (`initAuthenticationContext`, `decodeJwtClaims`) |
| VIP call (consumer) | `vip/execution/service/ExecutionServiceImpl.java` |

## Open items / risks observed

- **Token stored plaintext** in `execution_template.offline_token` — encryption-at-rest deferred.
- **Is it really an offline token?** `getRefreshToken()` returns the live session refresh
  token; unless the front client requests `offline_access`, it expires with the SSO session
  and Phase 5 fails (`refreshUserToken` → `SecurityException`, execution aborted).
- **Silent invalidation** if the user changes password / is deleted — no UI surfacing of a
  dead token on the template yet.
- **`client_id` coupling:** `refreshUserToken` must use the same client the token was issued
  to (`user-token.client.id`); a mismatch yields `invalid_grant`.