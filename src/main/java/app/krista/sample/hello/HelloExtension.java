package app.krista.sample.hello;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.impl.anno.Containerize;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.request.ExecutionMode;
import app.krista.ksdk.workspace.Workspace;

import javax.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Domain(id = HelloExtension.AGENT_DOMAIN_ID, name = "Agent",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista", ecosystemVersion = "9bc82a31-a79a-4dc8-bdbd-1ed7e576f7c1")
@Extension(name = "Hello", description = "Sample custom agent", version = "1.2.0",
        implementingDomainIds = HelloExtension.AGENT_DOMAIN_ID, supportingModes = ExecutionMode.LIVE,
        implementationModel = Extension.ImplementationModel.Krista_4_O)
@Containerize
@StaticResource(path = "app", file = "app")
@Field.Text(value = HelloExtension.APP_URL, required = true,
        description = "The public HTTPS entry URL of the hosted Hello app.")
public class HelloExtension {

    static final String AGENT_DOMAIN_ID = "catEntryDomain_c54d9930-ea03-4c58-bee6-26f90939171d";
    static final String APP_URL = "App URL";
    private static final int MAX_PORT = 65_535;

    private final Workspace workspace;

    @Inject
    public HelloExtension(Workspace workspace) {
        this.workspace = workspace;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator authenticator() {
        return new GreetingAuthenticator(workspace);
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> attributes) {
        requireAppUrl(attributes);
    }

    static String requireAppUrl(Map<String, Object> attributes) {
        if (!(attributes.get(APP_URL) instanceof String appUrl) || appUrl.isBlank()) {
            throw new IllegalArgumentException("Enter an App URL in Setup.");
        }
        try {
            URI uri = new URI(appUrl);
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getPort() <= MAX_PORT) {
                return appUrl;
            }
        } catch (URISyntaxException ignored) {
            // Report the same Setup error for a malformed URL.
        }
        throw new IllegalArgumentException("App URL must be an absolute HTTPS URL with a host and a valid port if specified.");
    }
}
