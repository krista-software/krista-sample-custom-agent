package app.krista.sample.hello;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@Service
@ApplicationPath("/")
@ContractsProvided(Application.class)
public class HelloApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(HostingResource.class, GreetingResource.class);
    }
}
