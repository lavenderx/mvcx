package io.quasar.demo;

import io.quasar.core.eventbus.EventBusVerticle;
import io.quasar.core.eventbus.context.AppContext;
import io.quasar.core.eventbus.msg.HttpEventMsg;
import io.quasar.core.eventbus.msg.HttpMsgConvert;
import io.quasar.core.util.JsonConfigUtil;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Args;
import io.vertx.core.impl.VertxFactoryImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VertxFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventServerStart {

    public static void main(String[] sargs) {
        Args args = new Args(sargs);
        String confArg = args.map.get("-conf");
        if (confArg == null) {
            confArg = "dev";
        }

        log.info("server start use conf:" + confArg);

        JsonObject config = JsonConfigUtil.getConfig(EventServerStart.class).getJsonObject(confArg);
        JsonObject sys = config.getJsonObject("sys");
        sys.forEach(entry -> {
            System.getProperties().put(entry.getKey(), entry.getValue());
        });


        AppContext appContext = AppContext.create(config);

        VertxFactory factory = new VertxFactoryImpl();
        final Vertx vertx = factory.vertx();

        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config);

        EventBusVerticle eventBusVerticle = new EventBusVerticle(appContext);
        vertx.deployVerticle(eventBusVerticle, deploymentOptions, res -> {
            if (res.succeeded()) {
                log.info("Deployment main eventbusVerticle id is: " + res.result());
            } else {
                log.info("Deployment failed!");
                res.cause().printStackTrace();
            }
        });

        DeploymentOptions readConsumeOpts = readConsumeOpts();
        readConsumeOpts.setConfig(config);
        vertx.deployVerticle("HttpConsumerVerticle", readConsumeOpts, res -> {
            if (res.succeeded()) {
                log.info("Deployment id is: " + res.result());
            } else {
                log.info("Deployment failed!");
                res.cause().printStackTrace();
            }
        });


        vertx.eventBus().registerDefaultCodec(HttpEventMsg.class, new HttpMsgConvert());

    }


    public static DeploymentOptions readConsumeOpts() {
        final DeploymentOptions options = new DeploymentOptions();
        //options.setHa(false);
        options.setInstances(5);
        options.setWorker(true);
        options.setMultiThreaded(true);
        return options;

    }
}
