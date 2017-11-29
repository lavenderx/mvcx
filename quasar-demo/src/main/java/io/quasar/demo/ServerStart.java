package io.quasar.demo;

import io.quasar.core.AutoVerticle;
import io.quasar.core.eventbus.context.AppContext;
import io.quasar.core.util.JsonConfigUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Args;
import io.vertx.core.impl.VertxFactoryImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VertxFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerStart extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Args args = new Args(new String[]{});
        String confArg = args.map.get("-conf");
        if (confArg == null) {
            confArg = "dev";
        }

        log.info("server start use conf:" + confArg);

        JsonObject config = JsonConfigUtil.getConfig(ServerStart.class).getJsonObject(confArg);
        JsonObject sys = config.getJsonObject("sys");
        sys.forEach(entry -> {
            System.getProperties().put(entry.getKey(), entry.getValue());
        });

        AppContext appContext = AppContext.create(config);

        VertxFactory factory = new VertxFactoryImpl();
        final Vertx vertx = factory.vertx();

        final DeploymentOptions deploymentOptions = readOpts();
        deploymentOptions.setConfig(config);

        AutoVerticle autoVerticle = new AutoVerticle(appContext);
        vertx.deployVerticle(autoVerticle, deploymentOptions, res -> {
            if (res.succeeded()) {
                log.info("Deployment id is: " + res.result());
            } else {
                log.info("Deployment failed!");
                res.cause().printStackTrace();
            }
        });
    }

    public static DeploymentOptions readOpts() {
        final DeploymentOptions options = new DeploymentOptions();
        //options.setHa(false);
        options.setInstances(1);
        //options.setWorker(false);
        //options.setMultiThreaded(false);
        return options;
    }
}
