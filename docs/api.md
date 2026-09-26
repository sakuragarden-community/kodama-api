# Kodama API

API REST per le entità `Member`, `Section` e `Setting`, protette da OAuth 2.0 + JWT.

## Autenticazione

Il bot Discord è un client macchina: non c'è un utente umano davanti a un
browser da cui raccogliere un consenso, quindi il flusso previsto è
**`client_credentials`** (RFC 6749 §4.4) e non l'authorization code.

Le credenziali vivono nella tabella `users`, volutamente separata da `members`:
la prima modella i client che chiamano le API, la seconda le persone della
community.

### 1. Ottenere un access token

```bash
curl -u "$CLIENT_ID:$CLIENT_SECRET" \
     -d "grant_type=client_credentials" \
     http://localhost:8081/oauth2/token
```

```json
{
  "access_token": "eyJraWQiOiJrb2RhbWEtc2lnbmluZy1rZXkiLCJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "BOT"
}
```

Le credenziali possono viaggiare nell'header `Authorization: Basic` (preferito)
oppure come parametri `client_id` / `client_secret` nel corpo.

Il grant `client_credentials` non prevede refresh token: alla scadenza il bot
richiede semplicemente un nuovo access token.

### 2. Chiamare le API

```bash
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
     http://localhost:8081/api/v1/members
```

### Errori del token endpoint

Formato RFC 6749 §5.2, diverso da quello delle API:

| Status | `error`                  | Quando                                        |
|--------|--------------------------|-----------------------------------------------|
| 400    | `invalid_request`        | `grant_type` o credenziali mancanti           |
| 400    | `unsupported_grant_type` | grant diverso da `client_credentials`         |
| 401    | `invalid_client`         | client sconosciuto, segreto errato, disabilitato |

`invalid_client` non distingue «client inesistente» da «segreto errato»: farlo
permetterebbe di enumerare le utenze valide.

### Chiavi di firma

`GET /oauth2/jwks` pubblica la sola chiave pubblica, per chi debba verificare i
token senza condividere la configurazione di questa applicazione.

In sviluppo, se non sono configurate chiavi, ne viene generata una effimera a
ogni avvio (i token non sopravvivono al riavvio). In produzione:

```yaml
kodama:
  security:
    jwt:
      private-key-location: file:/etc/kodama/jwt-private.pem  # PKCS#8
      public-key-location: file:/etc/kodama/jwt-public.pem    # X.509
```

### Ruoli

| Ruolo       | Lettura | Scrittura |
|-------------|---------|-----------|
| `BOT`       | ✅      | ✅        |
| `ADMIN`     | ✅      | ✅        |
| `READ_ONLY` | ✅      | ❌        |

Le authority nel JWT arrivano dal claim `roles` e diventano `ROLE_<nome>`.

### Primo avvio

`BootstrapClientInitializer` crea l'utenza del bot se assente. Il segreto va
fissato da configurazione:

```bash
BOT_CLIENT_SECRET=<segreto> ./mvnw spring-boot:run
```

Se omesso ne viene generato uno casuale, stampato **una sola volta** a log.
L'operazione è idempotente: se il client esiste già non viene toccato, quindi
per cambiare segreto occorre rimuovere la riga da `users`.

## Endpoint

Tutti sotto `/api/v1`, tutti autenticati. Scritture riservate a `BOT` / `ADMIN`.

### Members

| Metodo   | Path                             | Descrizione                              |
|----------|----------------------------------|------------------------------------------|
| `GET`    | `/members`                       | Lista paginata, filtro `?status=`        |
| `GET`    | `/members/{id}`                  | Dettaglio                                |
| `GET`    | `/members/by-discord-id/{id}`    | Lookup per snowflake Discord             |
| `POST`   | `/members`                       | Creazione                                |
| `PUT`    | `/members/{id}`                  | Aggiornamento completo                   |
| `DELETE` | `/members/{id}`                  | Eliminazione                             |

Parametri di paginazione: `page`, `size` (default 20), `sort` (default `id,asc`).

Campi:

| Campo          | Tipo        | Note                                            |
|----------------|-------------|-------------------------------------------------|
| `id`           | int         | generato                                        |
| `discordId`    | string      | univoco, solo cifre, **non modificabile**       |
| `username`     | string      | obbligatorio, max 100                           |
| `createdAt`    | timestamp   | valorizzato dal server                          |
| `joinedAt`     | timestamp   | opzionale                                       |
| `leftAt`       | timestamp   | opzionale                                       |
| `status`       | enum        | `ACTIVE` (default), `INACTIVE`, `LEFT`, `BANNED` |
| `presentation` | string      | opzionale, testo libero                         |
| `experience`   | int         | ≥ 0, default `0`                                |

`discordId` non è aggiornabile via `PUT`: è la chiave naturale assegnata da
Discord.

### Sections

| Metodo   | Path                     | Descrizione                       |
|----------|--------------------------|-----------------------------------|
| `GET`    | `/sections`              | Lista paginata                    |
| `GET`    | `/sections/{id}`         | Dettaglio                         |
| `GET`    | `/sections/by-code/{code}` | Lookup per chiave naturale      |
| `POST`   | `/sections`              | Creazione                         |
| `PUT`    | `/sections/{id}`         | Aggiornamento                     |
| `DELETE` | `/sections/{id}`         | Eliminazione                      |

| Campo  | Tipo   | Note                                              |
|--------|--------|---------------------------------------------------|
| `id`   | int    | generato                                          |
| `name` | string | obbligatorio, max 100                             |
| `code` | string | univoco, max 50, `[a-z0-9-]+`                     |

### Settings

Configurazioni applicative chiave/valore, modificabili a runtime senza
ridistribuire l'applicazione. Ogni configurazione è identificata dal proprio
`path`, una stringa univoca in notazione puntata (es. `discord.channels.welcome`).

| Metodo | Path                | Descrizione                                         |
|--------|---------------------|-----------------------------------------------------|
| `GET`  | `/settings/{path}`  | Legge la configurazione (`404` se il path non esiste) |
| `PUT`  | `/settings/{path}`  | Imposta il valore; crea la configurazione se assente |

| Campo   | Tipo   | Note                                                         |
|---------|--------|--------------------------------------------------------------|
| `path`  | string | univoco, max 255; arriva dall'URL, non dal corpo             |
| `type`  | string | max 32, `[a-z0-9_-]+`, default `text`                        |
| `value` | string | opzionale (`null` ammesso), testo libero                     |

Il valore è **sempre memorizzato come testo**: `type` è un'indicazione per chi
lo legge su come interpretarlo (es. `text`, `number`, `boolean`, `json`), non un
vincolo verificato dal server.

Il `PUT` è un upsert:

- path inesistente → la configurazione viene creata con il `type` indicato, o
  `text` se omesso;
- path esistente → il valore viene sovrascritto (anche con `null`); il `type`
  cambia solo se presente nel corpo.

```bash
curl -X PUT -H "Authorization: Bearer $ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"value": "123456789012345678"}' \
     http://localhost:8081/api/v1/settings/discord.channels.welcome
```

```json
{"path": "discord.channels.welcome", "type": "text", "value": "123456789012345678"}
```

Lato codice, le stesse operazioni sono disponibili in `SettingService`:

```java
String channelId = settingService.getSetting("discord.channels.welcome").value();
settingService.setSetting("xp.multiplier", "2");
settingService.setSetting("xp.multiplier", "2", "number");
```

## Formato degli errori

Tutte le API (non il token endpoint) rispondono con lo stesso corpo:

```json
{
  "timestamp": "2026-08-16T17:48:44.553Z",
  "status": 409,
  "error": "Conflict",
  "message": "Member con discordId '123456789012345678' è già presente",
  "path": "/api/v1/members",
  "violations": []
}
```

`violations` compare solo sugli errori di validazione:

```json
{
  "status": 400,
  "violations": [
    {"field": "username", "message": "username è obbligatorio"},
    {"field": "discordId", "message": "discordId deve contenere solo cifre"}
  ]
}
```

| Status | Quando                                            |
|--------|---------------------------------------------------|
| 400    | payload non valido, enum inesistente, JSON rotto  |
| 401    | token assente, scaduto o non verificabile         |
| 403    | token valido ma ruolo insufficiente               |
| 404    | risorsa inesistente                               |
| 409    | violazione di unicità (`discordId`, `code`)       |

## Struttura del progetto

Organizzazione per feature; dentro ogni feature, per responsabilità:

```
member/
├── model/       entità JPA
├── repository/  accesso ai dati (unico punto che parla col database)
├── dto/         payload di richiesta e risposta
├── mapper/      factory DTO ↔ entità
├── service/     logica di business (interfaccia + impl)
└── web/         controller REST
```

Il controller non conosce il repository e il service non conosce HTTP. Le
dipendenze passano sempre dal costruttore.

`common/` raccoglie ciò che è trasversale: eccezioni di dominio, gestore
centralizzato degli errori, involucro di paginazione, contratto dei mapper.
