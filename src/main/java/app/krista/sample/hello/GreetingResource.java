package app.krista.sample.hello;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.workspace.Workspace;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Path("/greeting")
public final class GreetingResource {

    private static final Gson JSON = new Gson();
    private final AuthorizationContext authorization;
    private final Workspace workspace;

    @Inject
    public GreetingResource(AuthorizationContext authorization, Workspace workspace) {
        this.authorization = authorization;
        this.workspace = workspace;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response greeting(@HeaderParam(GreetingAuthenticator.VERIFIED_HEADER) String verifiedLaunch) {
        if (!"true".equals(verifiedLaunch) || !authorization.isAuthenticated()) {
            return noStore(Response.status(Response.Status.UNAUTHORIZED));
        }

        var account = authorization.getAuthorizedAccount();
        var person = account == null ? null : account.getPerson();
        String displayName = person == null ? null : person.getPersonName();
        String workspaceId = workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (blank(displayName) || blank(workspaceId) || blank(workspaceName)) {
            return noStore(Response.status(Response.Status.SERVICE_UNAVAILABLE));
        }
        List<String> roles = account.getRoles().stream()
                .map(Role::getName)
                .filter(name -> !blank(name))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return noStore(Response.ok(JSON.toJson(
                new GreetingProfile(displayName, workspaceName, roles, personalAttributes(account)))));
    }

    private List<ProfileAttribute> personalAttributes(Account account) {
        var profile = new ArrayList<ProfileAttribute>();
        String email = account.getPrimaryEmailAddress();
        if (!blank(email)) {
            profile.add(new ProfileAttribute("Primary email", email));
        }
        var valuesById = new HashMap<String, Object>();
        account.getAttributes().forEach((attribute, value) -> valuesById.put(attribute.getAttributeId(), value));
        if (valuesById.isEmpty()) {
            return profile;
        }

        var custom = new ArrayList<ProfileAttribute>();
        try {
            // The workspace provider filters definitions using the bound caller's visibility.
            for (var attribute : workspace.getAttributeProvider().getAttributes()) {
                String name = attribute.getName();
                Object value = valuesById.get(attribute.getAttributeId());
                if (!blank(name) && (value instanceof String || value instanceof Number || value instanceof Boolean)
                        && !value.toString().isBlank()) {
                    custom.add(new ProfileAttribute(name, value.toString()));
                }
            }
        } catch (AuthorizationException ignored) {
            return profile;
        }
        custom.sort(Comparator.comparing(ProfileAttribute::label, String.CASE_INSENSITIVE_ORDER));
        profile.addAll(custom);
        return profile;
    }

    private record GreetingProfile(String displayName, String workspaceName,
                                   List<String> roles, List<ProfileAttribute> attributes) {}

    private record ProfileAttribute(String label, String value) {}

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static Response noStore(Response.ResponseBuilder response) {
        return response.header("Cache-Control", "no-store").build();
    }
}
