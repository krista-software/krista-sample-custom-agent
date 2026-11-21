package app.krista.sample.hello;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Pattern;

record DeploymentSettings(String appUrl, Environment environment) {

    static final String APP_URL = "App URL";
    static final String ENVIRONMENT = "Deployment Environment";
    static final String SOLUTION_LABS = "Solution Labs";
    static final String US_PRODUCTION = "US Production";
    static final String POC = "POC";
    static final String CLIENT_ORIGIN_HEADER = "X-Hello-Client-Origin";

    private static final Pattern APP_PATH =
            Pattern.compile("/extension/api/[A-Za-z0-9_-]+/static/app/index\\.html");

    static DeploymentSettings from(Map<String, Object> attributes) {
        Object choice = attributes.get(ENVIRONMENT);
        Environment environment;
        if (choice == null || SOLUTION_LABS.equals(choice)) {
            // Existing installs predate the dropdown. Their App URL must still match Solution Labs.
            environment = Environment.LABS;
        } else if (US_PRODUCTION.equals(choice)) {
            environment = Environment.PRODUCTION;
        } else if (POC.equals(choice)) {
            environment = Environment.POC;
        } else {
            throw new IllegalArgumentException("Select Solution Labs, US Production, or POC in Setup.");
        }

        if (!(attributes.get(APP_URL) instanceof String appUrl) || appUrl.isBlank()) {
            throw new IllegalArgumentException("Enter an App URL in Setup.");
        }
        try {
            URI uri = new URI(appUrl);
            if ("https".equalsIgnoreCase(uri.getScheme())
                    && environment.extensionHost().equalsIgnoreCase(uri.getHost())
                    && (uri.getPort() == -1 || uri.getPort() == 443)
                    && uri.getRawUserInfo() == null
                    && APP_PATH.matcher(uri.getRawPath()).matches()) {
                return new DeploymentSettings(appUrl, environment);
            }
        } catch (URISyntaxException ignored) {
            // Use the same Setup error for a malformed URL.
        }
        throw new IllegalArgumentException("App URL must use the selected environment's HTTPS Extension Base URL"
                + " (" + environment.extensionHost() + ") followed by /static/app/index.html.");
    }

    enum Environment {
        LABS("solution-labs.eng.krista.app"),
        PRODUCTION("us.krista.app"),
        POC("poc.krista.app");

        private final String clientHost;

        Environment(String clientHost) {
            this.clientHost = clientHost;
        }

        String extensionHost() {
            return "extension." + clientHost;
        }

        String clientOrigin() {
            return "https://" + clientHost;
        }

        URI iamEndpoint() {
            return URI.create("https://studio." + clientHost + "/auth/v1/introspect");
        }
    }
}
