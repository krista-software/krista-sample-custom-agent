package app.krista.sample.hello;

import app.krista.extension.executor.Invoker;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/hostUrl")
public class HostingResource {

    private static final Gson JSON = new Gson();

    private final Invoker invoker;

    @Inject
    public HostingResource(@Named("self") Invoker invoker) {
        this.invoker = invoker;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHostingUrl() {
        Map<String, Object> attributes = invoker.getAttributes();
        try {
            String appUrl = HelloExtension.requireAppUrl(attributes);
            return Response.ok(JSON.toJson(Map.of("payload", Map.of("hostingUrl", appUrl)))).build();
        } catch (IllegalArgumentException error) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(JSON.toJson(Map.of("message", error.getMessage())))
                    .build();
        }
    }
}
