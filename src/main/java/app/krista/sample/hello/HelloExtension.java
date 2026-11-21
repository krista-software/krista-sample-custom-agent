package app.krista.sample.hello;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
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
import javax.inject.Named;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Domain(id = HelloExtension.AGENT_DOMAIN_ID, name = "Agent",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista", ecosystemVersion = "9bc82a31-a79a-4dc8-bdbd-1ed7e576f7c1")
@Extension(name = "Hello", description = "Sample custom agent", version = "1.3.0",
        implementingDomainIds = HelloExtension.AGENT_DOMAIN_ID, supportingModes = ExecutionMode.LIVE,
        implementationModel = Extension.ImplementationModel.Krista_4_O)
@Containerize
@StaticResource(path = "app", file = "app")
@Field.PickOne(value = DeploymentSettings.ENVIRONMENT, required = true,
        values = {DeploymentSettings.SOLUTION_LABS, DeploymentSettings.US_PRODUCTION},
        description = "Select the Krista environment for this workspace.")
@Field.Text(value = DeploymentSettings.APP_URL, required = true,
        description = "Copy the invoker's Extension Base URL and append /static/app/index.html.")
public class HelloExtension {

    static final String AGENT_DOMAIN_ID = "catEntryDomain_c54d9930-ea03-4c58-bee6-26f90939171d";
    private final Workspace workspace;
    private final Invoker invoker;

    @Inject
    public HelloExtension(Workspace workspace, @Named("self") Invoker invoker) {
        this.workspace = workspace;
        this.invoker = invoker;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator authenticator() {
        return new GreetingAuthenticator(workspace, invoker);
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> attributes) {
        DeploymentSettings.from(attributes);
    }
}
