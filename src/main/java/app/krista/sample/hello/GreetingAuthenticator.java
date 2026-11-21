package app.krista.sample.hello;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extension.request.protos.http.HttpResponses;
import app.krista.ksdk.workspace.Workspace;
import app.krista.model.field.NamedField;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class GreetingAuthenticator implements RequestAuthenticator {

    // Use only the IAM endpoint verified for this build's deployment.
    private static final URI IAM = URI.create("https://studio.solution-labs.eng.krista.app/auth/v1/introspect");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(4))
            .build();
    private static final Gson JSON = new Gson();
    private static final Pattern JWT = Pattern.compile("[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+");
    static final String VERIFIED_HEADER = "X-Hello-Verified-Launch";

    private final Workspace workspace;

    GreetingAuthenticator(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getScheme() {
        return "Bearer";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of(HttpProtocol.PROTOCOL_NAME);
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest request) {
        if (!(request instanceof HttpRequest http)) {
            return null;
        }
        // Discard client-supplied values before marking this request as verified.
        http.getHeaders().keySet().removeIf(VERIFIED_HEADER::equalsIgnoreCase);
        if (!"GET".equalsIgnoreCase(http.getMethod()) || !"/greeting".equals(http.getUri().getPath())) {
            return null;
        }

        String credential = http.getHeader("Authorization");
        if (credential == null || !credential.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = credential.substring(7);
        if (!JWT.matcher(token).matches()) {
            return null;
        }

        try {
            String accountId = resolveAccountId(token);
            if (accountId != null) {
                http.getHeaders().put(VERIFIED_HEADER, "true");
            }
            return accountId;
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | RuntimeException failure) {
            // A failed IAM or KSDK lookup leaves the caller unauthorized. Never log credentials.
            return null;
        }
    }

    private String resolveAccountId(String token) throws IOException, InterruptedException {
        var request = java.net.http.HttpRequest.newBuilder(IAM)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(7))
                .POST(BodyPublishers.ofString(JSON.toJson(Map.of("payload", Map.of("token", token)))))
                .build();
        var response = HTTP.send(request, BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }

        JsonObject envelope = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!isBoolean(envelope, "isError", false)) {
            return null;
        }
        JsonObject result = envelope.getAsJsonObject("payload");
        if (result == null || !isBoolean(result, "active", true) || !"access_token".equals(text(result, "tokenType"))) {
            return null;
        }

        String workspaceId = workspace.getWorkspaceId();
        var workspaces = result.get("workspaces");
        if (workspaceId == null || workspaceId.isBlank() || workspaces == null || !workspaces.isJsonArray()
                || !workspaces.getAsJsonArray().contains(JSON.toJsonTree(workspaceId))) {
            return null;
        }
        String subject = text(result, "sub");
        String username = text(result, "username");
        if (subject.isBlank() || username.isBlank()) {
            return null;
        }

        var account = workspace.getAccountProvider().lookupAccount(username);
        if (account == null) {
            return null;
        }
        var person = account.getPerson();
        String accountId = account.getAccountId();
        return person != null && subject.equals(person.getPersonId()) && accountId != null && !accountId.isBlank()
                ? accountId : null;
    }

    private static boolean isBoolean(JsonObject object, String key, boolean expected) {
        var value = object.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()
                && value.getAsBoolean() == expected;
    }

    private static String text(JsonObject object, String key) {
        var value = object.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString() : "";
    }

    @Override
    public boolean setServiceAuthorization(String token) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException error, ProtoRequest request) {
        return HttpResponses.createResponse(401, "Authentication required");
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException error) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException error, ProtoRequest request) {
        return HttpResponses.FORBIDDEN;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException error) {
        return null;
    }
}
